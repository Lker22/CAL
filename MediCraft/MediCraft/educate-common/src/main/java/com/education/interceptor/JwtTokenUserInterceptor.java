package com.education.interceptor;

import com.education.context.BaseContext;
import com.education.properties.JwtProperties;
import com.education.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@Slf4j
public class JwtTokenUserInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是控制器方法，直接放行（如静态资源）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 1. 从请求头中获取 token，去掉 "Bearer " 前缀
        String header = request.getHeader(jwtProperties.getUserTokenName());
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        log.info("拦截到请求：{}，token：{}", request.getRequestURI(), token);

        // 2. 校验 token
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            log.info("当前用户ID：{}", userId);
            BaseContext.setCurrentId(userId);   // 存入线程上下文
            return true; // 放行
        } catch (Exception ex) {
            log.error("JWT 校验失败：{}", ex.getMessage());
            response.setStatus(401);
            return false; // 拦截
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后移除线程变量，避免内存泄漏
        BaseContext.removeCurrentId();
    }
}