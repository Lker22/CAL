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
import java.util.Objects;

/**
 * 思维导图生成智能体（mind）
 */
@Component
public class MindAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        // 1. 构建Prompt
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? "请以{topic}为核心，生成一份知识点思维导图的Markdown格式内容（使用-、*层级表示），要求覆盖{knowledgePoints}，难度：{difficulty}。"
                : aiAgent.getPromptTemplate();

        // 2. 填充参数
        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("topic", topic);
        promptParams.put("knowledgePoints", Objects.requireNonNullElse(params.getString("knowledgePoints"), "核心知识点、重点、难点"));
        promptParams.put("difficulty", Objects.requireNonNullElse(params.getString("difficulty"), "中等"));

        // 3. 调用大模型 + 空安全处理（消除空指针警告）
        String mindContent = SpringAiUtil.callAi(chatClient, promptTemplate, promptParams);

        // 4. 构建学习资源
        LearningResource resource = new LearningResource();
        resource.setUserId(userId);
        resource.setAgentId(aiAgent.getId());
        resource.setResourceType("mind");
        resource.setContentFormat("markdown");
        resource.setResourceTitle(topic + "-思维导图");
        resource.setResourceContent(mindContent);

        // 5. 元数据
        JSONObject metadata = new JSONObject();
        metadata.put("knowledgePoints", params.getString("knowledgePoints"));
        metadata.put("difficulty", params.getString("difficulty"));
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);
        resource.setStatus(1);
        resource.setDeleted(0);

        return resource;
    }

    @Override
    public String getSupportRole() {
        return "mind";
    }
}