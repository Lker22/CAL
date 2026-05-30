package com.education.outcome.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.entity.QuestionAnswerRecord;

import java.util.List;

public interface QuestionAnswerRecordService extends IService<QuestionAnswerRecord> {

    /**
     * 查询指定用户的答题记录
     */
    List<QuestionAnswerRecord> getByUserId(Long userId);
}
