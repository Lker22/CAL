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
 * 学生学习画像
 * </p>
 *
 * @author Lker
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("student_profile")
public class StudentProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 知识基础 弱/中/强
     */
    private String knowledgeBase;

    /**
     * 认知风格 视觉型/听觉型/动手型
     */
    private String cognitiveStyle;

    /**
     * 学习目标
     */
    private String learningGoal;

    /**
     * 易错点(JSON数组)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String errorPronePoints;

    /**
     * 学习节奏 慢/中/快
     */
    private String learningPace;

    /**
     * 资源偏好 文档/视频/题库/实操
     */
    private String resourcePreference;

    /**
     * 学习习惯补充
     */
    private String learningHabits;

    /**
     * 更新场景 对话/做题/路径调整
     */
    private String updateScene;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
