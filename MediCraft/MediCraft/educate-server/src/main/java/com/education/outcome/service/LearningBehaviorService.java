package com.education.outcome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.LearningBehavior;

import java.util.List;

public interface LearningBehaviorService extends IService<LearningBehavior> {

    /**
     * 查询指定用户的学习行为记录
     */
    List<LearningBehavior> getByUserId(Long userId);
}
