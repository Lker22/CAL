package com.education.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.entity.ResourceGenerateTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ResourceGenerateTaskMapper extends BaseMapper<ResourceGenerateTask> {
    // 根据taskId查询任务
    ResourceGenerateTask selectByTaskId(@Param("taskId") String taskId);
}