package com.education.path.controller;

import com.education.context.BaseContext;
import com.education.path.service.LearningPathService;
import com.education.vo.AdjustPathVO;
import com.education.vo.CompleteStepVO;
import com.education.vo.GeneratePathVO;
import com.education.vo.RecordBehaviorVO;
import com.education.dto.QuizAnswerDTO;
import com.education.result.Result;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习路径控制器
 */
@RestController
@RequestMapping("/learning-path")
@Slf4j
public class LearningPathController {

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    /**
     * 一键生成学习路径
     * POST /learning-path/generate
     */
    @PostMapping("/generate")
    public Result<?> generatePath(@Valid @RequestBody GeneratePathVO vo) {
        Long userId = BaseContext.getCurrentId();
        log.info("生成学习路径, userId={}, subject={}", userId, vo.getSubject());
        return learningPathService.generatePath(userId, vo);
    }

    /**
     * 获取学习路径列表
     * GET /learning-path/list
     */
    @GetMapping("/list")
    public Result<?> getPathList(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String status) {
        Long userId = BaseContext.getCurrentId();
        return learningPathService.getPathList(userId, page, pageSize, status);
    }

    /**
     * 获取路径详情(含步骤)
     * GET /learning-path/detail/{pathId}
     */
    @GetMapping("/detail/{pathId}")
    public Result<?> getPathDetail(@PathVariable Long pathId) {
        Long userId = BaseContext.getCurrentId();
        return learningPathService.getPathDetail(pathId, userId);
    }

    /**
     * 完成学习步骤(打卡)
     * POST /learning-path/step/{stepId}/complete
     */
    @PostMapping("/step/{stepId}/complete")
    public Result<?> completeStep(@PathVariable Long stepId,
                                   @RequestBody CompleteStepVO vo) {
        Long userId = BaseContext.getCurrentId();
        log.info("完成学习步骤打卡, userId={}, stepId={}", userId, stepId);
        return learningPathService.completeStep(stepId, userId, vo);
    }

    /**
     * 获取智能资源推荐
     * GET /learning-path/{pathId}/recommend
     */
    @GetMapping("/{pathId}/recommend")
    public Result<?> getRecommendedResources(@PathVariable Long pathId) {
        Long userId = BaseContext.getCurrentId();
        return learningPathService.getRecommendedResources(pathId, userId);
    }

    /**
     * 动态调整学习路径
     * PUT /learning-path/{pathId}/adjust
     */
    @PutMapping("/{pathId}/adjust")
    public Result<?> adjustPath(@PathVariable Long pathId,
                                 @Valid @RequestBody AdjustPathVO vo) {
        Long userId = BaseContext.getCurrentId();
        log.info("调整学习路径, userId={}, pathId={}, type={}", userId, pathId, vo.getAdjustmentType());
        return learningPathService.adjustPath(pathId, userId, vo);
    }

    /**
     * 删除学习路径
     * DELETE /learning-path/{pathId}
     */
    @DeleteMapping("/{pathId}")
    public Result<?> deletePath(@PathVariable Long pathId) {
        Long userId = BaseContext.getCurrentId();
        log.info("删除学习路径, userId={}, pathId={}", userId, pathId);
        return learningPathService.deletePath(pathId, userId);
    }

    /**
     * 记录学习行为
     * POST /learning-path/behavior
     */
    @PostMapping("/behavior")
    public Result<?> recordBehavior(@RequestBody RecordBehaviorVO vo) {
        Long userId = BaseContext.getCurrentId();
        log.info("记录学习行为, userId={}, type={}", userId, vo.getBehaviorType());
        return learningPathService.recordBehavior(userId, vo);
    }

    /**
     * 生成步骤学习资源(检查缓存/调用智能体)
     * POST /learning-path/step/{stepId}/generate-resource
     */
    @PostMapping("/step/{stepId}/generate-resource")
    public Result<?> generateStepResource(@PathVariable Long stepId) {
        Long userId = BaseContext.getCurrentId();
        return learningPathService.generateStepResource(stepId, userId);
    }

    /**
     * 关联生成的资源到步骤
     * POST /learning-path/step/{stepId}/link-resource/{resourceId}
     */
    @PostMapping("/step/{stepId}/link-resource/{resourceId}")
    public Result<?> linkResourceToStep(@PathVariable Long stepId,
                                         @PathVariable Long resourceId) {
        Long userId = BaseContext.getCurrentId();
        return learningPathService.linkResourceToStep(stepId, resourceId, userId);
    }

    /**
     * 提交测验答案
     * POST /learning-path/step/{stepId}/submit-quiz
     */
    @PostMapping("/step/{stepId}/submit-quiz")
    public Result<?> submitQuiz(@PathVariable Long stepId,
                                @RequestBody List<QuizAnswerDTO> answers) {
        Long userId = BaseContext.getCurrentId();
        return learningPathService.submitQuiz(stepId, userId, answers);
    }
}
