package com.education.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.ChatContext;

import java.util.List;

public interface ChatContextService extends IService<ChatContext> {
    // 生成sessionId
    String generateSessionId();

    // 缓存对话到Redis
    void cacheChatContext(String sessionId, List<ChatContext> contextList);

    // 从Redis查对话
    List<ChatContext> getChatContextFromRedis(String sessionId);

    // 持久化对话到DB
    void saveChatContext(ChatContext chatContext);
}