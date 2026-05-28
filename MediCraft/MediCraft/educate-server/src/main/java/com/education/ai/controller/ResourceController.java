package com.education.ai.controller;

import com.alibaba.fastjson2.JSONObject;
import com.education.ai.service.impl.ResourceService;
import com.education.entity.ResourceGenerateTask;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;



/**
 * 学习资源API（对应src/api/resource.js）
 */
@RestController
@RequestMapping("/resource")
public class ResourceController {

    @Resource
    private ResourceService resourceService;

    /**
     * 生成学习资源（异步）
     * @param req { agentId, resourceType, topic, params }
     * @return 任务ID
     */
    @PostMapping("/generate")
    public JSONObject generateResource(@RequestBody JSONObject req, @RequestAttribute("userId") Long userId) {
        Long agentId = req.getLong("agentId");
        String topic = req.getString("topic");
        JSONObject params = req.getJSONObject("params");

        String taskId = resourceService.createGenerateTask(userId, agentId, topic, params);
        
        JSONObject result = new JSONObject();
        result.put("code", 200);
        result.put("msg", "生成任务已创建");
        result.put("data", new JSONObject().fluentPut("taskId", taskId));
        return result;
    }

    /**
     * 获取生成进度
     * @param taskId 任务ID
     * @return 进度信息
     */
    @GetMapping("/generate/progress/{taskId}")
    public JSONObject getGenerateProgress(@PathVariable String taskId) {
        ResourceGenerateTask task = resourceService.getGenerateProgress(taskId);
        
        JSONObject result = new JSONObject();
        result.put("code", 200);
        result.put("data", new JSONObject()
                .fluentPut("taskId", task.getTaskId())
                .fluentPut("status", task.getStatus())
                .fluentPut("progress", task.getProgress())
                .fluentPut("errorMsg", task.getErrorMsg())
                .fluentPut("resourceId", task.getResourceId()));
        return result;
    }

    /**
     * 获取资源列表
     * @param userId 用户ID
     * @param type 资源类型
     * @param keyword 关键词
     * @param page 页码
     * @param pageSize 页大小
     * @return 资源列表
     */
    // （可根据需求实现，调用resourceMapper分页查询）

    /**
     * 获取资源详情
     * @param resourceId 资源ID
     * @return 资源详情
     */
    // （可根据需求实现，调用resourceMapper.selectById）
}