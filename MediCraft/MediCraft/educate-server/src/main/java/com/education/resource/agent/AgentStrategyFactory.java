package com.education.resource.agent;

import com.education.resource.service.AgentGenerateStrategy;
import jakarta.annotation.Resource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能体生成策略工厂（根据agentRole获取对应策略）
 */
@Component
public class AgentStrategyFactory {

    @Resource
    private List<AgentGenerateStrategy> agentGenerateStrategies;

    // 缓存策略映射
    private Map<String, AgentGenerateStrategy> strategyMap;

    // 初始化策略映射
    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>();
        for (AgentGenerateStrategy strategy : agentGenerateStrategies) {
            strategyMap.put(strategy.getSupportRole(), strategy);
        }
    }

    /**
     * 获取智能体生成策略
     * @param agentRole 智能体角色
     * @return 生成策略
     */
    public AgentGenerateStrategy getStrategy(String agentRole) {
        if (strategyMap == null) {
            init();
        }
        return strategyMap.get(agentRole);
        //调用 getStrategy(agentRole) 时，就可以直接从 map 中通过 key 拿到对应的策略实例，而无需每次遍历整个 list，提升查找效率。
    }
}