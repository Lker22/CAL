package com.education.outcome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.LearningEvaluate;

import java.util.List;
import java.util.Map;

public interface LearningEvaluateService extends IService<LearningEvaluate> {

    /**
     * 获取评估报告（按日期范围）
     */
    List<LearningEvaluate> getReport(Long userId, String startDate, String endDate);

    /**
     * AI生成评估报告
     * @param pathId 学习路径ID（可为null评估整体）
     * @param includeModules 包含的评估模块
     */
    LearningEvaluate generateReport(Long pathId, List<String> includeModules);

    /**
     * 获取最新一条评估报告
     */
    LearningEvaluate getLatestReport(Long userId);

    /**
     * 获取学习统计数据
     */
    Map<String, Object> getStats(Long userId, String startDate, String endDate, String type);

    /**
     * 获取薄弱点分析
     */
    Map<String, Object> getWeakPoints(Long userId);

    /**
     * 获取学习趋势
     */
    List<Map<String, Object>> getTrend(Long userId, String period);
}
