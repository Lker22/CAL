package com.education.vo;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 记录学习行为请求VO
 */
@Data
public class RecordBehaviorVO {

    /**
     * 学习路径ID
     */
    @NotNull(message = "路径ID不能为空")
    private Long pathId;

    /**
     * 学习步骤ID
     */
    private Long stepId;

    /**
     * 行为类型 learning/quiz/view/complete
     */
    private String behaviorType;

    /**
     * 学习时长(秒)
     */
    private Integer duration;

    /**
     * 资源ID
     */
    private Long resourceId;
}
