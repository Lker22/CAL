package com.education.resource.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.education.resource.mapper.AiAgentMapper;
import com.education.resource.mapper.LearningResourceMapper;
import com.education.resource.mapper.ResourceGenerateTaskMapper;
import com.education.resource.service.AgentGenerateStrategy;
import com.education.resource.agent.AgentStrategyFactory;
import com.education.entity.AiAgent;
import com.education.entity.LearningResource;
import com.education.entity.ResourceGenerateTask;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 资源生成异步执行器
 * 独立 Bean，解决同一类内 @Async 自调用不生效的问题
 *
 * 注意：不能加 @Transactional！
 * @Async 在新线程执行，@Transactional 会创建一个长事务。
 * AI 调用可能耗时几十秒，事务超时或连接池耗尽会导致所有更新静默失败。
 * 每次 updateTaskStatus 自动走独立短事务即可。
 */
@Slf4j
@Component
public class ResourceAsyncExecutor {

    @Resource
    private AiAgentMapper aiAgentMapper;
    @Resource
    private ResourceGenerateTaskMapper taskMapper;
    @Resource
    private LearningResourceMapper resourceMapper;
    @Resource
    private AgentStrategyFactory agentStrategyFactory;

    @Async("taskExecutor")
    public void executeGenerateTask(String taskId) {
        log.info("异步任务开始执行: taskId={}", taskId);

        ResourceGenerateTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<ResourceGenerateTask>()
                        .eq(ResourceGenerateTask::getTaskId, taskId)
        );
        if (task == null) {
            log.error("任务不存在: taskId={}", taskId);
            return;
        }

        try {
            // 1. 更新状态为 running
            updateTaskStatus(taskId, "running", 10, null);
            log.info("任务状态更新为running: taskId={}", taskId);

            // 2. 查询智能体
            AiAgent aiAgent = aiAgentMapper.selectById(task.getAgentId());
            if (aiAgent == null) {
                throw new RuntimeException("智能体不存在，agentId=" + task.getAgentId());
            }
            log.info("找到智能体: agentRole={}, agentName={}", aiAgent.getAgentRole(), aiAgent.getAgentName());

            // 3. 获取策略
            AgentGenerateStrategy strategy = agentStrategyFactory.getStrategy(aiAgent.getAgentRole());
            if (strategy == null) {
                throw new RuntimeException("未找到策略: agentRole=" + aiAgent.getAgentRole());
            }
            log.info("找到策略: strategy={}", strategy.getClass().getSimpleName());

            updateTaskStatus(taskId, "running", 30, null);

            // 4. 调用策略生成内容
            JSONObject params = task.getParams() != null ? JSONObject.parseObject(task.getParams()) : new JSONObject();
            log.info("开始调用AI生成: topic={}", task.getTopic());

            LearningResource resource = strategy.generate(aiAgent, task.getUserId(), task.getTopic(), params);
            resource.setTaskId(taskId);

            log.info("AI生成完成，准备保存资源");

            // 5. 保存资源
            resourceMapper.insert(resource);
            log.info("资源保存成功: resourceId={}", resource.getId());

            // 6. 更新任务为成功
            updateTaskStatus(taskId, "success", 100, null);

            // 7. 关联 resourceId
            LambdaUpdateWrapper<ResourceGenerateTask> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ResourceGenerateTask::getTaskId, taskId)
                    .set(ResourceGenerateTask::getResourceId, resource.getId())
                    .set(ResourceGenerateTask::getUpdateTime, LocalDateTime.now());
            taskMapper.update(null, updateWrapper);

            log.info("异步任务完成: taskId={}, resourceId={}", taskId, resource.getId());

        } catch (Exception e) {
            log.error("异步任务失败: taskId={}, error={}", taskId, e.getMessage(), e);
            try {
                updateTaskStatus(taskId, "failed", 0, e.getMessage());
            } catch (Exception ex) {
                log.error("更新失败状态也失败了: taskId={}", taskId, ex);
            }
        }
    }

    /**
     * 更新任务状态（每次调用独立事务，不依赖外层 @Transactional）
     */
    private void updateTaskStatus(String taskId, String status, Integer progress, String errorMsg) {
        LambdaUpdateWrapper<ResourceGenerateTask> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ResourceGenerateTask::getTaskId, taskId)
                .set(ResourceGenerateTask::getStatus, status)
                .set(ResourceGenerateTask::getProgress, progress)
                .set(ResourceGenerateTask::getUpdateTime, LocalDateTime.now());
        if (errorMsg != null) {
            wrapper.set(ResourceGenerateTask::getErrorMsg, errorMsg);
        }
        taskMapper.update(null, wrapper);
    }
}
