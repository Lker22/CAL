package com.education.tutor.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.education.dto.TutorAskRequest;
import com.education.entity.SmartTutor;
import com.education.tutor.mapper.SmartTutorMapper;
import com.education.tutor.service.SmartTutorService;
import com.education.vo.TutorAnswerVO;
import com.education.vo.TutorHistoryVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class SmartTutorServiceImpl extends ServiceImpl<SmartTutorMapper, SmartTutor>
        implements SmartTutorService {

    @Resource
    private ChatClient chatClient;

    @Value("${spring.ai.tutor.prompt.template:你是一名专业的智能辅导老师。\\n\\n学生的问题是：{question}\\n\\n图片信息：{images}\\n\\n请根据学生的问题给出详细、易懂的解答。回答要求：使用Markdown格式，结构清晰，分点论述，必要时结合案例说明。}")
    private String tutorPromptTemplate;

    @Override
    public TutorAnswerVO ask(TutorAskRequest request) {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        Long userId = request.getUserId();

        Prompt prompt = buildPrompt(request);
        ChatResponse response = chatClient.prompt(prompt).call().chatResponse();
        String textAnswer = response.getResult().getOutput().getText();

        SmartTutor smartTutor = new SmartTutor();
        smartTutor.setUserId(userId);
        smartTutor.setSessionId(sessionId);
        smartTutor.setQuestion(request.getText());
        smartTutor.setTextAnswer(textAnswer);
        smartTutor.setCreateTime(LocalDateTime.now());
        this.save(smartTutor);

        TutorAnswerVO answerVO = new TutorAnswerVO();
        BeanUtils.copyProperties(smartTutor, answerVO);
        answerVO.setRecordId(smartTutor.getId());
        return answerVO;
    }

    @Override
    public TutorHistoryVO getHistory(Long userId, Integer page, Integer pageSize) {
        Page<TutorAnswerVO> pageParam = new Page<>(page, pageSize);
        IPage<TutorAnswerVO> resultPage = baseMapper.selectHistoryPage(pageParam, userId);

        TutorHistoryVO historyVO = new TutorHistoryVO();
        historyVO.setTotal(resultPage.getTotal());
        historyVO.setPages(resultPage.getPages());
        historyVO.setRecords(resultPage.getRecords());
        return historyVO;
    }

    @Override
    public TutorAnswerVO getDetail(Long recordId, Long userId) {
        return baseMapper.selectDetailById(recordId, userId);
    }

    @Override
    public boolean deleteRecord(Long recordId, Long userId) {
        SmartTutor record = this.lambdaQuery()
                .eq(SmartTutor::getId, recordId)
                .eq(SmartTutor::getUserId, userId)
                .one();
        if (record == null) {
            return false;
        }
        return this.removeById(recordId);
    }

    private Prompt buildPrompt(TutorAskRequest request) {
        PromptTemplate promptTemplate = new PromptTemplate(tutorPromptTemplate);
        Map<String, Object> params = new HashMap<>();
        params.put("question", request.getText());
        params.put("images", request.getImages() != null && !request.getImages().isEmpty()
                ? "学生提供了" + request.getImages().size() + "张图片，请结合图片内容分析" : "无");
        return promptTemplate.create(params);
    }
}
