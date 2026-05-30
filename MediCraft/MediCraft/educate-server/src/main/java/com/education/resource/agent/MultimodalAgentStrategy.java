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
 * 多模态生成智能体（multimodal）
 * 生成教学视频脚本、动画文案等多模态内容
 * 资源类型存为 document（系统没有独立的"视频"资源类型）
 */
@Component
public class MultimodalAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? """
                  你是专业的多模态教学内容生成智能体。
                  根据学习主题：{topic}，难度：{difficulty}，生成教学视频脚本。
                  必须包含：

                  1. 视频标题和简介
                  2. 分镜脚本（不少于5个分镜，每个分镜包含：画面描述、旁白文字、时长建议）
                  3. 关键知识点图解说明（用文字描述图解内容）
                  4. 总结与课后思考题

                  格式使用Markdown，语言生动、适合学生理解。
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
        resource.setResourceTitle(topic + "-教学视频脚本");
        resource.setResourceContent(content);

        JSONObject metadata = new JSONObject();
        metadata.put("difficulty", params.getString("difficulty"));
        metadata.put("agentRole", "multimodal");
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);
        resource.setStatus(1);
        resource.setDeleted(0);

        return resource;
    }

    @Override
    public String getSupportRole() {
        return "multimodal";
    }
}
