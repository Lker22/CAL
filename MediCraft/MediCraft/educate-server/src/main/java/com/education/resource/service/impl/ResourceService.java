package com.education.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.fastjson2.JSONObject;
import com.education.resource.mapper.AiAgentMapper;
import com.education.resource.mapper.ResourceGenerateTaskMapper;
import com.education.entity.AiAgent;
import com.education.entity.ResourceGenerateTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class ResourceService {

    @Resource
    private AiAgentMapper aiAgentMapper;
    @Resource
    private ResourceGenerateTaskMapper taskMapper;
    @Resource
    private ResourceAsyncExecutor resourceAsyncExecutor;

    /**
     * 创建资源生成任务（异步）
     * 注意：不加 @Transactional！
     * 异步线程需要立即读取任务记录，如果外层事务还没提交，异步线程查不到任务。
     */
    public String createGenerateTask(Long userId, Long agentId, String topic, JSONObject params) {
        // 1. 验证智能体
        AiAgent aiAgent = aiAgentMapper.selectById(agentId);
        if (aiAgent == null || aiAgent.getStatus() != 1) {
            throw new RuntimeException("智能体不存在或已禁用");
        }

        // 2. 创建任务记录（立即提交，不等事务结束）
        String taskId = UUID.randomUUID().toString().replace("-", "");
        ResourceGenerateTask task = new ResourceGenerateTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setAgentId(agentId);
        task.setTopic(topic);
        task.setParams(params.toJSONString());
        task.setStatus("pending");
        task.setProgress(0);
        taskMapper.insert(task);

        log.info("任务已创建: taskId={}, agentRole={}", taskId, aiAgent.getAgentRole());

        // 3. 调用独立Bean的异步方法（通过Spring代理，@Async才生效）
        resourceAsyncExecutor.executeGenerateTask(taskId);

        return taskId;
    }

    /**
     * 查询生成进度
     */
    public ResourceGenerateTask getGenerateProgress(String taskId) {
        LambdaQueryWrapper<ResourceGenerateTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceGenerateTask::getTaskId, taskId);
        return taskMapper.selectOne(wrapper);
    }
}
