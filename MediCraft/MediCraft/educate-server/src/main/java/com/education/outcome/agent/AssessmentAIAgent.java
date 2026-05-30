package com.education.outcome.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

/**
 * 学习评估AI智能体
 * 封装SpringAI调用逻辑，生成评估报告/薄弱点分析/提升建议
 */
@Component
public class AssessmentAIAgent {

    private final ChatClient chatClient;

    public AssessmentAIAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 生成评估报告
     * @param studentInfo 学生画像文本描述
     * @param behaviorData 学习行为数据文本
     * @param answerData 答题记录数据文本
     * @param pathInfo 学习路径完成情况
     * @param includeModules 评估模块列表
     * @return AI生成的评估报告文本
     */
    //传入学生画像、学习行为、答题记录、学习路径、评估模块
    public String generateEvaluation(String studentInfo, String behaviorData,
                                     String answerData, String pathInfo,
                                     java.util.List<String> includeModules) {

        // 用 .replace() 而非 .formatted()，避免用户数据中的 % 字符被当成格式化占位符
        String prompt = """
                你是专业的医学教育学习效果评估智能体，请基于以下学生数据生成评估报告。

                学生画像信息：
                __STUDENT_INFO__

                学习行为数据：
                __BEHAVIOR_DATA__

                答题记录数据：
                __ANSWER_DATA__

                学习路径完成情况：
                __PATH_INFO__

                请严格按以下格式输出（不要加```json标记，不要输出多余内容）：

                【学习概况】
                简要总结学习时长、活跃度、资源使用情况。

                【知识点掌握度】
                直接输出JSON对象，键为知识点名称，值为0-100的掌握度分数。例如：
                {"解剖学":75,"生理学":60,"病理学":85}
                如果没有具体知识点数据，请根据学习行为推断3-5个相关知识点并给出掌握度分数。

                【薄弱点分析】
                列出掌握度低于60%的知识点，分析原因。

                【提升建议】
                提升建议：结合学生画像给出个性化学习方案。
                """
                .replace("__STUDENT_INFO__", studentInfo != null ? studentInfo : "暂无画像数据")
                .replace("__BEHAVIOR_DATA__", behaviorData != null ? behaviorData : "暂无学习行为数据")
                .replace("__ANSWER_DATA__", answerData != null ? answerData : "暂无答题记录")
                .replace("__PATH_INFO__", pathInfo != null ? pathInfo : "未指定学习路径");

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * 从AI响应中提取提升建议
     */
    public String extractImproveSuggest(String aiResponse) {
        if (aiResponse == null) {
            return "暂无提升建议";
        }
        String marker = "提升建议：";
        int idx = aiResponse.lastIndexOf(marker);
        if (idx != -1) {
            return aiResponse.substring(idx + marker.length()).trim();
        }
        // 尝试其他常见标记
        marker = "【提升建议】";
        idx = aiResponse.indexOf(marker);
        if (idx != -1) {
            String after = aiResponse.substring(idx + marker.length()).trim();
            // 截取到下一个【或结尾
            int nextSection = after.indexOf("【");
            if (nextSection != -1) {
                after = after.substring(0, nextSection).trim();
            }
            return after;
        }
        return "暂无提升建议";
    }

    /**
     * 从AI响应中提取知识点掌握度JSON字符串
     * 定位【知识点掌握度】段落，从中提取JSON
     */
    public String parseKnowledgeMastery(String aiResponse) {
        if (aiResponse == null || aiResponse.isEmpty()) {
            return "{}";
        }

        // 0. 先去掉AI可能包裹的代码块标记 ```json ... ```
        String cleaned = aiResponse.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

        // 1. 尝试定位【知识点掌握度】段落
        String section = cleaned;
        String[] markers = {"【知识点掌握度】", "知识点掌握度：", "知识点掌握度:"};
        for (String marker : markers) {
            int idx = cleaned.indexOf(marker);
            if (idx != -1) {
                section = cleaned.substring(idx + marker.length());
                // 截取到下一个【或结尾
                int nextSection = section.indexOf("【");
                if (nextSection != -1) {
                    section = section.substring(0, nextSection);
                }
                break;
            }
        }

        // 2. 在段落中找JSON（第一个{到最后一个}）
        int jsonStart = section.indexOf("{");
        int jsonEnd = section.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            String jsonCandidate = section.substring(jsonStart, jsonEnd + 1).trim();
            if (jsonCandidate.contains(":")) {
                return jsonCandidate;
            }
        }

        // 3. 兜底：在整个清理后的响应中找
        jsonStart = cleaned.indexOf("{");
        jsonEnd = cleaned.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            String jsonCandidate = cleaned.substring(jsonStart, jsonEnd + 1).trim();
            if (jsonCandidate.contains(":")) {
                return jsonCandidate;
            }
        }

        return "{}";
    }
}
