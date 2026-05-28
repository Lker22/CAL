package com.education.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.ai.mapper.AiAgentMapper;
import com.education.ai.service.AiAgentService;
import com.education.entity.AiAgent;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI智能体服务实现类
 */
@Service
public class AiAgentServiceImpl extends ServiceImpl<AiAgentMapper, AiAgent> implements AiAgentService {

    @Override
    public List<AiAgent> listAllEnabledAgents() {
        LambdaQueryWrapper<AiAgent> queryWrapper = new LambdaQueryWrapper<>();
        // 只过滤逻辑删除，不过滤status（6个预置智能体必须全部显示）
        queryWrapper.eq(AiAgent::getDeleted, 0)
                // 按sort升序，保证1-6的固定顺序
                .orderByAsc(AiAgent::getSort);
        return list(queryWrapper);
    }

    @Override
    public AiAgent getAgentById(Long agentId) {
        LambdaQueryWrapper<AiAgent> queryWrapper = new LambdaQueryWrapper<>();
        // 只过滤逻辑删除，不过滤status
        queryWrapper.eq(AiAgent::getId, agentId)
                .eq(AiAgent::getDeleted, 0);
        return getOne(queryWrapper);
    }
}