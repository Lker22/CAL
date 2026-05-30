package com.education.resource.service;

import com.alibaba.fastjson2.JSONObject;
import com.education.entity.AiAgent;
import com.education.entity.LearningResource;

/**
 * 智能体生成策略接口（不同智能体实现不同生成逻辑）
 */
public interface AgentGenerateStrategy {
    /**
     * 生成资源
     * @param aiAgent 智能体配置
     * @param userId 用户ID
     * @param topic 生成主题
     * @param params 生成参数
     * @return 学习资源
     */
    LearningResource generate(AiAgent aiAgent, Long userId, String topic, JSONObject params);

    /**
     * 获取支持的智能体角色
     * @return agentRole
     */
    String getSupportRole();
}