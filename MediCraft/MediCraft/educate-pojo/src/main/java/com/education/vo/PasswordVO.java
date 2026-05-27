package com.education.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 修改密码请求VO
 * 用于PUT /user/password接口
 */
@Data
public class PasswordVO {
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}