package com.education.vo;

import lombok.Data;

/**
 * 推荐资源VO
 */
@Data
public class RecommendedResourceVO {

    private Long id;

    /**
     * 资源标题
     */
    private String title;

    /**
     * 资源类型 document/mindmap/quiz/practice
     */
    private String type;

    /**
     * 匹配度 0-100
     */
    private Integer matchScore;

    /**
     * 推荐理由
     */
    private String reason;

    /**
     * 关联路径名称
     */
    private String pathName;
}
