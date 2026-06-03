package com.education.dto;

import lombok.Data;

/**
 * 答题提交DTO
 */
@Data
public class QuizAnswerDTO {
    /**
     * 题目索引(对应题库JSON数组中的位置)
     */
    private Integer questionId;

    /**
     * 用户答案 (如 "A")
     */
    private String userAnswer;

    /**
     * 答题耗时(秒)
     */
    private Integer spendTime;
}
