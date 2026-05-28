package com.education.ai.utils;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Map;

public final class SpringAiUtil {

    // 抽取重复的空安全获取AI回复代码
    public static String getAiResponseContent(ChatResponse response) {
        String defaultContent = "生成失败，请稍后重试";
        if (response == null || CollectionUtils.isEmpty(response.getResults())) {
            return defaultContent;
        }
        AssistantMessage assistantMessage = response.getResult().getOutput();
        return assistantMessage == null ? defaultContent : assistantMessage.getText();
    }

    // 全量抽取：创建Prompt + 调用AI + 获取结果（彻底消灭重复）
    public static String callAi(ChatClient chatClient, String promptTemplate, Map<String, Object> promptParams) {
        Prompt prompt = new PromptTemplate(promptTemplate).create(promptParams);
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        return getAiResponseContent(response);
    }
}