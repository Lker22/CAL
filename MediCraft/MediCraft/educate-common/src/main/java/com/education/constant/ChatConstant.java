package com.education.constant;

/**
 * 对话相关常量
 */
public class ChatConstant {
    // Redis缓存对话上下文的key前缀
    public static final String CHAT_CONTEXT_REDIS_KEY = "chat:context:%s:%s";
    // 画像抽取触发关键词
    public static final String PROFILE_EXTRACT_KEYWORD = "完成";
    // 对话上下文缓存过期时间（24小时，秒）
    public static final long CHAT_CONTEXT_EXPIRE_SECONDS = 86400;
}