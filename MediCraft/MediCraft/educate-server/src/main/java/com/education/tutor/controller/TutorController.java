package com.education.tutor.controller;

import com.education.context.BaseContext;
import com.education.dto.TutorAskRequest;
import com.education.result.Result;
import com.education.tutor.service.SmartTutorService;
import com.education.vo.TutorAnswerVO;
import com.education.vo.TutorHistoryVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/tutor")
public class TutorController {

    @Resource
    private SmartTutorService smartTutorService;

    @PostMapping("/ask")
    public Result<TutorAnswerVO> ask(@Valid @RequestBody TutorAskRequest request) {
        Long userId = BaseContext.getCurrentId();
        request.setUserId(userId);
        TutorAnswerVO answerVO = smartTutorService.ask(request);
        return Result.success(answerVO);
    }

    @GetMapping("/history")
    public Result<TutorHistoryVO> getHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = BaseContext.getCurrentId();
        TutorHistoryVO historyVO = smartTutorService.getHistory(userId, page, pageSize);
        return Result.success(historyVO);
    }

    @GetMapping("/detail/{recordId}")
    public Result<TutorAnswerVO> getDetail(@PathVariable Long recordId) {
        Long userId = BaseContext.getCurrentId();
        TutorAnswerVO detail = smartTutorService.getDetail(recordId, userId);
        return Result.success(detail);
    }

    @DeleteMapping("/{recordId}")
    public Result<Boolean> deleteRecord(@PathVariable Long recordId) {
        Long userId = BaseContext.getCurrentId();
        boolean result = smartTutorService.deleteRecord(recordId, userId);
        return Result.success(result);
    }
}
