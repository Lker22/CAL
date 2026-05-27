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
 * 题库答题记录表
 * </p>
 *
 * @author Lker
 * @since 2026-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("question_answer_record")
public class QuestionAnswerRecord implements Serializable {

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
     * 题库资源ID
     */
    private Long resourceId;

    /**
     * 题目ID（对应metadata中的questions数组索引）
     */
    private Integer questionId;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 正确答案
     */
    private String correctAnswer;

    /**
     * 是否正确 0否 1是
     */
    private Integer isCorrect;

    /**
     * 答题时长(秒)
     */
    private Integer spendTime;

    /**
     * 答题时间
     */
    private LocalDateTime answerTime;


}
