package com.education.ai.service.impl;

import com.alibaba.fastjson2.JSON;
import com.education.ai.service.AIChatService;
import com.education.context.BaseContext;
import com.education.entity.ChatContext;
import com.education.entity.StudentProfile;
import com.education.user.service.ChatContextService;
import com.education.utils.SpringContextUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;

    public AIChatServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chatWithAI(String sessionId, String userMessage) {
        ChatContextService chatContextService = SpringContextUtil.getBean(ChatContextService.class);
        List<ChatContext> history = chatContextService.getChatContextFromRedis(sessionId);

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

        // 拼接参数
        String historyStr = formatHistory(history);
        String prompt = promptTemplate
                .replace("{history}", historyStr)
                .replace("{msg}", userMessage);

        //调用
        String reply = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        // 保存对话
        ChatContext ctx = new ChatContext();
        ctx.setUserId(BaseContext.getCurrentId());
        ctx.setSessionId(sessionId);
        ctx.setUserMessage(userMessage);
        ctx.setAiReply(reply);
        ctx.setIsExtractProfile(0); // 这里修复为 int 0
        ctx.setChatType("profile");

        history.add(ctx);
        chatContextService.cacheChatContext(sessionId, history);
        chatContextService.saveChatContext(ctx);

        return reply;
    }

    @Override
    public StudentProfile extractProfileFromChat(String sessionId) {
        ChatContextService chatContextService = SpringContextUtil.getBean(ChatContextService.class);
        List<ChatContext> history = chatContextService.getChatContextFromRedis(sessionId);

        String extractPromptTemplate = """
                从以下对话中提取学生学习画像，严格返回JSON格式：
                字段：knowledgeBase、cognitiveStyle、learningGoal、errorPronePoints、learningPace、resourcePreference
                无法提取则填“未知”，不要输出多余内容。
                
                对话内容：{history}
                """;

        String historyStr = formatHistory(history);
        String finalPrompt = extractPromptTemplate.replace("{history}", historyStr);

        // 正确调用
        String json = chatClient.prompt()
                .user(finalPrompt)
                .call()
                .content();

        StudentProfile profile = JSON.parseObject(json, StudentProfile.class);
        profile.setUpdateScene("对话抽取");

        // 标记已抽取
        for (ChatContext c : history) {
            c.setIsExtractProfile(1); // 修复为 int 1
            chatContextService.updateById(c);
        }

        return profile;
    }

    private String formatHistory(List<ChatContext> list) {
        StringBuilder sb = new StringBuilder();
        for (ChatContext c : list) {
            sb.append("用户：").append(c.getUserMessage()).append("\n");
            sb.append("AI：").append(c.getAiReply()).append("\n");
        }
        return sb.toString();
    }
}