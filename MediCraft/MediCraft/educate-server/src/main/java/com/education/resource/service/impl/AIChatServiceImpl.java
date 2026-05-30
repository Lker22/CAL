package com.education.resource.service.impl;

import com.alibaba.fastjson2.JSON;
import com.education.resource.service.AIChatService;
import com.education.context.BaseContext;
import com.education.entity.ChatContext;
import com.education.entity.StudentProfile;
import com.education.user.service.ChatContextService;
import com.education.utils.SpringContextUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;

    public AIChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    //AI引导对话
    @Override
    public String chatWithAI(String sessionId, String userMessage) {
        // 1. 获取对话上下文服务（手动从Spring拿Bean，解决循环依赖）
        ChatContextService chatContextService = SpringContextUtil.getBean(ChatContextService.class);
        // 2. 从Redis获取当前会话的历史对话
        List<ChatContext> history = chatContextService.getChatContextFromRedis(sessionId);
        if (history == null) {
            history = new ArrayList<>();
        }

        // 3. 获取用户已有画像
        com.education.user.service.StudentProfileService profileService =
                SpringContextUtil.getBean(com.education.user.service.StudentProfileService.class);
        StudentProfile existingProfile = null;
        try { existingProfile = profileService.getByUserId(); } catch (Exception ignored) {}

        String profileInfo = "暂无画像数据";
        if (existingProfile != null) {
            StringBuilder ps = new StringBuilder();
            if (existingProfile.getKnowledgeBase() != null) ps.append("知识基础: ").append(existingProfile.getKnowledgeBase()).append("\n");
            if (existingProfile.getCognitiveStyle() != null) ps.append("认知风格: ").append(existingProfile.getCognitiveStyle()).append("\n");
            if (existingProfile.getLearningGoal() != null) ps.append("学习目标: ").append(existingProfile.getLearningGoal()).append("\n");
            if (existingProfile.getLearningPace() != null) ps.append("学习节奏: ").append(existingProfile.getLearningPace()).append("\n");
            if (existingProfile.getResourcePreference() != null) ps.append("资源偏好: ").append(existingProfile.getResourcePreference()).append("\n");
            if (existingProfile.getErrorPronePoints() != null) ps.append("易错点: ").append(existingProfile.getErrorPronePoints()).append("\n");
            if (ps.length() > 0) profileInfo = ps.toString();
        }

        String promptTemplate = """
                你是智能学习助手，有两项职责：
                1. 当用户询问画像信息时，直接展示已有的画像数据
                2. 当用户补充或修改画像信息时，引导收集缺失的信息

                用户当前画像数据：
                __PROFILE__

                对话历史：
                __HISTORY__

                用户当前输入：__MSG__

                要求：
                - 如果用户在询问画像（如"我的画像"、"画像信息"），直接展示已有数据，不要反问
                - 如果用户在补充新信息，友好确认并引导收集未完善的信息
                - 语气友好简洁，用中文回复
                """;

        // 格式化历史对话
        String historyStr = formatHistory(history);

        // 替换模板变量，生成最终给AI的提示词
        String prompt = promptTemplate
                .replace("__PROFILE__", profileInfo)
                .replace("__HISTORY__", historyStr)
                .replace("__MSG__", userMessage);

        //调用Spring AI，获取回复
        String reply = chatClient.prompt()
                .user(prompt)// 传入提示词
                .call() // 调用模型
                .content(); // 获取文本回复

        // 保存对话
        ChatContext ctx = new ChatContext();
        ctx.setUserId(BaseContext.getCurrentId());// 当前登录用户ID
        ctx.setSessionId(sessionId); // 会话ID
        ctx.setUserMessage(userMessage); // 用户输入
        ctx.setAiReply(reply); // AI回复
        ctx.setIsExtractProfile(0); //未抽取画像标记（int类型）
        ctx.setChatType("profile"); // 对话类型：画像收集

        // 保存对话：更新Redis缓存 + 入库持久化
        history.add(ctx);
        chatContextService.cacheChatContext(sessionId, history);
        chatContextService.saveChatContext(ctx);

        return reply;// 返回AI回复给前端
    }

    //抽取画像
    @Override
    public StudentProfile extractProfileFromChat(String sessionId) {
        //  获取对话服务 + 读取历史对话
        ChatContextService chatContextService = SpringContextUtil.getBean(ChatContextService.class);
        List<ChatContext> history = chatContextService.getChatContextFromRedis(sessionId);
        if (history == null) {
            history = new ArrayList<>();
        }

        String extractPromptTemplate = """
                从以下对话中提取学生学习画像，严格返回JSON格式：
                字段：knowledgeBase、cognitiveStyle、learningGoal、errorPronePoints、learningPace、resourcePreference
                无法提取则填"未知"，不要输出多余内容。
                
                对话内容：{history}
                """;

        // 格式化对话 + 生成最终提示词
        String historyStr = formatHistory(history);
        String finalPrompt = extractPromptTemplate.replace("{history}", historyStr);

        // 正确调用
        String json = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

        // JSON转Java对象,强转（学生画像实体）
        StudentProfile profile = JSON.parseObject(json, StudentProfile.class);
        profile.setUpdateScene("对话抽取");

        // 标记已抽取并更新数据库
        for (ChatContext c : history) {
            c.setIsExtractProfile(1); // 修复为 int 1
            chatContextService.updateById(c);
        }

        return profile;// 返回结构化画像
    }

    //格式化对话
    // 把List<ChatContext> 转成可读文本：用户：xx \n AI：xx
    private String formatHistory(List<ChatContext> list) {
        StringBuilder sb = new StringBuilder();
        for (ChatContext c : list) {
            sb.append("用户：").append(c.getUserMessage()).append("\n");
            sb.append("AI：").append(c.getAiReply()).append("\n");
        }
        return sb.toString();
    }
}