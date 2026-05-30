package com.education.resource.agent;

import com.education.resource.service.BaseAgent;
import com.education.resource.utils.SpringAiUtil;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

/**
 * 视频生成智能体
 */
@Component
public class VideoAgent extends BaseAgent {

    @Override
    protected Prompt buildPrompt(String userInput) {
        String template = """
            请根据用户需求生成专业视频脚本，包含：
            1. 视频主题
            2. 分镜列表（不少于5个）
            3. 素材类型（图片/文字/音频）
            4. 输出标准JSON格式
            
            用户需求：{userInput}
            """;

        //把用户输入放入 map，替换模板里的 {userInput}
        Map<String, Object> map = new HashMap<>();
        map.put("userInput", userInput);
        //构建 Prompt 对象，交给 Spring AI 调用
        return new PromptTemplate(template).create(map);
    }

    @Override
    protected String handleAiResponse(ChatResponse response, String taskId, SseEmitter emitter) throws Exception {
        // 统一使用我们的空安全工具类
        return SpringAiUtil.getAiResponseContent(response);
    }
}