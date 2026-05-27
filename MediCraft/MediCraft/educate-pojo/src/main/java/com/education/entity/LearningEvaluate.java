package com.education.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习效果评估
 * </p>
 *
 * @author Lker
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_evaluate")
public class LearningEvaluate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 评估报告
     */
    private String evaluateContent;

    /**
     * 提升建议
     */
    private String improveSuggest;

    /**
     * 知识点掌握度(JSON)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String knowledgeMastery;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;


}
