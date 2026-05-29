package com.education.path.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.entity.LearningPathStepResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学习路径步骤资源关联Mapper
 */
@Mapper
public interface LearningPathStepResourceMapper extends BaseMapper<LearningPathStepResource> {

    /**
     * 根据步骤ID查询关联的资源ID列表
     */
    @Select("SELECT * FROM learning_path_step_resource WHERE step_id = #{stepId} ORDER BY sort ASC")
    List<LearningPathStepResource> selectByStepId(@Param("stepId") Long stepId);
}
