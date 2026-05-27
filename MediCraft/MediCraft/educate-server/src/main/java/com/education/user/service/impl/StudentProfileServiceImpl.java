package com.education.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.context.BaseContext;
import com.education.entity.StudentProfile;
import com.education.user.mapper.StudentProfileMapper;
import com.education.user.service.StudentProfileService;
import org.springframework.stereotype.Service;

@Service
public class StudentProfileServiceImpl extends ServiceImpl<StudentProfileMapper, StudentProfile>
        implements StudentProfileService {

    /**
     * 根据当前用户ID获取画像
     */
    @Override
    public StudentProfile getByUserId() {
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<StudentProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentProfile::getUserId, userId);
        return getOne(wrapper);
    }

    /**
     * 保存或更新画像
     */
    @Override
    public void saveOrUpdateProfile(StudentProfile profile) {
        Long userId = BaseContext.getCurrentId();
        StudentProfile exist = getByUserId();

        if (exist == null) {
            profile.setUserId(userId);
            save(profile);
        } else {
//            profile.setId(exist.getId());
//            profile.setUserId(userId);
//            updateById(profile);
            // 增量更新：只更新大模型返回的非空字段，保留原有数据
            LambdaUpdateWrapper<StudentProfile> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(StudentProfile::getId, exist.getId());

            // 知识基础
            if (profile.getKnowledgeBase() != null) {
                wrapper.set(StudentProfile::getKnowledgeBase, profile.getKnowledgeBase());
            }
            // 认知风格
            if (profile.getCognitiveStyle() != null) {
                wrapper.set(StudentProfile::getCognitiveStyle, profile.getCognitiveStyle());
            }
            // 学习目标
            if (profile.getLearningGoal() != null) {
                wrapper.set(StudentProfile::getLearningGoal, profile.getLearningGoal());
            }
            // 易错点
            if (profile.getErrorPronePoints() != null) {
                wrapper.set(StudentProfile::getErrorPronePoints, profile.getErrorPronePoints());
            }
            // 学习节奏
            if (profile.getLearningPace() != null) {
                wrapper.set(StudentProfile::getLearningPace, profile.getLearningPace());
            }
            // 资源偏好
            if (profile.getResourcePreference() != null) {
                wrapper.set(StudentProfile::getResourcePreference, profile.getResourcePreference());
            }
            // 学习习惯
            if (profile.getLearningHabits() != null) {
                wrapper.set(StudentProfile::getLearningHabits, profile.getLearningHabits());
            }
            // 更新场景（强制更新，每次更新都要记录场景）
            wrapper.set(StudentProfile::getUpdateScene, profile.getUpdateScene());

            // 执行更新
            update(wrapper);

        }
    }

    /**
     * 根据ID删除画像（给ProfileServiceImpl调用）
     */
    @Override
    public boolean removeById(Long id) {
        return removeById(id);
    }
}