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

/**
 * 文档生成智能体（question）---策略实现，告诉AI生成什么，怎么生成
 */
@Component
public class DocumentAgentStrategy implements AgentGenerateStrategy {

    @Resource
    private ChatClient chatClient;

    @Override
    public LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params) {
        String promptTemplate = aiAgent.getPromptTemplate() == null || aiAgent.getPromptTemplate().isEmpty()
                ? "请以{topic}为主题，生成一份结构化的学习文档，要求：{requirements}，格式为Markdown。"
                : aiAgent.getPromptTemplate();
        //如果模板为 null 或空字符串，则使用默认的提示词模板

        Map<String, Object> promptParams = new HashMap<>();
        //把具体参数填入模板，生成最终提示词
        //创建一个 HashMap 用来存放提示词模板中的占位符参数（key 为占位符名，value 为实际内容）
        promptParams.put("topic", topic);
        promptParams.put("requirements", params.getString("requirements") == null ? "内容详细、逻辑清晰、适合学生学习" : params.getString("requirements"));
        promptParams.put("difficulty", params.getString("difficulty") == null ? "中等" : params.getString("difficulty"));

        String documentContent = SpringAiUtil.callAi(chatClient, promptTemplate, promptParams);

        LearningResource resource = new LearningResource();
        resource.setUserId(userId);
        resource.setAgentId(aiAgent.getId());//设置生成该资源的智能体 ID，记录是哪个智能体产生的
        resource.setResourceType("document");
        resource.setContentFormat("markdown");
        resource.setResourceTitle(topic + "-学习文档");
        resource.setResourceContent(documentContent);

        JSONObject metadata = new JSONObject();
        //把原始请求中传入的具体要求字段（可能是 null）放入元数据
        metadata.put("requirements", params.getString("requirements"));
        metadata.put("difficulty", params.getString("difficulty"));
        resource.setMetadata(metadata.toJSONString());

        resource.setVersion(1);//设置资源版本号，这是初始版本
        resource.setStatus(1);//资源状态，一般约定为1
        resource.setDeleted(0);

        return resource;//返回组装好的 LearningResource 对象
    }

    @Override
    public String getSupportRole() {
        return "document";
    }
}