package com.education.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "education.jwt")
public class JwtProperties {
    /** 用户端用户 token 名称（请求头 key） */
    private String userTokenName = "Authorization";
    /** 用户端密钥 */
    private String userSecretKey;
    /** token 过期时间（毫秒） */
    private Long userTtl;
}