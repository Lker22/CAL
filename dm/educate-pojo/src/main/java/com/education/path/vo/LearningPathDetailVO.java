package com.education.path.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学习路径详情VO(包含步骤列表, 适配前端字段)
 */
@Data
public class LearningPathDetailVO {

    private Long id;

    private Long userId;

    /**
     * 路径名称
     */
    private String pathName;

    /**
     * 路径名称(前端title字段的别名)
     */
    @JsonProperty("title")
    public String getTitle() {
        return this.pathName;
    }

    /**
     * 状态 pending/inProgress/completed/paused
     */
    private String status;

    /**
     * 总步骤数
     */
    private Integer totalStep;

    /**
     * 前端totalSteps别名
     */
    @JsonProperty("totalSteps")
    public Integer getTotalSteps() {
        return this.totalStep;
    }

    /**
     * 已完成步骤数
     */
    private Integer completedSteps;

    /**
     * 进度百分比 0-100
     */
    private Integer progress;

    /**
     * 学习周期描述
     */
    private String duration;

    /**
     * 步骤列表(适配前端字段)
     */
    private List<LearningPathStepVO> steps;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime createTime;

    /**
     * 前端createdAt别名
     */
    @JsonProperty("createdAt")
    public String getCreatedAt() {
        return this.createTime != null ? this.createTime.toString().replace("T", " ").substring(0, 16) : null;
    }

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime updateTime;
}
