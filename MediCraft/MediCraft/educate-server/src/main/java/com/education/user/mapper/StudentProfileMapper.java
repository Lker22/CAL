package com.education.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.education.entity.StudentProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StudentProfileMapper extends BaseMapper<StudentProfile> {
}