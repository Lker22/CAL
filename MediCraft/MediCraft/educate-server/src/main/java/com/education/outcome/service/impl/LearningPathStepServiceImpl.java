package com.education.outcome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.entity.LearningPathStep;
import com.education.path.mapper.LearningPathStepMapper;
import com.education.outcome.service.LearningPathStepService;
import org.springframework.stereotype.Service;

@Service
public class LearningPathStepServiceImpl extends ServiceImpl<LearningPathStepMapper, LearningPathStep>
        implements LearningPathStepService {

    @Override
    public int countCompleted(Long pathId) {
        LambdaQueryWrapper<LearningPathStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPathStep::getPathId, pathId)
                .eq(LearningPathStep::getFinishStatus, 1);
        return (int) count(wrapper);
    }

    @Override
    public int countByPath(Long pathId) {
        LambdaQueryWrapper<LearningPathStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningPathStep::getPathId, pathId);
        return (int) count(wrapper);
    }
}
