package com.education.vo;

import lombok.Data;

/**
 * 完成学习步骤请求VO
 */
@Data
public class CompleteStepVO {

    /**
     * 学习时长(秒)
     */
    private Integer duration;

    /**
     * 学习笔记
     */
    private String notes;

    /**
     * 自评分数
     */
    private Integer score;
}
