package com.education.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TutorAnswerVO {
    /** 答疑记录ID */
    private Long recordId;

    /** 会话ID */
    private String sessionId;

    /** 学生问题 */
    private String question;

    /** 文字解答 */
    private String textAnswer;

    /** 图解URL */
    private String imageUrl;

    /** 讲解视频URL */
    private String videoUrl;

    /** 提问时间 */
    private LocalDateTime createTime;
}