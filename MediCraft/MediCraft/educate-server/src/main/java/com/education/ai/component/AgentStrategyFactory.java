package com.education.ai.component;

import com.education.ai.service.AgentGenerateStrategy;
import jakarta.annotation.Resource;
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
    //构建一个“角色 → 策略”的映射缓存，方便后续根据 agentRole 快速找到对应的生成策略。
    public void init() {
        strategyMap = new HashMap<>();
        for (AgentGenerateStrategy strategy : agentGenerateStrategies) {
            strategyMap.put(strategy.getSupportRole(), strategy);
            //从每个策略中获取它支持的智能体角色,将这个角色作为 key，策略实例作为 value，存入 map
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