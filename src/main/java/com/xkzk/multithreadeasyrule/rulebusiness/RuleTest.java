package com.xkzk.multithreadeasyrule.rulebusiness;

import com.xkzk.multithreadeasyrule.EasyRuleMultithreadApplication;
import com.xkzk.multithreadeasyrule.engine.MultiThreadRulesEngine;
import com.xkzk.multithreadeasyrule.facts.RuleEngineFacts;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author : ligang1
 * @version : v1.0
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EasyRuleMultithreadApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RuleTest {

    @Resource
    private List<BaseRule> baseRules;
    @Resource
    private ThreadPoolTaskExecutor ruleWorkThreadPool;

    @Test
    public void test() {
        RulesEngine engine = new MultiThreadRulesEngine(ruleWorkThreadPool);
        Rules rules = new Rules();
        for (BaseRule baseRule : baseRules) {
            rules.register(baseRule);
        }
        Facts facts = new Facts();
        RuleEngineFacts ruleEngineFacts = bulidFacts();
        facts.put("RuleEngineFacts", ruleEngineFacts);
        engine.fire(rules, facts);
    }

    private RuleEngineFacts bulidFacts() {
        RuleEngineFacts ruleEngineFacts = new RuleEngineFacts();
        ruleEngineFacts.setCondition1("condition1");
        ruleEngineFacts.setCondition2("condition2");
        return ruleEngineFacts;
    }
}
