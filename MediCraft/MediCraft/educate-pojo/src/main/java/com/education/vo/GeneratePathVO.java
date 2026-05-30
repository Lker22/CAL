package com.education.vo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 生成学习路径请求VO
 */
@Data
public class GeneratePathVO {

    /**
     * 学习主题
     */
    @NotBlank(message = "学习主题不能为空")
    private String subject;

    /**
     * 学习目标
     */
    private String goal;

    /**
     * 学习周期(周) 2/4/8/12
     */
    private String duration;

    /**
     * 学习强度 low/medium/high
     */
    private String intensity;
}
