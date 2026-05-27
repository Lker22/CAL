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
 * 对话上下文
 * </p>
 *
 * @author Lker
 * @since 2026-05-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_context")
public class ChatContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 对话会话ID
     */
    private String sessionId;

    private String chatType;

    /**
     * 用户输入
     */
    private String userMessage;

    /**
     * AI回复
     */
    private String aiReply;

    /**
     * 是否抽取画像 0否 1是
     */
    private Integer isExtractProfile;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;


}
