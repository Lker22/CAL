package com.education.outcome.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.context.BaseContext;
import com.education.entity.LearningBehavior;
import com.education.entity.LearningEvaluate;
import com.education.entity.LearningPath;
import com.education.entity.QuestionAnswerRecord;
import com.education.entity.StudentProfile;
import com.education.outcome.agent.AssessmentAIAgent;
import com.education.outcome.mapper.LearningEvaluateMapper;
import com.education.outcome.service.LearningBehaviorService;
import com.education.outcome.service.LearningEvaluateService;
import com.education.outcome.service.LearningPathStepService;
import com.education.outcome.service.QuestionAnswerRecordService;
import com.education.path.mapper.LearningPathMapper;
import com.education.resource.mapper.LearningResourceMapper;
import com.education.user.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningEvaluateServiceImpl extends ServiceImpl<LearningEvaluateMapper, LearningEvaluate>
        implements LearningEvaluateService {

    private final StudentProfileService studentProfileService;
    private final LearningBehaviorService learningBehaviorService;
    private final QuestionAnswerRecordService questionAnswerRecordService;
    private final LearningPathStepService learningPathStepService;
    private final LearningPathMapper learningPathMapper;
    private final LearningResourceMapper learningResourceMapper;
    private final AssessmentAIAgent assessmentAIAgent;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    //拼装学习数据 → 调用 AI → 解析结果 → 保存数据库 → 同步画像
    /**
     * 获取评估报告（按日期范围查询）
     */
    @Override
    public List<LearningEvaluate> getReport(Long userId, String startDate, String endDate) {
        LambdaQueryWrapper<LearningEvaluate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningEvaluate::getUserId, userId);

        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDate + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.ge(LearningEvaluate::getCreateTime, start);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(endDate + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            wrapper.le(LearningEvaluate::getCreateTime, end);
        }

        wrapper.orderByDesc(LearningEvaluate::getCreateTime);
        return list(wrapper);
    }

    /**
     * 获取最新一条评估报告
     */
    @Override
    public LearningEvaluate getLatestReport(Long userId) {
        LambdaQueryWrapper<LearningEvaluate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningEvaluate::getUserId, userId)
                .orderByDesc(LearningEvaluate::getCreateTime)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    /**
     * AI生成评估报告
     */
    @Override
    public LearningEvaluate generateReport(Long pathId, List<String> includeModules) {
        Long userId = BaseContext.getCurrentId();

        // 1. 获取学生画像
        StudentProfile profile = studentProfileService.getByUserId();
        String profileText = formatProfile(profile);

        // 2. 获取学习行为数据
        List<LearningBehavior> behaviors = learningBehaviorService.getByUserId(userId);
        String behaviorText = formatBehaviors(behaviors);

        // 3. 获取答题记录
        List<QuestionAnswerRecord> answers = questionAnswerRecordService.getByUserId(userId);
        String answerText = formatAnswers(answers);

        // 4. 学习路径完成情况
        String pathInfo = null;
        if (pathId != null) {
            int completed = learningPathStepService.countCompleted(pathId);
            int total = learningPathStepService.countByPath(pathId);
            pathInfo = "路径ID:" + pathId + "，已完成" + completed + "/" + total + "个步骤";
        }

        // 5. 调用AI评估智能体
        String aiResponse = assessmentAIAgent.generateEvaluation(
                profileText, behaviorText, answerText, pathInfo, includeModules);

        // 6. 解析AI响应，构建评估记录
        LearningEvaluate evaluate = new LearningEvaluate();
        evaluate.setUserId(userId);
        evaluate.setEvaluateContent(aiResponse);
        evaluate.setImproveSuggest(assessmentAIAgent.extractImproveSuggest(aiResponse));
        evaluate.setKnowledgeMastery(assessmentAIAgent.parseKnowledgeMastery(aiResponse));
        evaluate.setStartTime(LocalDateTime.now().minusDays(7));
        evaluate.setEndTime(LocalDateTime.now());
        evaluate.setDeleted(0);

        // 7. 保存数据库
        this.save(evaluate);

        // 8. 同步评估结果到学习画像（更新场景标记）
        try {
            syncProfileFromEvaluation(userId, evaluate);
        } catch (Exception e) {
            log.warn("同步评估结果到画像失败，userId={}", userId, e);
            // 不中断主流程
        }

        return evaluate;
    }

    /**
     * 获取学习统计数据
     */
    @Override
    public Map<String, Object> getStats(Long userId, String startDate, String endDate, String type) {
        Map<String, Object> stats = new HashMap<>();

        // 解析日期范围
        LocalDateTime start = parseStartDate(startDate);
        LocalDateTime end = parseEndDate(endDate);

        // 查询行为数据
        LambdaQueryWrapper<LearningBehavior> behaviorWrapper = new LambdaQueryWrapper<>();
        behaviorWrapper.eq(LearningBehavior::getUserId, userId)
                .ge(start != null, LearningBehavior::getBehaviorTime, start)
                .le(end != null, LearningBehavior::getBehaviorTime, end);
        List<LearningBehavior> behaviors = learningBehaviorService.list(behaviorWrapper);

        // 查询答题数据
        LambdaQueryWrapper<QuestionAnswerRecord> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(QuestionAnswerRecord::getUserId, userId)
                .ge(start != null, QuestionAnswerRecord::getAnswerTime, start)
                .le(end != null, QuestionAnswerRecord::getAnswerTime, end);
        List<QuestionAnswerRecord> answers = questionAnswerRecordService.list(answerWrapper);

        // ========== 前端 LearningStatsView 期望的字段 ==========
        // 总学习时长（分钟）
        long totalMinutes = behaviors.stream()
                .mapToLong(b -> b.getDuration() != null ? b.getDuration() : 0)
                .sum() / 60;
        stats.put("totalTime", totalMinutes);

        // 学习资源数（关联过的不同资源数）
        long resourceCount = behaviors.stream()
                .filter(b -> b.getResourceId() != null)
                .map(LearningBehavior::getResourceId)
                .distinct().count();
        stats.put("totalResources", resourceCount);

        // 答题数
        stats.put("totalQuiz", answers.size());

        // 正确率（0-100）
        long correctCount = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
        double accuracy = answers.isEmpty() ? 0 : (double) correctCount / answers.size() * 100;
        stats.put("avgScore", Math.round(accuracy));

        // 每日学习时长（近7天，按自然日排列）
        List<Map<String, Object>> weekData = new ArrayList<>();
        String[] weekDayNames = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        java.time.LocalDate today = java.time.LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate day = today.minusDays(i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = dayStart.plusDays(1);
            long dayMinutes = behaviors.stream()
                    .filter(b -> b.getBehaviorTime() != null
                            && !b.getBehaviorTime().isBefore(dayStart)
                            && b.getBehaviorTime().isBefore(dayEnd))
                    .mapToLong(b -> b.getDuration() != null ? b.getDuration() : 0)
                    .sum() / 60;
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("day", weekDayNames[day.getDayOfWeek().getValue() % 7]);
            dayMap.put("time", dayMinutes);
            weekData.add(dayMap);
        }
        stats.put("weekData", weekData);

        // 按行为类型统计
        Map<String, Long> behaviorByType = new HashMap<>();
        for (LearningBehavior b : behaviors) {
            String bt = b.getBehaviorType() != null ? b.getBehaviorType() : "学习";
            behaviorByType.merge(bt, 1L, Long::sum);
        }
        stats.put("behaviorByType", behaviorByType);

        // 资源类型分布（查 learning_resource 表按 resource_type 分组）
        Map<String, String> typeLabels = Map.of(
                "document", "文档", "mind", "思维导图", "question", "题库", "case", "实操案例"
        );
        LambdaQueryWrapper<com.education.entity.LearningResource> resWrapper = new LambdaQueryWrapper<>();
        resWrapper.eq(com.education.entity.LearningResource::getUserId, userId)
                .select(com.education.entity.LearningResource::getResourceType);
        List<com.education.entity.LearningResource> userResources = learningResourceMapper.selectList(resWrapper);
        Map<String, Long> typeCount = new LinkedHashMap<>();
        for (com.education.entity.LearningResource r : userResources) {
            String t = r.getResourceType() != null ? r.getResourceType() : "other";
            typeCount.merge(t, 1L, Long::sum);
        }
        long totalResources = Math.max(userResources.size(), 1);
        List<Map<String, Object>> resourceStats = new ArrayList<>();
        for (Map.Entry<String, Long> entry : typeCount.entrySet()) {
            Map<String, Object> rs = new HashMap<>();
            rs.put("type", typeLabels.getOrDefault(entry.getKey(), entry.getKey()));
            rs.put("count", entry.getValue());
            rs.put("percentage", Math.round((double) entry.getValue() / totalResources * 100));
            resourceStats.add(rs);
        }
        if (resourceStats.isEmpty()) {
            Map<String, Object> rs = new HashMap<>();
            rs.put("type", "暂无数据");
            rs.put("count", 0);
            rs.put("percentage", 0);
            resourceStats.add(rs);
        }
        stats.put("resourceStats", resourceStats);

        // 学科进度（查 learning_path 表，用真实路径名）
        LambdaQueryWrapper<LearningPath> pathWrapper = new LambdaQueryWrapper<>();
        pathWrapper.eq(LearningPath::getUserId, userId)
                .eq(LearningPath::getDeleted, 0)
                .orderByDesc(LearningPath::getCreateTime)
                .last("LIMIT 10");
        List<LearningPath> paths = learningPathMapper.selectList(pathWrapper);
        List<Map<String, Object>> subjectStats = new ArrayList<>();
        for (LearningPath p : paths) {
            int total = p.getTotalStep() != null ? p.getTotalStep() : 0;
            int current = p.getCurrentStep() != null ? p.getCurrentStep() : 0;
            int progress = total > 0 ? Math.min(100, Math.round((float) current / total * 100)) : 0;
            // 计算该路径的学习时长
            long pathMinutes = behaviors.stream()
                    .filter(b -> b.getStepId() != null)
                    .filter(b -> {
                        try {
                            var step = learningPathStepService.getById(b.getStepId());
                            return step != null && step.getPathId() != null && step.getPathId().equals(p.getId());
                        } catch (Exception e) { return false; }
                    })
                    .mapToLong(b -> b.getDuration() != null ? b.getDuration() / 60 : 0)
                    .sum();
            Map<String, Object> s = new HashMap<>();
            s.put("name", p.getPathName());
            s.put("progress", progress);
            s.put("time", pathMinutes);
            subjectStats.add(s);
        }
        if (subjectStats.isEmpty()) {
            Map<String, Object> s = new HashMap<>();
            s.put("name", "暂无学习路径");
            s.put("progress", 0);
            s.put("time", 0);
            subjectStats.add(s);
        }
        stats.put("subjectStats", subjectStats);

        // ========== 前端 AssessmentResultView 雷达图期望的字段 ==========
        // 没有答题数据时，用行为数据估算分数，避免全0
        int masteryBase = Math.min(100, (int) (totalMinutes / 2.0)); // 每2分钟=1分
        int consistencyBase = Math.min(100, behaviors.size() * 15); // 每次行为+15分
        stats.put("masteryScore", Math.max(masteryBase, answers.isEmpty() ? 0 : Math.round(accuracy)));
        stats.put("applicationScore", answers.isEmpty() ? masteryBase : Math.round(accuracy * 0.8));
        stats.put("progressScore", consistencyBase);
        stats.put("accuracyScore", Math.round(accuracy));
        stats.put("consistencyScore", consistencyBase);

        // ========== 原有字段（保留兼容） ==========
        stats.put("totalDurationMinutes", totalMinutes);
        stats.put("behaviorCount", behaviors.size());
        stats.put("answerCount", answers.size());
        stats.put("correctCount", correctCount);
        stats.put("accuracy", Math.round(accuracy * 10) / 10.0);
        double avgTime = answers.stream()
                .filter(a -> a.getSpendTime() != null)
                .mapToLong(QuestionAnswerRecord::getSpendTime)
                .average().orElse(0);
        stats.put("avgSpendTime", Math.round(avgTime));
        stats.put("startDate", startDate);
        stats.put("endDate", endDate);
        stats.put("type", type);

        return stats;
    }

    /**
     * 薄弱点分析
     */
    @Override
    public Map<String, Object> getWeakPoints(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 从学生画像获取易错点
        StudentProfile profile = studentProfileService.getByUserId();
        if (profile != null && profile.getErrorPronePoints() != null) {
            try {
                Object errorPoints = JSON.parse(profile.getErrorPronePoints());
                result.put("errorPronePoints", errorPoints);
            } catch (Exception e) {
                result.put("errorPronePoints", profile.getErrorPronePoints());
            }
        }

        // 2. 从最近的评估报告获取知识点掌握度
        LambdaQueryWrapper<LearningEvaluate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningEvaluate::getUserId, userId)
                .orderByDesc(LearningEvaluate::getCreateTime)
                .last("LIMIT 1");
        LearningEvaluate latestEval = getOne(wrapper);

        if (latestEval != null && latestEval.getKnowledgeMastery() != null) {
            try {
                JSONObject mastery = JSON.parseObject(latestEval.getKnowledgeMastery());
                // 找出掌握度低于60的知识点
                Map<String, Object> weakPoints = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : mastery.entrySet()) {
                    try {
                        double val = Double.parseDouble(entry.getValue().toString());
                        if (val < 60) {
                            Map<String, Object> detail = new HashMap<>();
                            detail.put("mastery", val);
                            detail.put("gap", 60 - val);
                            weakPoints.put(entry.getKey(), detail);
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                result.put("weakKnowledgePoints", weakPoints);
                result.put("allMastery", mastery);
            } catch (Exception e) {
                log.warn("解析知识点掌握度失败", e);
            }
        }

        // 3. 答题正确率最低的知识点（从答题记录分析）
        List<QuestionAnswerRecord> answers = questionAnswerRecordService.getByUserId(userId);
        long totalAnswers = answers.size();
        long wrongAnswers = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 0).count();
        result.put("totalAnswers", totalAnswers);
        result.put("wrongAnswers", wrongAnswers);
        result.put("wrongRate", totalAnswers > 0 ? Math.round((double) wrongAnswers / totalAnswers * 1000) / 10.0 : 0);

        // 4. 画像中的知识基础水平
        if (profile != null) {
            result.put("knowledgeBase", profile.getKnowledgeBase());
            result.put("learningPace", profile.getLearningPace());
        }

        return result;
    }

    /**
     * 学习趋势分析
     */
    @Override
    public List<Map<String, Object>> getTrend(Long userId, String period) {
        List<Map<String, Object>> trendList = new ArrayList<>();

        // 解析周期：7d, 30d, 90d
        int days = 7;
        if (period != null) {
            if (period.contains("30")) days = 30;
            else if (period.contains("90")) days = 90;
        }

        LocalDateTime now = LocalDateTime.now();

        // 按天聚合数据
        for (int i = days - 1; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.withHour(23).withMinute(59).withSecond(59);

            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dayStart.format(DATE_FORMAT));

            // 当天学习时长
            LambdaQueryWrapper<LearningBehavior> bw = new LambdaQueryWrapper<>();
            bw.eq(LearningBehavior::getUserId, userId)
                    .ge(LearningBehavior::getBehaviorTime, dayStart)
                    .le(LearningBehavior::getBehaviorTime, dayEnd);
            List<LearningBehavior> dayBehaviors = learningBehaviorService.list(bw);
            long dayDuration = dayBehaviors.stream()
                    .mapToLong(b -> b.getDuration() != null ? b.getDuration() : 0)
                    .sum();
            dayData.put("durationMinutes", dayDuration / 60);
            dayData.put("behaviorCount", dayBehaviors.size());

            // 当天答题情况
            LambdaQueryWrapper<QuestionAnswerRecord> aw = new LambdaQueryWrapper<>();
            aw.eq(QuestionAnswerRecord::getUserId, userId)
                    .ge(QuestionAnswerRecord::getAnswerTime, dayStart)
                    .le(QuestionAnswerRecord::getAnswerTime, dayEnd);
            List<QuestionAnswerRecord> dayAnswers = questionAnswerRecordService.list(aw);
            dayData.put("answerCount", dayAnswers.size());
            long dayCorrect = dayAnswers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
            dayData.put("correctCount", dayCorrect);

            trendList.add(dayData);
        }

        return trendList;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将评估结果同步到学习画像
     */
    private void syncProfileFromEvaluation(Long userId, LearningEvaluate evaluate) {
        StudentProfile profile = studentProfileService.getByUserId();
        if (profile == null) {
            profile = new StudentProfile();
        }
        // 无论新建还是更新，都必须设置userId
        profile.setUserId(userId);
        // 标记更新场景为"评估触发"
        profile.setUpdateScene("评估触发");

        // 如果知识点掌握度中有薄弱知识点，更新易错点
        if (evaluate.getKnowledgeMastery() != null) {
            try {
                JSONObject mastery = JSON.parseObject(evaluate.getKnowledgeMastery());
                List<String> weakList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : mastery.entrySet()) {
                    try {
                        double val = Double.parseDouble(entry.getValue().toString());
                        if (val < 60) {
                            weakList.add(entry.getKey());
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (!weakList.isEmpty()) {
                    profile.setErrorPronePoints(JSON.toJSONString(weakList));
                }
            } catch (Exception e) {
                log.warn("解析知识点掌握度失败，跳过易错点更新", e);
            }
        }

        studentProfileService.saveOrUpdateProfile(profile);
    }

    /**
     * 格式化学生画像为可读文本
     */
    private String formatProfile(StudentProfile profile) {
        if (profile == null) {
            return "暂无画像数据";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("知识基础: ").append(nullToDefault(profile.getKnowledgeBase(), "未知")).append("\n");
        sb.append("认知风格: ").append(nullToDefault(profile.getCognitiveStyle(), "未知")).append("\n");
        sb.append("学习目标: ").append(nullToDefault(profile.getLearningGoal(), "未知")).append("\n");
        sb.append("学习节奏: ").append(nullToDefault(profile.getLearningPace(), "未知")).append("\n");
        sb.append("资源偏好: ").append(nullToDefault(profile.getResourcePreference(), "未知")).append("\n");
        sb.append("易错点: ").append(nullToDefault(profile.getErrorPronePoints(), "无")).append("\n");
        sb.append("学习习惯: ").append(nullToDefault(profile.getLearningHabits(), "无"));
        return sb.toString();
    }

    /**
     * 格式化学习行为数据为可读文本
     */
    private String formatBehaviors(List<LearningBehavior> behaviors) {
        if (behaviors == null || behaviors.isEmpty()) {
            return "暂无学习行为数据";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("共").append(behaviors.size()).append("条学习记录\n");

        // 汇总统计
        long totalDuration = behaviors.stream().mapToLong(b -> b.getDuration() != null ? b.getDuration() : 0).sum();
        sb.append("总学习时长: ").append(totalDuration / 60).append("分钟\n");

        Map<String, Long> typeCount = new HashMap<>();
        for (LearningBehavior b : behaviors) {
            String type = b.getBehaviorType() != null ? b.getBehaviorType() : "未知";
            typeCount.merge(type, 1L, Long::sum);
        }
        sb.append("行为分布: ").append(typeCount);

        // 最近5条明细
        sb.append("\n最近学习记录:\n");
        int count = Math.min(5, behaviors.size());
        for (int i = 0; i < count; i++) {
            LearningBehavior b = behaviors.get(i);
            sb.append("- ").append(b.getBehaviorType())
              .append(", 时长").append(b.getDuration() != null ? b.getDuration() : 0).append("秒")
              .append(", 时间").append(b.getBehaviorTime())
              .append("\n");
        }
        return sb.toString();
    }

    /**
     * 格式化答题记录为可读文本
     */
    private String formatAnswers(List<QuestionAnswerRecord> answers) {
        if (answers == null || answers.isEmpty()) {
            return "暂无答题记录";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("共").append(answers.size()).append("条答题记录\n");

        long correct = answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
        sb.append("正确数: ").append(correct).append(", 正确率: ")
          .append(Math.round((double) correct / answers.size() * 1000) / 10.0).append("%\n");

        double avgTime = answers.stream()
                .filter(a -> a.getSpendTime() != null)
                .mapToLong(QuestionAnswerRecord::getSpendTime)
                .average().orElse(0);
        sb.append("平均答题时长: ").append(Math.round(avgTime)).append("秒");

        return sb.toString();
    }

    private LocalDateTime parseStartDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateStr + " 00:00:00",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDateTime parseEndDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateStr + " 23:59:59",
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            return null;
        }
    }

    private String nullToDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}
