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
 * 多模态学习资源
 * </p>
 *
 * @author Lker
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_resource")
public class LearningResource implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 生成智能体ID
     */
    private Long agentId;

    /**
     * 资源类型 document/mind/question/video/case
     */
    private String resourceType;

    private String contentFormat;

    /**
     * 资源标题
     */
    private String resourceTitle;

    /**
     * 资源内容/URL
     */
    private String resourceContent;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String metadata;

    private String fileUrl;

    private String taskId;

    private Integer version;

    /**
     * 关联课程
     */
    private String courseName;

    /**
     * 关联知识点
     */
    private String knowledgePoint;

    /**
     * 难度 简单/中等/困难
     */
    private String difficulty;

    /**
     * 状态
     */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
