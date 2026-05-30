package com.education.tutor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.education.dto.TutorAskRequest;
import com.education.entity.SmartTutor;
import com.education.vo.TutorAnswerVO;
import com.education.vo.TutorHistoryVO;

public interface SmartTutorService extends IService<SmartTutor> {

    TutorAnswerVO ask(TutorAskRequest request);

    TutorHistoryVO getHistory(Long userId, Integer page, Integer pageSize);

    TutorAnswerVO getDetail(Long recordId, Long userId);

    boolean deleteRecord(Long recordId, Long userId);
}