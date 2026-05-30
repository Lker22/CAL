package com.education.path.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.ai.mapper.LearningResourceMapper;
import com.education.entity.LearningBehavior;
import com.education.entity.LearningPath;
import com.education.entity.LearningPathStep;
import com.education.entity.LearningResource;
import com.education.path.mapper.LearningBehaviorMapper;
import com.education.path.mapper.LearningPathMapper;
import com.education.path.mapper.LearningPathStepMapper;
import com.education.path.service.LearningPathService;
import com.education.vo.*;
import com.education.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 学习路径Service实现
 *
 * 数据库状态说明：
 * learning_path.status: doing(进行中) / finish(已完成)
 * learning_path_step.finish_status: 0(未完成) / 1(已完成)
 * 前端展示状态通过VO层映射: pending / inProgress / completed
 */
@Service
@Slf4j
public class LearningPathServiceImpl implements LearningPathService {

    private final LearningPathMapper pathMapper;
    private final LearningPathStepMapper stepMapper;
    private final LearningBehaviorMapper behaviorMapper;
    private final LearningResourceMapper resourceMapper;
    private final ChatClient chatClient;

    public LearningPathServiceImpl(LearningPathMapper pathMapper,
                                    LearningPathStepMapper stepMapper,
                                    LearningBehaviorMapper behaviorMapper,
                                    LearningResourceMapper resourceMapper,
                                    ChatClient.Builder chatClientBuilder) {
        this.pathMapper = pathMapper;
        this.stepMapper = stepMapper;
        this.behaviorMapper = behaviorMapper;
        this.resourceMapper = resourceMapper;
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * 一键生成学习路径
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> generatePath(Long userId, GeneratePathVO vo) {
        log.info("生成学习路径, userId={}, subject={}", userId, vo.getSubject());

        // 1. 调用AI生成学习步骤
        List<LearningPathStep> steps = generateStepsWithAI(vo);

        // 2. 创建学习路径(数据库status为doing)
        LearningPath path = new LearningPath();
        path.setUserId(userId);
        path.setPathName(vo.getSubject());
        path.setTotalStep(steps.size());
        path.setCurrentStep(0);
        path.setStatus("doing");
        pathMapper.insert(path);

        // 3. 批量保存步骤(数据库finish_status为0=未完成)
        for (int i = 0; i < steps.size(); i++) {
            LearningPathStep step = steps.get(i);
            step.setPathId(path.getId());
            step.setSort(i + 1);
            step.setFinishStatus(0);
            stepMapper.insert(step);
        }

        // 4. 返回路径详情
        return getPathDetail(path.getId(), userId);
    }

    /**
     * 调用AI生成学习步骤
     */
    private List<LearningPathStep> generateStepsWithAI(GeneratePathVO vo) {
        String durationLabel = switch (vo.getDuration()) {
            case "2" -> "2周";
            case "8" -> "2个月";
            case "12" -> "3个月";
            default -> "1个月";
        };

        String intensityLabel = switch (vo.getIntensity()) {
            case "low" -> "轻松(每天1小时)";
            case "high" -> "密集(每天3小时以上)";
            default -> "适中(每天2小时)";
        };

        String prompt = """
                请为以下学习需求生成学习路径步骤，严格返回JSON数组格式，每个元素包含stepName和stepContent字段。

                学习主题：%s
                学习目标：%s
                学习周期：%s
                学习强度：%s

                要求：
                1. 步骤数量控制在4-8个，按从基础到进阶排列
                2. stepName为步骤名称（简洁明了）
                3. stepContent为步骤内容描述（包含具体学习内容和要求）
                4. 只返回JSON数组，不要输出多余内容

                示例格式：
                [{"stepName":"基础语法学习","stepContent":"学习变量、数据类型、控制流等基础语法知识，完成课后练习题"}]
                """.formatted(vo.getSubject(), vo.getGoal(), durationLabel, intensityLabel);

        try {
            String reply = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseStepsFromAI(reply);
        } catch (Exception e) {
            log.warn("AI生成步骤失败，使用默认步骤: {}", e.getMessage());
            return generateDefaultSteps(vo.getSubject());
        }
    }

    private List<LearningPathStep> parseStepsFromAI(String aiReply) {
        List<LearningPathStep> steps = new ArrayList<>();
        try {
            String jsonStr = aiReply.trim();
            int startIdx = jsonStr.indexOf('[');
            int endIdx = jsonStr.lastIndexOf(']');
            if (startIdx >= 0 && endIdx > startIdx) {
                jsonStr = jsonStr.substring(startIdx, endIdx + 1);
            }

            JSONArray arr = JSON.parseArray(jsonStr);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                LearningPathStep step = new LearningPathStep();
                step.setStepName(obj.getString("stepName"));
                step.setStepContent(obj.getString("stepContent"));
                steps.add(step);
            }
        } catch (Exception e) {
            log.warn("解析AI步骤失败: {}", e.getMessage());
        }

        if (steps.isEmpty()) {
            return generateDefaultSteps("学习主题");
        }
        return steps;
    }

    private List<LearningPathStep> generateDefaultSteps(String subject) {
        List<LearningPathStep> steps = new ArrayList<>();

        LearningPathStep step1 = new LearningPathStep();
        step1.setStepName(subject + "基础入门");
        step1.setStepContent("了解" + subject + "的基本概念和核心知识");
        steps.add(step1);

        LearningPathStep step2 = new LearningPathStep();
        step2.setStepName(subject + "核心知识");
        step2.setStepContent("深入学习" + subject + "的核心技术和方法");
        steps.add(step2);

        LearningPathStep step3 = new LearningPathStep();
        step3.setStepName(subject + "实践练习");
        step3.setStepContent("通过实际练习巩固所学知识，完成相关练习题");
        steps.add(step3);

        LearningPathStep step4 = new LearningPathStep();
        step4.setStepName(subject + "综合应用");
        step4.setStepContent("综合运用所学知识，完成一个完整的实践项目");
        steps.add(step4);

        return steps;
    }

    /**
     * 获取学习路径列表(分页)
     */
    @Override
    public Result<?> getPathList(Long userId, Integer page, Integer pageSize, String status) {
        Page<LearningPath> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<LearningPath> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPath::getUserId, userId);

        if (status != null && !status.isEmpty()) {
            if ("completed".equals(status)) {
                wrapper.eq(LearningPath::getStatus, "finish");
            } else {
                wrapper.eq(LearningPath::getStatus, "doing");
            }
        }
        wrapper.orderByDesc(LearningPath::getCreateTime);

        Page<LearningPath> pathPage = pathMapper.selectPage(pageParam, wrapper);

        List<LearningPathDetailVO> voList = new ArrayList<>();
        for (LearningPath path : pathPage.getRecords()) {
            voList.add(buildPathDetailVO(path));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", voList);
        result.put("total", pathPage.getTotal());
        result.put("current", pathPage.getCurrent());
        result.put("size", pathPage.getSize());

        return Result.success(result);
    }

    /**
     * 获取路径详情(含步骤)
     */
    @Override
    public Result<?> getPathDetail(Long pathId, Long userId) {
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(404, "学习路径不存在");
        }

        LearningPathDetailVO vo = buildPathDetailVO(path);
        return Result.success(vo);
    }

    /**
     * 构建路径详情VO(含步骤和进度)
     */
    private LearningPathDetailVO buildPathDetailVO(LearningPath path) {
        LearningPathDetailVO vo = new LearningPathDetailVO();
        vo.setId(path.getId());
        vo.setUserId(path.getUserId());
        vo.setPathName(path.getPathName());
        vo.setTotalStep(path.getTotalStep());
        vo.setCreateTime(path.getCreateTime());
        vo.setUpdateTime(path.getUpdateTime());

        List<LearningPathStep> steps = stepMapper.selectByPathId(path.getId());

        long completedCount = steps.stream()
                .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 1)
                .count();
        vo.setCompletedSteps((int) completedCount);

        int progress = path.getTotalStep() > 0
                ? (int) (completedCount * 100 / path.getTotalStep())
                : 0;
        vo.setProgress(progress);

        if ("finish".equals(path.getStatus())) {
            vo.setStatus("completed");
        } else if (completedCount > 0) {
            vo.setStatus("inProgress");
        } else {
            vo.setStatus("pending");
        }

        vo.setDuration(path.getTotalStep() + "个步骤");

        List<LearningPathStepVO> stepVOs = convertToStepVOs(steps);
        vo.setSteps(stepVOs);

        return vo;
    }

    /**
     * 将步骤实体列表转换为前端VO列表
     * 数据库finish_status: 0=未完成, 1=已完成
     * 前端status: pending(待学习), inProgress(学习中), completed(已完成)
     */
    private List<LearningPathStepVO> convertToStepVOs(List<LearningPathStep> steps) {
        List<LearningPathStepVO> voList = new ArrayList<>();

        int firstUnfinishedSort = Integer.MAX_VALUE;
        for (LearningPathStep step : steps) {
            if (step.getFinishStatus() == null || step.getFinishStatus() == 0) {
                firstUnfinishedSort = Math.min(firstUnfinishedSort, step.getSort());
                break;
            }
        }

        for (LearningPathStep step : steps) {
            LearningPathStepVO stepVO = new LearningPathStepVO();
            stepVO.setId(step.getId());
            stepVO.setPathId(step.getPathId());
            stepVO.setTitle(step.getStepName());
            stepVO.setDescription(step.getStepContent());
            stepVO.setSort(step.getSort());
            stepVO.setDuration("3-5天");

            if (step.getFinishStatus() != null && step.getFinishStatus() == 1) {
                stepVO.setStatus("completed");
            } else if (step.getSort() == firstUnfinishedSort) {
                stepVO.setStatus("inProgress");
            } else {
                stepVO.setStatus("pending");
            }

            if (step.getFinishTime() != null) {
                stepVO.setCompletedAt(step.getFinishTime().toString().substring(0, 10));
            }

            if (step.getResourceIds() != null && !step.getResourceIds().isEmpty()) {
                List<String> resourceNames = resolveResourceNames(step.getResourceIds());
                stepVO.setResources(resourceNames);
            } else {
                stepVO.setResources(new ArrayList<>());
            }

            voList.add(stepVO);
        }
        return voList;
    }

    private List<String> resolveResourceNames(String resourceIds) {
        List<String> names = new ArrayList<>();
        String[] ids = resourceIds.split(",");
        for (String idStr : ids) {
            try {
                Long resId = Long.parseLong(idStr.trim());
                LearningResource resource = resourceMapper.selectById(resId);
                if (resource != null && resource.getResourceTitle() != null) {
                    names.add(resource.getResourceTitle());
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return names;
    }

    /**
     * 完成学习步骤(打卡)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> completeStep(Long stepId, Long userId, CompleteStepVO vo) {
        LearningPathStep step = stepMapper.selectById(stepId);
        if (step == null) {
            return Result.fail(404, "学习步骤不存在");
        }

        LearningPath path = pathMapper.selectById(step.getPathId());
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作此步骤");
        }

        LambdaUpdateWrapper<LearningPathStep> stepWrapper = new LambdaUpdateWrapper<>();
        stepWrapper.eq(LearningPathStep::getId, stepId)
                .set(LearningPathStep::getFinishStatus, 1)
                .set(LearningPathStep::getFinishTime, LocalDateTime.now());
        stepMapper.update(null, stepWrapper);

        List<LearningPathStep> allSteps = stepMapper.selectByPathId(path.getId());
        long completedCount = allSteps.stream()
                .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 1)
                .count();

        String newStatus = completedCount >= allSteps.size() ? "finish" : "doing";

        LambdaUpdateWrapper<LearningPath> pathWrapper = new LambdaUpdateWrapper<>();
        pathWrapper.eq(LearningPath::getId, path.getId())
                .set(LearningPath::getCurrentStep, (int) completedCount)
                .set(LearningPath::getStatus, newStatus);
        pathMapper.update(null, pathWrapper);

        LearningBehavior behavior = new LearningBehavior();
        behavior.setUserId(userId);
        behavior.setStepId(stepId);
        behavior.setBehaviorType("完成");
        behavior.setDuration(vo.getDuration());
        behavior.setScore(vo.getScore());
        behavior.setBehaviorTime(LocalDateTime.now());
        behaviorMapper.insert(behavior);

        return Result.success(null);
    }

    /**
     * 获取智能资源推荐
     * 推荐策略：
     * 1. 优先推荐路径步骤关联的资源（resourceIds字段）
     * 2. 其次按用户画像偏好筛选资源（resource_preference）
     * 3. 兜底推荐用户最近创建的资源
     */
    @Override
    public Result<?> getRecommendedResources(Long pathId, Long userId) {
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(404, "学习路径不存在");
        }

        // 1. 收集路径步骤中关联的资源ID
        List<LearningPathStep> steps = stepMapper.selectByPathId(pathId);
        Set<Long> linkedResourceIds = new LinkedHashSet<>();
        for (LearningPathStep step : steps) {
            if (step.getResourceIds() != null && !step.getResourceIds().isEmpty()) {
                String[] ids = step.getResourceIds().split(",");
                for (String idStr : ids) {
                    try {
                        linkedResourceIds.add(Long.parseLong(idStr.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // 2. 查询资源：优先关联的，没有则查用户所有资源
        List<LearningResource> resources;
        if (!linkedResourceIds.isEmpty()) {
            LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(LearningResource::getId, linkedResourceIds);
            resources = resourceMapper.selectList(wrapper);
        } else {
            // 查用户所有资源，按时间倒序
            LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LearningResource::getUserId, userId)
                    .orderByDesc(LearningResource::getCreateTime)
                    .last("LIMIT 10");
            resources = resourceMapper.selectList(wrapper);
        }

        // 3. 构建推荐VO，计算匹配度和推荐理由
        List<RecommendedResourceVO> voList = new ArrayList<>();
        for (LearningResource res : resources) {
            RecommendedResourceVO vo = new RecommendedResourceVO();
            vo.setId(res.getId());
            vo.setTitle(res.getResourceTitle());
            vo.setType(mapResourceType(res.getResourceType()));
            vo.setMatchScore(calculateMatchScore(res, path, linkedResourceIds.contains(res.getId())));
            vo.setReason(generateRecommendReason(res, path, linkedResourceIds.contains(res.getId())));
            vo.setPathName(path.getPathName());
            voList.add(vo);
        }

        // 如果资源不足5条，从用户的其他资源中补充
        if (voList.size() < 5) {
            Set<Long> existingIds = resources.stream().map(LearningResource::getId).collect(java.util.stream.Collectors.toSet());
            LambdaQueryWrapper<LearningResource> extraWrapper = new LambdaQueryWrapper<>();
            extraWrapper.eq(LearningResource::getUserId, userId)
                    .notIn(!existingIds.isEmpty(), LearningResource::getId, existingIds)
                    .orderByDesc(LearningResource::getCreateTime)
                    .last("LIMIT " + (5 - voList.size()));
            List<LearningResource> extraResources = resourceMapper.selectList(extraWrapper);
            for (LearningResource res : extraResources) {
                RecommendedResourceVO vo = new RecommendedResourceVO();
                vo.setId(res.getId());
                vo.setTitle(res.getResourceTitle());
                vo.setType(mapResourceType(res.getResourceType()));
                vo.setMatchScore(65); // 基础匹配度
                vo.setReason("根据你的学习路径推荐");
                vo.setPathName(path.getPathName());
                voList.add(vo);
            }
        }

        // 按匹配度降序排列
        voList.sort((a, b) -> b.getMatchScore() - a.getMatchScore());
        return Result.success(voList);
    }

    private String mapResourceType(String resourceType) {
        if (resourceType == null) return "document";
        return switch (resourceType) {
            case "mind" -> "mindmap";
            case "question" -> "quiz";
            case "case" -> "practice";
            case "video" -> "document";
            default -> resourceType;
        };
    }

    private int calculateMatchScore(LearningResource resource, LearningPath path, boolean isLinked) {
        int score = 60;
        // 关联了路径步骤的资源加分
        if (isLinked) score += 25;
        // 有知识点标签加分
        if (resource.getKnowledgePoint() != null) score += 8;
        // 有难度标签加分
        if (resource.getDifficulty() != null) score += 5;
        // 微调
        score += new Random().nextInt(6);
        return Math.min(score, 99);
    }

    private String generateRecommendReason(LearningResource resource, LearningPath path, boolean isLinked) {
        if (isLinked) {
            return "该资源与" + path.getPathName() + "的步骤直接关联";
        }
        if (resource.getKnowledgePoint() != null) {
            return "与当前学习步骤知识点「" + resource.getKnowledgePoint() + "」匹配";
        }
        return "根据「" + path.getPathName() + "」学习路径推荐";
    }

    /**
     * 动态调整学习路径
     * 前端params字段为JSON字符串，不同调整类型包含不同参数：
     * - extend/compress: { duration: "7天" }
     * - reorder: { steps: [{ id, sort }] }
     * - add: { contentName, contentDetail }
     * - remove: { removeStepIds: [id1, id2] }
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> adjustPath(Long pathId, Long userId, AdjustPathVO vo) {
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(404, "学习路径不存在");
        }

        log.info("调整学习路径, pathId={}, type={}, reason={}, params={}",
                pathId, vo.getAdjustmentType(), vo.getReason(), vo.getParams());

        JSONObject params = null;
        if (vo.getParams() != null && !vo.getParams().isEmpty()) {
            try {
                params = JSONObject.parseObject(vo.getParams());
            } catch (Exception e) {
                log.error("路径调整参数JSON解析失败, pathId={}, params={}", pathId, vo.getParams(), e);
                return Result.fail(400, "调整参数格式错误，请检查参数");
            }
        }

        try {
            switch (vo.getAdjustmentType()) {
                case "extend" -> adjustExtend(pathId, path, params, vo.getReason());
                case "compress" -> adjustCompress(pathId, path, params, vo.getReason());
                case "reorder" -> adjustReorder(pathId, params);
                case "add" -> adjustAdd(pathId, path, params, vo.getReason());
                case "remove" -> adjustRemove(pathId, params);
                default -> {
                    // 兜底：用AI重新规划
                    adjustWithAI(pathId, path, vo);
                }
            }
        } catch (RuntimeException e) {
            log.error("路径调整业务异常, pathId={}, type={}: {}", pathId, vo.getAdjustmentType(), e.getMessage(), e);
            return Result.fail(500, e.getMessage());
        } catch (Exception e) {
            log.error("路径调整未知异常, pathId={}, type={}", pathId, vo.getAdjustmentType(), e);
            return Result.fail(500, "路径调整失败: " + e.getMessage());
        }

        return getPathDetail(pathId, userId);
    }

    /**
     * 延长周期：用AI根据指定时长重新规划未完成步骤
     */
    private void adjustExtend(Long pathId, LearningPath path, JSONObject params, String reason) {
        String duration = params != null ? params.getString("duration") : "7天";
        List<LearningPathStep> currentSteps = stepMapper.selectByPathId(pathId);

        String prompt = buildDurationPrompt(path, currentSteps, "延长", duration, reason);
        regenerateUnfinishedSteps(pathId, currentSteps, prompt);
    }

    /**
     * 压缩周期：用AI根据指定时长重新规划未完成步骤
     */
    private void adjustCompress(Long pathId, LearningPath path, JSONObject params, String reason) {
        String duration = params != null ? params.getString("duration") : "7天";
        List<LearningPathStep> currentSteps = stepMapper.selectByPathId(pathId);

        String prompt = buildDurationPrompt(path, currentSteps, "压缩", duration, reason);
        regenerateUnfinishedSteps(pathId, currentSteps, prompt);
    }

    /**
     * 调整顺序：直接更新步骤的sort字段
     */
    private void adjustReorder(Long pathId, JSONObject params) {
        if (params == null || !params.containsKey("steps")) {
            throw new RuntimeException("缺少排序参数");
        }

        JSONArray stepsArr = params.getJSONArray("steps");
        for (int i = 0; i < stepsArr.size(); i++) {
            JSONObject stepObj = stepsArr.getJSONObject(i);
            Long stepId = stepObj.getLong("id");
            Integer sort = stepObj.getInteger("sort");

            LambdaUpdateWrapper<LearningPathStep> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(LearningPathStep::getId, stepId)
                    .eq(LearningPathStep::getPathId, pathId)
                    .set(LearningPathStep::getSort, sort);
            stepMapper.update(null, wrapper);
        }
    }

    /**
     * 新增内容：用AI生成新的学习步骤，插入到未完成步骤中
     */
    private void adjustAdd(Long pathId, LearningPath path, JSONObject params, String reason) {
        String contentName = params != null ? params.getString("contentName") : "";
        String contentDetail = params != null ? params.getString("contentDetail") : "";

        if (contentName.isEmpty()) {
            throw new RuntimeException("缺少新增内容名称");
        }

        List<LearningPathStep> currentSteps = stepMapper.selectByPathId(pathId);
        long completedCount = currentSteps.stream()
                .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 1).count();

        // 用AI生成新增内容对应的学习步骤
        String prompt = """
                请为学习路径"%s"新增以下知识点的学习步骤，严格返回JSON数组格式。

                新增知识点：%s
                详细描述：%s

                要求：
                1. 生成2-3个相关学习步骤
                2. 每个步骤包含stepName和stepContent字段
                3. stepContent要具体可执行
                4. 只返回JSON数组
                """.formatted(path.getPathName(), contentName, contentDetail);

        try {
            String reply = chatClient.prompt().user(prompt).call().content();
            List<LearningPathStep> newSteps = parseStepsFromAI(reply);

            if (!newSteps.isEmpty()) {
                int sort = (int) completedCount + currentSteps.size() - (int) completedCount + 1;
                // 未完成步骤的起始sort
                int nextSort = currentSteps.stream()
                        .filter(s -> s.getFinishStatus() == null || s.getFinishStatus() == 0)
                        .mapToInt(LearningPathStep::getSort)
                        .max().orElse((int) completedCount) + 1;

                for (LearningPathStep step : newSteps) {
                    step.setPathId(pathId);
                    step.setSort(nextSort++);
                    step.setFinishStatus(0);
                    stepMapper.insert(step);
                }

                // 更新总步骤数
                LambdaUpdateWrapper<LearningPath> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(LearningPath::getId, pathId)
                        .set(LearningPath::getTotalStep, path.getTotalStep() + newSteps.size());
                pathMapper.update(null, wrapper);
            }
        } catch (Exception e) {
            log.warn("AI生成新增步骤失败: {}", e.getMessage());
            // 降级：直接创建一个步骤
            LearningPathStep step = new LearningPathStep();
            step.setPathId(pathId);
            step.setStepName(contentName);
            step.setStepContent(contentDetail.isEmpty() ? "学习" + contentName : contentDetail);
            step.setSort(currentSteps.size() + 1);
            step.setFinishStatus(0);
            stepMapper.insert(step);

            LambdaUpdateWrapper<LearningPath> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(LearningPath::getId, pathId)
                    .set(LearningPath::getTotalStep, path.getTotalStep() + 1);
            pathMapper.update(null, wrapper);
        }
    }

    /**
     * 简化内容：软删除选中的未完成步骤
     */
    private void adjustRemove(Long pathId, JSONObject params) {
        if (params == null || !params.containsKey("removeStepIds")) {
            throw new RuntimeException("缺少要移除的步骤ID");
        }

        JSONArray removeIds = params.getJSONArray("removeStepIds");
        for (int i = 0; i < removeIds.size(); i++) {
            Long stepId = removeIds.getLong(i);
            LearningPathStep step = stepMapper.selectById(stepId);
            // 只允许删除未完成的步骤
            if (step != null && step.getPathId().equals(pathId)
                    && (step.getFinishStatus() == null || step.getFinishStatus() == 0)) {
                stepMapper.deleteById(stepId);
            }
        }

        // 重新编号剩余步骤的sort
        List<LearningPathStep> remaining = stepMapper.selectByPathId(pathId);
        for (int i = 0; i < remaining.size(); i++) {
            LearningPathStep step = remaining.get(i);
            if (step.getSort() != i + 1) {
                LambdaUpdateWrapper<LearningPathStep> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(LearningPathStep::getId, step.getId())
                        .set(LearningPathStep::getSort, i + 1);
                stepMapper.update(null, wrapper);
            }
        }

        // 更新总步骤数
        LambdaUpdateWrapper<LearningPath> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(LearningPath::getId, pathId)
                .set(LearningPath::getTotalStep, remaining.size());
        pathMapper.update(null, wrapper);
    }

    /**
     * 兜底：用AI整体重新规划（兼容旧逻辑）
     */
    private void adjustWithAI(Long pathId, LearningPath path, AdjustPathVO vo) {
        List<LearningPathStep> currentSteps = stepMapper.selectByPathId(pathId);
        String prompt = buildGeneralAdjustPrompt(path, currentSteps, vo);
        regenerateUnfinishedSteps(pathId, currentSteps, prompt);
    }

    /**
     * 用AI重新生成未完成步骤的通用逻辑
     */
    private void regenerateUnfinishedSteps(Long pathId, List<LearningPathStep> currentSteps, String aiPrompt) {
        String reply;
        try {
            reply = chatClient.prompt().user(aiPrompt).call().content();
        } catch (Exception e) {
            log.error("AI服务调用失败, pathId={}: {}", pathId, e.getMessage(), e);
            throw new RuntimeException("AI服务调用失败，请确认AI服务已正确配置: " + e.getMessage(), e);
        }
        List<LearningPathStep> newSteps = parseStepsFromAI(reply);

        if (!newSteps.isEmpty()) {
            List<LearningPathStep> completedSteps = currentSteps.stream()
                    .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 1)
                    .toList();

            // 软删除未完成的步骤
            for (LearningPathStep step : currentSteps) {
                if (step.getFinishStatus() == null || step.getFinishStatus() != 1) {
                    stepMapper.deleteById(step.getId());
                }
            }

            // 插入新步骤
            int sort = completedSteps.size() + 1;
            for (LearningPathStep step : newSteps) {
                step.setPathId(pathId);
                step.setSort(sort++);
                step.setFinishStatus(0);
                stepMapper.insert(step);
            }

            // 更新路径
            int totalSteps = completedSteps.size() + newSteps.size();
            LambdaUpdateWrapper<LearningPath> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(LearningPath::getId, pathId)
                    .set(LearningPath::getTotalStep, totalSteps)
                    .set(LearningPath::getStatus, "doing");
            pathMapper.update(null, wrapper);
        }
    }

    /**
     * 构建延长/压缩周期的AI提示词
     */
    private String buildDurationPrompt(LearningPath path, List<LearningPathStep> steps,
                                        String action, String duration, String reason) {
        StringBuilder stepsStr = new StringBuilder();
        for (LearningPathStep step : steps) {
            String status = step.getFinishStatus() != null && step.getFinishStatus() == 1 ? "已完成" : "未完成";
            stepsStr.append("- ").append(step.getStepName()).append(" (").append(status).append("): ")
                    .append(step.getStepContent()).append("\n");
        }

        return """
                请根据以下需求，重新规划学习路径中未完成的步骤，严格返回JSON数组格式。

                路径名称：%s
                当前步骤：
                %s

                调整需求：%s周期 %s
                原因：%s

                要求：
                1. 只重新规划"未完成"的步骤，已完成的步骤保持不变
                2. %s周期%s，需要%s每个步骤的学习内容
                3. 每个步骤包含stepName和stepContent字段
                4. 步骤数量4-8个
                5. 只返回JSON数组
                """.formatted(path.getPathName(), stepsStr, action, duration,
                reason != null ? reason : "无",
                action, duration,
                "延长".equals(action) ? "分散/细化" : "合并/精简");
    }

    /**
     * 构建通用调整的AI提示词（兜底）
     */
    private String buildGeneralAdjustPrompt(LearningPath path, List<LearningPathStep> steps, AdjustPathVO vo) {
        StringBuilder stepsStr = new StringBuilder();
        for (LearningPathStep step : steps) {
            String status = step.getFinishStatus() != null && step.getFinishStatus() == 1 ? "已完成" : "未完成";
            stepsStr.append("- ").append(step.getStepName()).append(" (").append(status).append("): ")
                    .append(step.getStepContent()).append("\n");
        }

        return """
                请根据以下调整需求，重新规划学习路径中未完成的步骤，严格返回JSON数组格式。

                路径名称：%s
                当前步骤：
                %s

                调整类型：%s
                调整原因：%s

                要求：
                1. 只重新规划"未完成"的步骤，已完成的步骤保持不变
                2. 每个步骤包含stepName和stepContent字段
                3. 步骤数量4-8个
                4. 只返回JSON数组
                """.formatted(path.getPathName(), stepsStr, vo.getAdjustmentType(), vo.getReason());
    }

    /**
     * 删除学习路径
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> deletePath(Long pathId, Long userId) {
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(404, "学习路径不存在");
        }

        pathMapper.deleteById(pathId);

        LambdaQueryWrapper<LearningPathStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPathStep::getPathId, pathId);
        List<LearningPathStep> steps = stepMapper.selectList(wrapper);
        for (LearningPathStep step : steps) {
            stepMapper.deleteById(step.getId());
        }

        return Result.success(null);
    }

    /**
     * 记录学习行为
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> recordBehavior(Long userId, RecordBehaviorVO vo) {
        LearningBehavior behavior = new LearningBehavior();
        behavior.setUserId(userId);
        behavior.setResourceId(vo.getResourceId());
        behavior.setStepId(vo.getStepId());
        behavior.setBehaviorType(vo.getBehaviorType());
        behavior.setDuration(vo.getDuration());
        behavior.setBehaviorTime(LocalDateTime.now());
        behaviorMapper.insert(behavior);

        return Result.success(null);
    }
}
