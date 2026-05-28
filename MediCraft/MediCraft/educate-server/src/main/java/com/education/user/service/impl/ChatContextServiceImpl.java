package com.education.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.constant.ChatConstant;
import com.education.context.BaseContext;
import com.education.entity.ChatContext;
import com.education.user.mapper.ChatContextMapper;
import com.education.user.service.ChatContextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//对话存储服务
@Service
public class ChatContextServiceImpl
        extends ServiceImpl<ChatContextMapper, ChatContext>
        implements ChatContextService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 生成唯一会话ID（UUID去横杠）
    @Override
    public String generateSessionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 缓存对话到Redis（key: 前缀+userId+sessionId，带过期时间）
    @Override
    public void cacheChatContext(String sessionId, List<ChatContext> contextList) {
        Long userId = BaseContext.getCurrentId();
        String key = String.format(ChatConstant.CHAT_CONTEXT_REDIS_KEY, userId, sessionId);
        redisTemplate.opsForValue().set(key, contextList, ChatConstant.CHAT_CONTEXT_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    // 从Redis读取对话历史
    @Override
    public List<ChatContext> getChatContextFromRedis(String sessionId) {
        Long userId = BaseContext.getCurrentId();
        String key = String.format(ChatConstant.CHAT_CONTEXT_REDIS_KEY, userId, sessionId);
        return (List<ChatContext>) redisTemplate.opsForValue().get(key);
    }

    // 持久化对话到数据库
    @Override
    public void saveChatContext(ChatContext chatContext) {
        // 复用MyBatis-Plus的save方法
        save(chatContext);
    }
}