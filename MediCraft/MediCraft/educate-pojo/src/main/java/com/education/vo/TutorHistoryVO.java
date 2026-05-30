package com.education.vo;

import lombok.Data;
import java.util.List;

@Data
public class TutorHistoryVO {
    /** 总条数 */
    private Long total;

    /** 总页数 */
    private Long pages;

    /** 当前页数据 */
    private List<TutorAnswerVO> records;
}