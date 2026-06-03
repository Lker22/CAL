package com.education.path.service;

import com.education.vo.*;
import com.education.result.Result;

/**
 * 学习路径Service接口
 */
public interface LearningPathService {

    /**
     * 一键生成学习路径
     */
    Result<?> generatePath(Long userId, GeneratePathVO vo);

    /**
     * 获取学习路径列表
     */
    Result<?> getPathList(Long userId, Integer page, Integer pageSize, String status);

    /**
     * 获取路径详情(含步骤)
     */
    Result<?> getPathDetail(Long pathId, Long userId);

    /**
     * 完成学习步骤(打卡)
     */
    Result<?> completeStep(Long stepId, Long userId, CompleteStepVO vo);

    /**
     * 获取智能资源推荐
     */
    Result<?> getRecommendedResources(Long pathId, Long userId);

    /**
     * 动态调整学习路径
     */
    Result<?> adjustPath(Long pathId, Long userId, AdjustPathVO vo);

    /**
     * 删除学习路径
     */
    Result<?> deletePath(Long pathId, Long userId);

    /**
     * 记录学习行为
     */
    Result<?> recordBehavior(Long userId, RecordBehaviorVO vo);

    /**
     * 生成步骤学习资源
     */
    Result<?> generateStepResource(Long stepId, Long userId);

    /**
     * 关联资源到步骤
     */
    Result<?> linkResourceToStep(Long stepId, Long resourceId, Long userId);

    /**
     * 提交测验答案
     */
    Result<?> submitQuiz(Long stepId, Long userId, java.util.List<com.education.dto.QuizAnswerDTO> answers);
}
