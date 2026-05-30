package com.education.resource.agent;

import com.alibaba.fastjson2.JSONObject;
import com.education.resource.service.AgentGenerateStrategy;
import com.education.resource.utils.SpringAiUtil;
import com.education.entity.AiAgent;
import com.education.entity.LearningResource;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档生成智能体 — 策略实现，告诉AI生成什么，怎么生成
 */
@Component
public class DocumentAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? "请以{topic}为主题，生成一份结构化的学习文档，要求：{requirements}，难度：{difficulty}，格式为Markdown。"
                : aiAgent.getPromptTemplate();

        // 兼容前端 additionalRequirements 和后端 requirements 两个字段名
        String requirements = params.getString("requirements");
        if (requirements == null) {
            requirements = params.getString("additionalRequirements");
        }
        if (requirements == null || requirements.isBlank()) {
            requirements = "内容详细、逻辑清晰、适合学生学习";
        }

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("topic", topic);
        promptParams.put("requirements", requirements);
        promptParams.put("difficulty", params.getString("difficulty") == null ? "中等" : params.getString("difficulty"));

        String documentContent = SpringAiUtil.callAi(chatClient, promptTemplate, promptParams);

        LearningResource resource = new LearningResource();
        resource.setUserId(userId);
        resource.setAgentId(aiAgent.getId());
        resource.setResourceType("document");
        resource.setContentFormat("markdown");
        resource.setResourceTitle(topic + "-学习文档");
        resource.setResourceContent(documentContent);

        JSONObject metadata = new JSONObject();
        metadata.put("requirements", requirements);
        metadata.put("difficulty", params.getString("difficulty"));
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);
        resource.setStatus(1);
        resource.setDeleted(0);

        return resource;
    }

    @Override
    public String getSupportRole() {
        return "document";
    }
}
