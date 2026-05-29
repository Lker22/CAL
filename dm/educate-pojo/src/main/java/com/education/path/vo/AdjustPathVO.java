package com.education.path.vo;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 调整学习路径请求VO
 */
@Data
public class AdjustPathVO {

    /**
     * 调整类型 extend/compress/reorder/add/remove
     */
    @NotBlank(message = "调整类型不能为空")
    private String adjustmentType;

    /**
     * 调整原因
     */
    @NotBlank(message = "调整原因不能为空")
    private String reason;

    /**
     * 附加参数(JSON字符串)
     */
    private String params;
}
