package com.education.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ResourceQueryDTO {
    // 资源类型：document/mind/question/video/case
    private String type;

    // 搜索关键词（标题/知识点）
    private String keyword;

    // 分页参数
    private Integer page = 1;

    private Integer pageSize = 10;

    // 用户ID（后台从登录态获取，前端无需传）
    @JsonIgnore
    private Long userId;
}