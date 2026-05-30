package com.education.outcome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.entity.LearningBehavior;
import com.education.outcome.mapper.LearningBehaviorMapper;
import com.education.outcome.service.LearningBehaviorService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LearningBehaviorServiceImpl extends ServiceImpl<LearningBehaviorMapper, LearningBehavior>
        implements LearningBehaviorService {

    @Override
    public List<LearningBehavior> getByUserId(Long userId) {
        LambdaQueryWrapper<LearningBehavior> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LearningBehavior::getUserId, userId)
                .orderByDesc(LearningBehavior::getBehaviorTime);
        return list(wrapper);
    }
}
