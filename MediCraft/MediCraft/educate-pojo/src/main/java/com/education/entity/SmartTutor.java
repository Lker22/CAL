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
 * 智能辅导答疑
 * </p>
 *
 * @author Lker
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("smart_tutor")
public class SmartTutor implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    /**
     * 学生问题
     */
    private String question;

    /**
     * 多会话共用id
     */
    private String sessionId;

    /**
     * 文字解答
     */
    private String textAnswer;

    /**
     * 图解URL
     */
    private String imageUrl;

    /**
     * 讲解视频URL
     */
    private String videoUrl;

    @TableLogic
    private Integer deleted;


    private LocalDateTime createTime;


}
