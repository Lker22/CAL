package com.education.resource.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.education.resource.mapper.LearningResourceMapper;
import com.education.resource.service.impl.LearningResourceService;
import com.education.resource.service.impl.ResourceService;
import com.education.context.BaseContext;
import com.education.entity.LearningResource;
import com.education.entity.ResourceGenerateTask;
import com.education.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/resource")
@Slf4j
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private LearningResourceService learningResourceService;

    @Autowired
    private LearningResourceMapper resourceMapper;

    /**
     * 生成学习资源（异步）
     */
    @PostMapping("/generate")
    public Result<?> generateResource(@RequestBody JSONObject req) {
        Long userId = BaseContext.getCurrentId();//当前登录人（资源归属）
        Long agentId = req.getLong("agentId");//用哪个 AI 智能体
        String topic = req.getString("topic");//生成主题

        if (agentId == null || topic == null || topic.isBlank()) {
            return Result.fail(400, "参数不完整：agentId和topic不能为空");
        }

        try {
            String taskId = resourceService.createGenerateTask(userId, agentId, topic, req);
            ResourceGenerateTask task = resourceService.getGenerateProgress(taskId);//返回任务信息，前端轮询进度
            return Result.success(task);
        } catch (Exception e) {
            log.error("资源生成任务创建失败", e);
            return Result.fail(500, "任务创建失败：" + e.getMessage());
        }
    }

    /**
     * 资源生成进度查询
     */
    @GetMapping("/generate/progress/{taskId}")
    public Result<ResourceGenerateTask> getGenerateProgress(@PathVariable String taskId) {
        ResourceGenerateTask task = resourceService.getGenerateProgress(taskId);
        if (task == null) {
            return Result.fail(404, "任务不存在");
        }
        return Result.success(task);
    }

    /**
     * 我的学习资源列表
     */
    @GetMapping("/list")
    public Result<List<LearningResource>> getMyResourceList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword) {

        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
        //数据权限控制：只能查自己的资源
        wrapper.eq(LearningResource::getUserId, userId)
                .eq(type != null && !type.isBlank(), LearningResource::getResourceType, type)
                .and(keyword != null && !keyword.isBlank(), w ->
                        w.like(LearningResource::getResourceTitle, keyword)
                                .or()
                                .like(LearningResource::getKnowledgePoint, keyword))
                .orderByDesc(LearningResource::getCreateTime);

        List<LearningResource> list = resourceMapper.selectList(wrapper);
        return Result.success(list);
    }

    /**
     * 获取资源详情
     */
    @GetMapping("/detail/{id}")
    public Result<LearningResource> getResourceDetail(@PathVariable Long id) {
        LearningResource resource = resourceMapper.selectById(id);
        if (resource == null || !resource.getUserId().equals(BaseContext.getCurrentId())) {
            return Result.fail(404, "资源不存在");
        }
        return Result.success(resource);
    }

    /**
     * 删除资源
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteResource(@PathVariable Long id) {
        LearningResource resource = resourceMapper.selectById(id);
        if (resource == null || !resource.getUserId().equals(BaseContext.getCurrentId())) {
            return Result.fail(404, "资源不存在");
        }
        resourceMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
