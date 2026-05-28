package com.education.ai.controller;

import com.education.ai.service.AiAgentService;
import com.education.entity.AiAgent;
import com.education.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI多智能体资源生成模块控制器
 */
@RestController
@RequestMapping("/resource")
public class ResourceAgentController {

    @Autowired
    private AiAgentService aiAgentService;

    /**
     * 获取智能体列表
     * 接口文档：GET /resource/agents
     * @return 所有启用的智能体列表
     */
    @GetMapping("/agents")
    public Result<List<AiAgent>> getAgentList() {
        List<AiAgent> agentList = aiAgentService.listAllEnabledAgents();
        return Result.success(agentList);
    }

    /**
     * 获取智能体详情
     * 接口文档：GET /resource/agents/{agentId}
     * @param agentId 智能体ID
     * @return 智能体详情
     */
    @GetMapping("/agents/{agentId}")
    public Result<AiAgent> getAgentDetail(@PathVariable Long agentId) {
        AiAgent agent = aiAgentService.getAgentById(agentId);
        if (agent == null) {
            return Result.fail(404,"智能体不存在或已禁用");
        }
        return Result.success(agent);
    }
}