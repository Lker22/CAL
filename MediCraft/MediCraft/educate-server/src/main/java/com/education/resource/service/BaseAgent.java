package com.education.resource.service;

import com.alibaba.fastjson2.JSONObject;
import com.education.resource.mapper.LearningResourceMapper;
import com.education.resource.mapper.ResourceGenerateTaskMapper;
import com.education.entity.LearningResource;
import com.education.entity.ResourceGenerateTask;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 智能体抽象父类
 */
public abstract class BaseAgent {

    @Autowired
    protected ChatClient chatClient;

    @Autowired
    protected ResourceGenerateTaskMapper taskMapper;

    @Autowired
    protected LearningResourceMapper resourceMapper;

    // 生成任务ID
    protected String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 抽象方法：子类实现Prompt构建
    protected abstract Prompt buildPrompt(String userInput);

    // 抽象方法：处理流式响应
    protected abstract String handleAiResponse(ChatResponse response, String taskId, SseEmitter emitter) throws Exception;

    // 核心流式生成
    public SseEmitter generate(String userInput, String resourceType, Long userId, Long agentId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        String taskId = generateTaskId();

        // 初始化任务（和你系统表结构一致）
        ResourceGenerateTask task = new ResourceGenerateTask();
        task.setTaskId(taskId);
        task.setUserId(userId);
        task.setAgentId(agentId);
        task.setTopic(userInput);
        task.setStatus("running");
        task.setProgress(0);
        taskMapper.insert(task);

        // 异步执行
        CompletableFuture.runAsync(() -> {
            try {
                Prompt prompt = buildPrompt(userInput);
                ChatResponse response = chatClient.prompt(prompt).call().chatResponse();

                // 处理返回
                String content = handleAiResponse(response, taskId, emitter);

                // 保存学习资源
                LearningResource resource = new LearningResource();
                resource.setUserId(userId);
                resource.setAgentId(agentId);
                resource.setTaskId(taskId);
                resource.setResourceType(resourceType);
                resource.setContentFormat("json");
                resource.setResourceTitle("视频脚本-" + taskId.substring(0, 8));
                resource.setResourceContent(content);

                JSONObject metadata = new JSONObject();
                metadata.put("userInput", userInput);
                resource.setMetadata(metadata.toJSONString());

                resource.setVersion(1);
                resource.setStatus(1);
                resource.setDeleted(0);
                resourceMapper.insert(resource);

                // 更新任务成功
                updateTaskSuccess(taskId, resource.getId());
                sendProgress(emitter, 100);
                emitter.complete();

            } catch (Exception e) {
                updateTaskFailed(taskId, e.getMessage());
                try {
                    emitter.completeWithError(e);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return emitter;
    }

    // 发送进度
    protected void sendProgress(SseEmitter emitter, int progress) {
        try {
            emitter.send(SseEmitter.event().name("progress").data(progress));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 更新任务成功
    private void updateTaskSuccess(String taskId, Long resourceId) {
        ResourceGenerateTask task = new ResourceGenerateTask();
        task.setTaskId(taskId);
        task.setStatus("success");
        task.setProgress(100);
        task.setResourceId(resourceId);
        taskMapper.updateById(task);
    }

    // 更新任务失败
    private void updateTaskFailed(String taskId, String errorMsg) {
        ResourceGenerateTask task = new ResourceGenerateTask();
        task.setTaskId(taskId);
        task.setStatus("failed");
        task.setErrorMsg(errorMsg);
        taskMapper.updateById(task);
    }
}