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
 * 视频观看进度表
 * </p>
 *
 * @author Lker
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("video_watch_progress")
public class VideoWatchProgress implements Serializable {

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
     * 视频资源ID
     */
    private Long resourceId;

    /**
     * 当前观看位置(秒)
     */
    private Integer currentPosition;

    /**
     * 视频总时长(秒)
     */
    private Integer totalDuration;

    /**
     * 观看次数
     */
    private Integer watchCount;

    /**
     * 最后观看时间
     */
    private LocalDateTime lastWatchTime;


}
