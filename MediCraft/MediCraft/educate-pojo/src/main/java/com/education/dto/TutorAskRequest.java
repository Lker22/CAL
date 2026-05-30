package com.education.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class TutorAskRequest {
    /** 问题文本 */
    @NotBlank(message = "问题不能为空")
    private String text;

    /** 图片列表（base64 或 文件URL） */
    private List<String> images;

    /** 当前用户ID（后端从token解析，前端可不传） */
    private Long userId;
}