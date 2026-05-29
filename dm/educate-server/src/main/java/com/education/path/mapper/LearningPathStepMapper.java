package com.education.path.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.entity.LearningPathStep;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 学习路径步骤Mapper
 */
@Mapper
public interface LearningPathStepMapper extends BaseMapper<LearningPathStep> {

    /**
     * 根据路径ID查询步骤列表
     */
    @Select("SELECT * FROM learning_path_step WHERE path_id = #{pathId} AND deleted = 0 ORDER BY sort ASC")
    List<LearningPathStep> selectByPathId(@Param("pathId") Long pathId);
}
