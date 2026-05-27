package com.education.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 资源生成任务表
 * </p>
 *
 * @author Lker
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("resource_generate_task")
public class ResourceGenerateTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 任务唯一ID
     */
    private String taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 生成智能体ID
     */
    private Long agentId;

    /**
     * 生成主题
     */
    private String topic;

    /**
     * 生成参数
     */
    private String params;

    /**
     * 任务状态 pending/running/success/failed
     */
    private String status;

    /**
     * 生成进度 0-100
     */
    private Integer progress;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 生成成功后关联的资源ID
     */
    private Long resourceId;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;


}
