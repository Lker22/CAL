package com.education.outcome.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.entity.QuestionAnswerRecord;
import com.education.outcome.mapper.QuestionAnswerRecordMapper;
import com.education.outcome.service.QuestionAnswerRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuestionAnswerRecordServiceImpl extends ServiceImpl<QuestionAnswerRecordMapper, QuestionAnswerRecord>
        implements QuestionAnswerRecordService {

    @Override
    public List<QuestionAnswerRecord> getByUserId(Long userId) {
        LambdaQueryWrapper<QuestionAnswerRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionAnswerRecord::getUserId, userId)
                .orderByDesc(QuestionAnswerRecord::getAnswerTime);
        return list(wrapper);
    }
}
