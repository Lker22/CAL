package com.education.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.dto.ResourceQueryDTO;
import com.education.entity.LearningResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LearningResourceMapper extends BaseMapper<LearningResource> {
    // 分页查询我的学习资源（关联AI智能体名称）
    IPage<LearningResource> queryMyResource(Page<LearningResource> page,
                                            @Param("dto") ResourceQueryDTO dto);
}