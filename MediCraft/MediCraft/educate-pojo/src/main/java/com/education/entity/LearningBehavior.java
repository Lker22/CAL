package com.education.entity;

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
 * 学习行为记录
 * </p>
 *
 * @author Lker
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("learning_behavior")
public class LearningBehavior implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习资源ID
     */
    private Long resourceId;

    /**
     * 学习路径步骤ID
     */
    private Long stepId;

    /**
     * 行为类型 学习/做题/查看/完成
     */
    private String behaviorType;

    /**
     * 学习时长(秒)
     */
    private Integer duration;

    /**
     * 做题分数
     */
    private Integer score;

    /**
     * 是否删除 0否 1是
     */
    private Integer deleted;

    /**
     * 行为发生时间
     */
    private LocalDateTime behaviorTime;


}
