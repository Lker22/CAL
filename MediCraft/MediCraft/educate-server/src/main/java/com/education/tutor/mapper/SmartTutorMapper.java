package com.education.tutor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.education.entity.SmartTutor;
import com.education.vo.TutorAnswerVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmartTutorMapper extends BaseMapper<SmartTutor> {
    /** 分页查询答疑历史 */
    IPage<TutorAnswerVO> selectHistoryPage(Page<TutorAnswerVO> page,
                                           @Param("userId") Long userId);

    /** 查询答疑详情 */
    TutorAnswerVO selectDetailById(@Param("id") Long recordId, 
                                   @Param("userId") Long userId);
}