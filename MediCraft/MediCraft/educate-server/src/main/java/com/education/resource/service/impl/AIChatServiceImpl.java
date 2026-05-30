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

        String promptTemplate = """
                你是智能学习助手，负责引导用户完善个人学习画像，只收集以下信息：
                1.专业、年级
                2.知识基础（弱/中/强）
                3.认知风格（视觉型/听觉型/动手型）
                4.学习目标
                5.薄弱知识点
                6.学习节奏（慢/中/快）
                7.资源偏好（文档/视频/题库/实操）
                
                对话历史：
                {history}
                
                用户当前输入：{msg}
                要求：语气友好简洁，只问未收集的信息，用户输入“完成”则结束收集。
                """;

        // 格式化历史对话
        String historyStr = formatHistory(history);

        // 替换模板变量，生成最终给AI的提示词
        String prompt = promptTemplate
                .replace("{history}", historyStr)
                .replace("{msg}", userMessage);

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
                无法提取则填“未知”，不要输出多余内容。
                
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