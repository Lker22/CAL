package com.education.path.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.ai.mapper.LearningResourceMapper;
import com.education.context.BaseContext;
import com.education.entity.LearningBehavior;
import com.education.entity.LearningPath;
import com.education.entity.LearningPathStep;
import com.education.entity.LearningResource;
import com.education.path.mapper.LearningBehaviorMapper;
import com.education.path.mapper.LearningPathMapper;
import com.education.path.mapper.LearningPathStepMapper;
import com.education.path.service.LearningPathService;
import com.education.path.vo.*;
import com.education.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 学习路径Service实现
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

        // 2. 创建学习路径
        LearningPath path = new LearningPath();
        path.setUserId(userId);
        path.setPathName(vo.getSubject());
        path.setTotalStep(steps.size());
        path.setCurrentStep(0);
        path.setStatus("pending");
        pathMapper.insert(path);

        // 3. 批量保存步骤
        for (int i = 0; i < steps.size(); i++) {
            LearningPathStep step = steps.get(i);
            step.setPathId(path.getId());
            step.setSort(i + 1);
            step.setFinishStatus(0); // 待学习
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

            // 解析AI返回的JSON数组
            return parseStepsFromAI(reply);
        } catch (Exception e) {
            log.warn("AI生成步骤失败，使用默认步骤: {}", e.getMessage());
            return generateDefaultSteps(vo.getSubject());
        }
    }

    /**
     * 解析AI返回的步骤JSON
     */
    private List<LearningPathStep> parseStepsFromAI(String aiReply) {
        List<LearningPathStep> steps = new ArrayList<>();
        try {
            // 尝试从回复中提取JSON数组
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

    /**
     * 生成默认步骤（AI不可用时的降级方案）
     */
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
            wrapper.eq(LearningPath::getStatus, status);
        }
        wrapper.orderByDesc(LearningPath::getCreateTime);

        Page<LearningPath> pathPage = pathMapper.selectPage(pageParam, wrapper);

        // 为每个路径计算进度
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
        vo.setStatus(path.getStatus());
        vo.setTotalStep(path.getTotalStep());
        vo.setCreateTime(path.getCreateTime());
        vo.setUpdateTime(path.getUpdateTime());

        // 查询步骤列表并转换为前端VO
        List<LearningPathStep> steps = stepMapper.selectByPathId(path.getId());
        List<LearningPathStepVO> stepVOs = convertToStepVOs(steps);
        vo.setSteps(stepVOs);

        // 计算进度
        long completedCount = steps.stream()
                .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 2)
                .count();
        vo.setCompletedSteps((int) completedCount);

        int progress = path.getTotalStep() > 0
                ? (int) (completedCount * 100 / path.getTotalStep())
                : 0;
        vo.setProgress(progress);

        return vo;
    }

    /**
     * 将步骤实体列表转换为前端VO列表
     */
    private List<LearningPathStepVO> convertToStepVOs(List<LearningPathStep> steps) {
        List<LearningPathStepVO> voList = new ArrayList<>();
        for (LearningPathStep step : steps) {
            LearningPathStepVO stepVO = new LearningPathStepVO();
            stepVO.setId(step.getId());
            stepVO.setPathId(step.getPathId());
            stepVO.setTitle(step.getStepName());
            stepVO.setDescription(step.getStepContent());
            stepVO.setSort(step.getSort());
            stepVO.setDuration("3-5天"); // 默认学习周期

            // 映射状态: 0->pending, 1->inProgress, 2->completed
            String status = switch (step.getFinishStatus() != null ? step.getFinishStatus() : 0) {
                case 1 -> "inProgress";
                case 2 -> "completed";
                default -> "pending";
            };
            stepVO.setStatus(status);

            // 格式化完成时间
            if (step.getFinishTime() != null) {
                stepVO.setCompletedAt(step.getFinishTime().toString().substring(0, 10));
            }

            // 解析关联资源名称
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

    /**
     * 根据资源ID列表解析资源名称
     */
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
        // 1. 查询步骤
        LearningPathStep step = stepMapper.selectById(stepId);
        if (step == null) {
            return Result.fail(404, "学习步骤不存在");
        }

        // 2. 验证路径归属
        LearningPath path = pathMapper.selectById(step.getPathId());
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(403, "无权操作此步骤");
        }

        // 3. 更新步骤状态
        LambdaUpdateWrapper<LearningPathStep> stepWrapper = new LambdaUpdateWrapper<>();
        stepWrapper.eq(LearningPathStep::getId, stepId)
                .set(LearningPathStep::getFinishStatus, 2) // 已完成
                .set(LearningPathStep::getFinishTime, LocalDateTime.now());
        stepMapper.update(null, stepWrapper);

        // 4. 更新路径进度
        List<LearningPathStep> allSteps = stepMapper.selectByPathId(path.getId());
        long completedCount = allSteps.stream()
                .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 2)
                .count();

        String newStatus = completedCount >= allSteps.size() ? "completed" : "inProgress";

        LambdaUpdateWrapper<LearningPath> pathWrapper = new LambdaUpdateWrapper<>();
        pathWrapper.eq(LearningPath::getId, path.getId())
                .set(LearningPath::getCurrentStep, (int) completedCount)
                .set(LearningPath::getStatus, newStatus);
        pathMapper.update(null, pathWrapper);

        // 5. 如果有下一个步骤，将其标记为进行中
        int nextSort = step.getSort() + 1;
        if (nextSort <= allSteps.size()) {
            LambdaUpdateWrapper<LearningPathStep> nextWrapper = new LambdaUpdateWrapper<>();
            nextWrapper.eq(LearningPathStep::getPathId, path.getId())
                    .eq(LearningPathStep::getSort, nextSort)
                    .eq(LearningPathStep::getFinishStatus, 0)
                    .set(LearningPathStep::getFinishStatus, 1); // 学习中
            stepMapper.update(null, nextWrapper);
        }

        // 6. 记录学习行为
        LearningBehavior behavior = new LearningBehavior();
        behavior.setUserId(userId);
        behavior.setStepId(stepId);
        behavior.setBehaviorType("complete");
        behavior.setDuration(vo.getDuration());
        behavior.setScore(vo.getScore());
        behavior.setBehaviorTime(LocalDateTime.now());
        behaviorMapper.insert(behavior);

        return Result.success(null);
    }

    /**
     * 获取智能资源推荐
     */
    @Override
    public Result<?> getRecommendedResources(Long pathId, Long userId) {
        // 1. 验证路径归属
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(404, "学习路径不存在");
        }

        // 2. 查询路径下所有步骤关联的资源ID
        List<LearningPathStep> steps = stepMapper.selectByPathId(pathId);
        Set<Long> resourceIds = new LinkedHashSet<>();
        for (LearningPathStep step : steps) {
            if (step.getResourceIds() != null && !step.getResourceIds().isEmpty()) {
                String[] ids = step.getResourceIds().split(",");
                for (String idStr : ids) {
                    try {
                        resourceIds.add(Long.parseLong(idStr.trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        // 3. 查询用户的所有学习资源，作为推荐池
        List<LearningResource> resources;
        if (!resourceIds.isEmpty()) {
            // 优先推荐关联的资源
            LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(LearningResource::getId, resourceIds);
            resources = resourceMapper.selectList(wrapper);
        } else {
            // 未关联资源时，推荐用户的所有资源
            LambdaQueryWrapper<LearningResource> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(LearningResource::getUserId, userId)
                    .orderByDesc(LearningResource::getCreateTime)
                    .last("LIMIT 10");
            resources = resourceMapper.selectList(wrapper);
        }

        // 4. 构建推荐VO列表
        List<RecommendedResourceVO> voList = new ArrayList<>();
        for (LearningResource res : resources) {
            RecommendedResourceVO vo = new RecommendedResourceVO();
            vo.setId(res.getId());
            vo.setTitle(res.getResourceTitle());
            vo.setType(mapResourceType(res.getResourceType()));
            vo.setMatchScore(calculateMatchScore(res, path));
            vo.setReason(generateRecommendReason(res, path));
            vo.setPathName(path.getPathName());
            voList.add(vo);
        }

        // 按匹配度降序排列
        voList.sort((a, b) -> b.getMatchScore() - a.getMatchScore());

        return Result.success(voList);
    }

    /**
     * 映射资源类型到前端类型
     */
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

    /**
     * 计算资源匹配度
     */
    private int calculateMatchScore(LearningResource resource, LearningPath path) {
        int score = 70; // 基础分
        // 关联了路径步骤的资源加分
        if (resource.getKnowledgePoint() != null) {
            score += 10;
        }
        // 有难度标签的资源加分
        if (resource.getDifficulty() != null) {
            score += 5;
        }
        // 随机波动(模拟AI匹配度)
        score += new Random().nextInt(16);
        return Math.min(score, 99);
    }

    /**
     * 生成推荐理由
     */
    private String generateRecommendReason(LearningResource resource, LearningPath path) {
        if (resource.getKnowledgePoint() != null) {
            return "与当前学习步骤知识点匹配";
        }
        return "根据" + path.getPathName() + "学习路径推荐";
    }

    /**
     * 动态调整学习路径
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> adjustPath(Long pathId, Long userId, AdjustPathVO vo) {
        // 1. 验证路径归属
        LearningPath path = pathMapper.selectById(pathId);
        if (path == null || !path.getUserId().equals(userId)) {
            return Result.fail(404, "学习路径不存在");
        }

        log.info("调整学习路径, pathId={}, type={}, reason={}", pathId, vo.getAdjustmentType(), vo.getReason());

        // 2. 获取当前步骤
        List<LearningPathStep> currentSteps = stepMapper.selectByPathId(pathId);

        // 3. 根据调整类型调用AI重新规划
        try {
            String adjustPrompt = buildAdjustPrompt(path, currentSteps, vo);

            String reply = chatClient.prompt()
                    .user(adjustPrompt)
                    .call()
                    .content();

            // 解析AI返回的新步骤
            List<LearningPathStep> newSteps = parseStepsFromAI(reply);

            if (!newSteps.isEmpty()) {
                // 保留已完成的步骤，替换未完成的步骤
                List<LearningPathStep> completedSteps = currentSteps.stream()
                        .filter(s -> s.getFinishStatus() != null && s.getFinishStatus() == 2)
                        .toList();

                // 软删除所有未完成的步骤
                for (LearningPathStep step : currentSteps) {
                    if (step.getFinishStatus() == null || step.getFinishStatus() != 2) {
                        stepMapper.deleteById(step.getId());
                    }
                }

                // 添加新步骤
                int sort = completedSteps.size() + 1;
                for (LearningPathStep step : newSteps) {
                    step.setPathId(pathId);
                    step.setSort(sort++);
                    step.setFinishStatus(0);
                    stepMapper.insert(step);
                }

                // 更新路径总步骤数
                int totalSteps = completedSteps.size() + newSteps.size();
                LambdaUpdateWrapper<LearningPath> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(LearningPath::getId, pathId)
                        .set(LearningPath::getTotalStep, totalSteps)
                        .set(LearningPath::getStatus, "inProgress");
                pathMapper.update(null, wrapper);
            }
        } catch (Exception e) {
            log.warn("AI调整路径失败: {}", e.getMessage());
            return Result.fail(500, "路径调整失败，请稍后重试");
        }

        // 4. 返回调整后的路径详情
        return getPathDetail(pathId, userId);
    }

    /**
     * 构建调整路径的AI提示词
     */
    private String buildAdjustPrompt(LearningPath path, List<LearningPathStep> steps, AdjustPathVO vo) {
        StringBuilder stepsStr = new StringBuilder();
        for (LearningPathStep step : steps) {
            String status = step.getFinishStatus() == 2 ? "已完成" : (step.getFinishStatus() == 1 ? "进行中" : "待学习");
            stepsStr.append("- ").append(step.getStepName()).append(" (").append(status).append("): ")
                    .append(step.getStepContent()).append("\n");
        }

        String adjustDesc = switch (vo.getAdjustmentType()) {
            case "extend" -> "延长学习周期，降低每个步骤的学习密度";
            case "compress" -> "压缩学习周期，提高学习密度和节奏";
            case "reorder" -> "调整学习步骤的先后顺序，优化学习路径";
            case "add" -> "在合适的位置新增知识点和学习步骤";
            case "remove" -> "精简内容，移除不必要的步骤";
            default -> "优化学习路径";
        };

        return """
                请根据以下调整需求，重新规划学习路径中未完成的步骤，严格返回JSON数组格式。

                路径名称：%s
                当前步骤：
                %s

                调整类型：%s
                调整原因：%s

                要求：
                1. 只重新规划"待学习"和"进行中"的步骤，已完成的步骤保持不变
                2. %s
                3. 每个步骤包含stepName和stepContent字段
                4. 步骤数量4-8个
                5. 只返回JSON数组
                """.formatted(path.getPathName(), stepsStr, vo.getAdjustmentType(), vo.getReason(), adjustDesc);
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

        // 软删除路径
        pathMapper.deleteById(pathId);

        // 软删除关联步骤
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
