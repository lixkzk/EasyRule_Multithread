package com.xkzk.multithreadeasyrule.rulebusiness;

import com.xkzk.multithreadeasyrule.facts.RuleEngineFacts;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.annotation.Action;
import org.jeasy.rules.annotation.Condition;
import org.jeasy.rules.annotation.Fact;
import org.jeasy.rules.annotation.Rule;
import org.springframework.stereotype.Component;

/**
 * @author : ligang1
 * @version : v1.0
 **/
@Slf4j
@Component
@Rule(description = "R001", name = "R001Business", priority = 1)
public class R001Business implements BaseRule {

    @Override
    public String code() {
        return "R001";
    }

    @Condition
    public boolean evaluate(@Fact(value = "RuleEngineFacts") RuleEngineFacts ruleEngineFacts) {
        return true;
    }

    @Action
    public void execute(@Fact(value = "RuleEngineFacts") RuleEngineFacts ruleEngineFacts) {
        log.info("R001 >>>>>>>>>>>" + ruleEngineFacts.getCondition1() + ",threadname: " + Thread.currentThread().getName());
    }
}
