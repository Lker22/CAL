package com.education.outcome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.LearningPathStep;

public interface LearningPathStepService extends IService<LearningPathStep> {

    /**
     * 统计路径下已完成的步骤数
     */
    int countCompleted(Long pathId);

    /**
     * 统计路径下的总步骤数
     */
    int countByPath(Long pathId);
}
