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

        String prompt = """
                你是专业的医学教育学习效果评估智能体，请基于以下学生数据生成多维度评估报告：

                学生画像信息：
                {studentInfo}

                学习行为数据：
                {behaviorData}

                答题记录数据：
                {answerData}

                学习路径完成情况：
                {pathInfo}

                评估模块要求：{includeModules}

                请按以下结构输出报告（不要输出JSON以外的多余标记）：

                【学习概况】
                简要总结学习时长、活跃度、资源使用情况。

                【知识点掌握度】
                必须输出JSON格式（不要加```json标记），例如：
                {"解剖学":75,"生理学":60,"病理学":85}

                【薄弱点分析】
                列出掌握度低于60%的知识点，分析原因。

                【提升建议】
                提升建议：结合学生画像（认知风格/资源偏好）给出个性化学习方案。
                """.replace("{studentInfo}", studentInfo)//把真实数据替换进提示词
                  .replace("{behaviorData}", behaviorData)
                  .replace("{answerData}", answerData)
                  .replace("{pathInfo}", pathInfo != null ? pathInfo : "未指定学习路径")
                  .replace("{includeModules}", includeModules != null ? includeModules.toString() : "全部");

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
     */
    public String parseKnowledgeMastery(String aiResponse) {
        if (aiResponse == null) {
            return "{}";
        }
        // 匹配 { "知识点": 数字, ... } 格式的JSON
        int jsonStart = aiResponse.indexOf("{");
        int jsonEnd = aiResponse.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd > jsonStart) {
            String jsonCandidate = aiResponse.substring(jsonStart, jsonEnd + 1).trim();
            // 基本校验：包含冒号说明是key:value格式
            if (jsonCandidate.contains(":")) {
                return jsonCandidate;
            }
        }
        return "{}";
    }
}
