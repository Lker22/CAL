package com.education.vo;

import lombok.Data;

/**
 * 用户信息返回VO
 * 用于GET /user/info接口，过滤敏感字段
 */
@Data
public class UserInfoVO {
    private Long userId;
    private String username;
    private String nickName;
    private String major;
    private String grade;
    private String phone;
    private String email;
    private String avatar;
}