package com.education.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习步骤VO(适配前端字段命名)
 */
@Data
public class LearningPathStepVO {

    private Long id;

    private Long pathId;

    /**
     * 步骤标题(对应前端title, 来源stepName)
     */
    private String title;

    /**
     * 步骤描述(对应前端description, 来源stepContent)
     */
    private String description;

    /**
     * 状态 pending/inProgress/completed(对应前端STEP_STATUS的key)
     */
    private String status;

    /**
     * 预计学习时长
     */
    private String duration;

    /**
     * 步骤顺序
     */
    private Integer sort;

    /**
     * 关联资源名称列表
     */
    private List<String> resources;

    /**
     * 关联资源ID(逗号分隔), 前端判断是否已缓存
     */
    private String resourceIds;

    /**
     * 步骤类型: document(学习文档) / quiz(知识测验, 最后一个步骤)
     */
    private String stepType;

    /**
     * 完成时间
     */
    private String completedAt;
}
