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
 * 题库生成智能体（question）
 */
@Component
public class QuestionAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        // 1. 构建Prompt
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? "请以{topic}为知识点，生成至少{questionCount}道{questionType}题目，难度：{difficulty}。" +
                  "每道题必须包含：id编号、题干、选项(选择题需4个选项)、正确答案、解析。" +
                  "严格输出JSON数组格式，不要输出其他内容。示例格式：[{{\"id\":1,\"title\":\"题干内容\",\"options\":[\"A.选项1\",\"B.选项2\",\"C.选项3\",\"D.选项4\"],\"answer\":\"A\",\"analysis\":\"解析内容\"}}]"
                : aiAgent.getPromptTemplate();

        // 2. 填充参数
        String questionCount = Objects.requireNonNullElse(params.getString("questionCount"), "5");
        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("topic", topic);
        promptParams.put("questionCount", questionCount);
        promptParams.put("questionType", Objects.requireNonNullElse(params.getString("questionType"), "选择题"));
        promptParams.put("difficulty", Objects.requireNonNullElse(params.getString("difficulty"), "中等"));

        // 3. 调用AI
        String questionContent = SpringAiUtil.callAi(chatClient, promptTemplate, promptParams);

        // 4. 构建学习资源
        LearningResource resource = new LearningResource();
        resource.setUserId(userId);
        resource.setAgentId(aiAgent.getId());
        resource.setResourceType("question");
        resource.setContentFormat("json");
        resource.setResourceTitle(topic + "-题库");
        resource.setResourceContent(questionContent);

        // 5. 元数据
        JSONObject metadata = new JSONObject();
        metadata.put("questionCount", questionCount);
        metadata.put("questionType", params.getString("questionType"));
        metadata.put("difficulty", params.getString("difficulty"));
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);
        resource.setStatus(1);
        resource.setDeleted(0);

        return resource;
    }

    @Override
    public String getSupportRole() {
        return "question";
    }
}
