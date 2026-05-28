package com.education.ai.component;

import com.alibaba.fastjson2.JSONObject;
import com.education.ai.service.AgentGenerateStrategy;
import com.education.ai.utils.SpringAiUtil;
import com.education.entity.AiAgent;
import com.education.entity.LearningResource;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 实操案例智能体（case）
 *
 */
@Component
public class CaseAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? """
                  你是专业实操案例智能体。
                  根据学生提供的知识点：{topic}，难度：{difficulty}，生成可直接动手操作的实战案例。
                  必须严格按照以下结构输出：
                  
                  1. 实践目标
                  2. 环境准备
                  3. 操作步骤
                  4. 完整可运行代码/命令
                  5. 效果展示
                  6. 常见问题
                  
                  内容必须详细、可直接运行、适合学生练习。
                  格式使用 Markdown。
                  """
                : aiAgent.getPromptTemplate();

        // 参数
        Map<String, Object> promptParams = new HashMap<>();
        promptParams.put("topic", topic);
        promptParams.put("difficulty", Objects.requireNonNullElse(params.getString("difficulty"), "中等"));

        // AI 生成
        String content = SpringAiUtil.callAi(chatClient, promptTemplate, promptParams);

        // 封装资源
        LearningResource resource = new LearningResource();
        resource.setUserId(userId);
        resource.setAgentId(aiAgent.getId());
        resource.setResourceType("case");        // 标记：实操
        resource.setContentFormat("markdown");    // 格式：markdown
        resource.setResourceTitle(topic + " - 实操案例");
        resource.setResourceContent(content);

        // 元数据
        JSONObject metadata = new JSONObject();
        metadata.put("difficulty", params.getString("difficulty"));
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);
        resource.setStatus(1);
        resource.setDeleted(0);

        return resource;
    }

    @Override
    public String getSupportRole() {
        return "case";
    }
}