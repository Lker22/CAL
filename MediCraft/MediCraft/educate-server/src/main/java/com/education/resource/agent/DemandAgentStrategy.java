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
 * 需求解析智能体（demand）
 * 分析学习需求，拆解生成任务，输出需求分析文档
 */
@Component
public class DemandAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? """
                  你是专业的需求解析智能体。
                  根据学生的学习主题：{topic}，难度：{difficulty}，进行需求分析并输出结构化文档。
                  必须包含以下内容：

                  1. 学习目标拆解（列出3-5个具体目标）
                  2. 知识点清单（按优先级排列）
                  3. 推荐学习路径（阶段划分和顺序）
                  4. 所需前置知识
                  5. 预计学习时长
                  6. 推荐资源类型（文档/视频/题库/实操）

                  格式使用Markdown，内容清晰、可操作。
                  """
                : aiAgent.getPromptTemplate();

        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("topic", topic);
        promptParams.put("difficulty", Objects.requireNonNullElse(params.getString("difficulty"), "中等"));

        String content = SpringAiUtil.callAi(chatClient, promptTemplate, promptParams);

        LearningResource resource = new LearningResource();
        resource.setUserId(userId);
        resource.setAgentId(aiAgent.getId());
        resource.setResourceType("document");
        resource.setContentFormat("markdown");
        resource.setResourceTitle(topic + "-需求分析");
        resource.setResourceContent(content);

        JSONObject metadata = new JSONObject();
        metadata.put("difficulty", params.getString("difficulty"));
        metadata.put("agentRole", "demand");
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);
        resource.setStatus(1);
        resource.setDeleted(0);

        return resource;
    }

    @Override
    public String getSupportRole() {
        return "demand";
    }
}
