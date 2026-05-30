package com.education.outcome.controller;

import com.education.context.BaseContext;
import com.education.outcome.service.LearningEvaluateService;
import com.education.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final LearningEvaluateService evaluateService;

    /**
     * 获取评估报告（按日期范围或周期）
     * GET /assessment/report?startDate=2026-05-01&endDate=2026-05-29
     * GET /assessment/report?period=week
     */
    @GetMapping("/report")
    public Result getReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String period) {

        // 支持 period 参数自动计算日期范围
        if ((startDate == null || endDate == null) && period != null) {
            LocalDate end = LocalDate.now();
            LocalDate start = switch (period) {
                case "month" -> end.minusDays(30);
                case "quarter" -> end.minusDays(90);
                default -> end.minusDays(7); // week
            };
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            startDate = start.format(fmt);
            endDate = end.format(fmt);
        }

        // 兜底：默认近7天
        if (startDate == null || endDate == null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            endDate = LocalDate.now().format(fmt);
            startDate = LocalDate.now().minusDays(7).format(fmt);
        }

        return Result.success(evaluateService.getReport(getUserId(), startDate, endDate));
    }

    /**
     * AI生成评估报告
     * POST /assessment/report/generate
     * Body: { "period": "week", "pathId": 1, "includeModules": "learningTime,answer" }
     */
    @PostMapping("/report/generate")
    public Result generateReport(@RequestBody Map<String, Object> param) {
        Long pathId = null;
        if (param.get("pathId") != null) {
            pathId = Long.valueOf(param.get("pathId").toString());
        }

        java.util.List<String> modules;
        Object modulesObj = param.get("includeModules");
        if (modulesObj instanceof java.util.List) {
            modules = (java.util.List<String>) modulesObj;
        } else if (modulesObj != null) {
            modules = java.util.Arrays.asList(modulesObj.toString().split(","));
        } else {
            modules = java.util.List.of("learningTime", "answer", "video", "path");
        }

        return Result.success(evaluateService.generateReport(pathId, modules));
    }

    /**
     * 评估结果详情
     * GET /assessment/result?reportId=1
     */
    @GetMapping("/result")
    public Result getResult(@RequestParam(required = false) Long reportId) {
        com.education.entity.LearningEvaluate evaluate;
        if (reportId == null) {
            evaluate = evaluateService.getLatestReport(getUserId());
        } else {
            evaluate = evaluateService.getById(reportId);
        }
        // 转为前端期望的结构
        if (evaluate == null) {
            return Result.success(null);
        }
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", evaluate.getId());
        result.put("evaluateContent", evaluate.getEvaluateContent());
        result.put("improveSuggest", evaluate.getImproveSuggest());
        // knowledgeMastery 是 JSON 字符串，解析为对象
        try {
            if (evaluate.getKnowledgeMastery() != null) {
                result.put("knowledgeMastery", com.alibaba.fastjson2.JSON.parse(evaluate.getKnowledgeMastery()));
            }
        } catch (Exception e) {
            result.put("knowledgeMastery", null);
        }
        result.put("createTime", evaluate.getCreateTime());
        return Result.success(result);
    }

    /**
     * 学习统计数据
     * GET /assessment/stats?startDate=...&endDate=...&type=all
     */
    @GetMapping("/stats")
    public Result getStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "all") String type) {

        if (startDate == null || endDate == null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            endDate = LocalDate.now().format(fmt);
            startDate = LocalDate.now().minusDays(30).format(fmt);
        }

        return Result.success(evaluateService.getStats(getUserId(), startDate, endDate, type));
    }

    /**
     * 薄弱点分析
     * GET /assessment/weak-points
     */
    @GetMapping("/weak-points")
    public Result weakPoints() {
        return Result.success(evaluateService.getWeakPoints(getUserId()));
    }

    /**
     * 学习趋势
     * GET /assessment/trend?period=7d
     */
    @GetMapping("/trend")
    public Result trend(@RequestParam(required = false, defaultValue = "7d") String period) {
        return Result.success(evaluateService.getTrend(getUserId(), period));
    }

    private Long getUserId() {
        return BaseContext.getCurrentId();
    }
}
