package com.education.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 学习路径步骤
 * </p>
 *
 * @author Lker
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_path_step")
public class LearningPathStep implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 路径ID
     */
    private Long pathId;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤要求
     */
    private String stepContent;

    /**
     * 关联资源ID(逗号分隔)
     */
    private String resourceIds;

    /**
     * 步骤顺序
     */
    private Integer sort;

    /**
     * 完成状态
     */
    private Integer finishStatus;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    @TableLogic
    private Integer deleted;


}
