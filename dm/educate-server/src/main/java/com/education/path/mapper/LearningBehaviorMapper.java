package com.education.path.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.entity.LearningBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学习行为记录Mapper
 */
@Mapper
public interface LearningBehaviorMapper extends BaseMapper<LearningBehavior> {

    /**
     * 根据用户ID查询学习行为记录
     */
    @Select("SELECT * FROM learning_behavior WHERE user_id = #{userId} AND deleted = 0 ORDER BY behavior_time DESC")
    List<LearningBehavior> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据路径步骤ID查询学习行为
     */
    @Select("SELECT * FROM learning_behavior WHERE step_id = #{stepId} AND deleted = 0 ORDER BY behavior_time DESC")
    List<LearningBehavior> selectByStepId(@Param("stepId") Long stepId);
}
