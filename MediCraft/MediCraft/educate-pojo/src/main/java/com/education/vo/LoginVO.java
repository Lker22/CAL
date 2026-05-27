package com.education.vo;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginVO {
    @NotBlank(message = "账号不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}