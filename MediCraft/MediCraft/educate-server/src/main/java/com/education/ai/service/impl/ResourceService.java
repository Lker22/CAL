package com.education.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.alibaba.fastjson2.JSONObject;
import com.education.ai.mapper.AiAgentMapper;
import com.education.ai.mapper.LearningResourceMapper;
import com.education.ai.mapper.ResourceGenerateTaskMapper;
import com.education.ai.service.AgentGenerateStrategy;
import com.education.ai.component.AgentStrategyFactory;
import com.education.entity.AiAgent;
import com.education.entity.LearningResource;
import com.education.entity.ResourceGenerateTask;
import jakarta.annotation.Resource;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ResourceService {

    @Resource
    private AiAgentMapper aiAgentMapper;
    @Resource
    private ResourceGenerateTaskMapper taskMapper;
    @Resource
    private LearningResourceMapper resourceMapper;

    @Resource
    private AgentStrategyFactory agentStrategyFactory;

    /**
     * 创建资源生成任务（异步）
     */
    @Transactional(rollbackFor = Exception.class)
    public String createGenerateTask(Long userId, Long agentId, String topic, JSONObject params) {
        // 1. 验证智能体
        AiAgent aiAgent = aiAgentMapper.selectById(agentId);
        if (aiAgent == null || aiAgent.getStatus() != 1) {
            throw new RuntimeException("智能体不存在或已禁用");
        }

        // 2. 创建任务
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

//        // 3. 发送MQ
//        JSONObject taskMsg = new JSONObject();
//        taskMsg.put("taskId", taskId);
//        rabbitTemplate.convertAndSend(RabbitMQConfig.RESOURCE_GENERATE_QUEUE, taskMsg.toJSONString());
        executeGenerateTask(taskId);

        return taskId;
    }

    /**
     * 执行资源生成（消费MQ）
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void executeGenerateTask(String taskId) {
        ResourceGenerateTask task = taskMapper.selectByTaskId(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在：" + taskId);
        }

        try {
            updateTaskStatus(taskId, "running", 20, null);

            AiAgent aiAgent = aiAgentMapper.selectById(task.getAgentId());
            if (aiAgent == null) {
                throw new RuntimeException("智能体不存在");
            }

            AgentGenerateStrategy strategy = agentStrategyFactory.getStrategy(aiAgent.getAgentRole());
            if (strategy == null) {
                throw new RuntimeException("未找到策略：" + aiAgent.getAgentRole());
            }

            updateTaskStatus(taskId, "running", 50, null);

            JSONObject params = JSONObject.parseObject(task.getParams());

            LearningResource resource = strategy.generate(aiAgent, task.getUserId(), task.getTopic(), params);
            resource.setTaskId(taskId);

            resourceMapper.insert(resource);

            updateTaskStatus(taskId, "success", 100, null);
            LambdaUpdateWrapper<ResourceGenerateTask> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ResourceGenerateTask::getTaskId, taskId)
                    .set(ResourceGenerateTask::getResourceId, resource.getId());
            taskMapper.update(null, wrapper);

        } catch (Exception e) {
            updateTaskStatus(taskId, "failed", 0, e.getMessage());
            throw new RuntimeException("资源生成失败：" + e.getMessage(), e);
        }
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, String status, Integer progress, String errorMsg) {
        LambdaUpdateWrapper<ResourceGenerateTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ResourceGenerateTask::getTaskId, taskId)
                .set(ResourceGenerateTask::getStatus, status)
                .set(ResourceGenerateTask::getProgress, progress)
                .set(errorMsg != null, ResourceGenerateTask::getErrorMsg, errorMsg);
        taskMapper.update(null, wrapper);
    }

    public ResourceGenerateTask getGenerateProgress(String taskId) {
        return taskMapper.selectByTaskId(taskId);
    }
}