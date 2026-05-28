package com.education.ai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.AiAgent;
import com.education.result.Result;

import java.util.List;

/**
 * AI智能体服务接口
 */
public interface AiAgentService extends IService<AiAgent> {

    /**
     * 查询所有启用的智能体列表（按sort升序排序）
     * @return 智能体列表
     */
    List<AiAgent> listAllEnabledAgents();

    /**
     * 根据ID查询智能体详情
     * @param agentId 智能体ID
     * @return 智能体详情
     */
    AiAgent getAgentById(Long agentId);
}