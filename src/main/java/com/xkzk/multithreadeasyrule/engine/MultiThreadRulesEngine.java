package com.xkzk.multithreadeasyrule.engine;

import lombok.extern.slf4j.Slf4j;
import org.jeasy.rules.api.*;
import org.jeasy.rules.core.RulesEngineParameters;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author : ligang1
 * @version : v1.0
 * 多线程执行规则，下列参数配置无效：
 *    skipOnFirstAppliedRule
 *    skipOnFirstNonTriggeredRule
 *    skipOnFirstFailedRule
 **/
@Slf4j
public class MultiThreadRulesEngine extends AbstractRulesEngine {
    ThreadPoolTaskExecutor threadPoolTaskExecutor;

    public MultiThreadRulesEngine(){
        super();
    }

    public MultiThreadRulesEngine(ThreadPoolTaskExecutor threadPoolTaskExecutor){
        super();
        this.threadPoolTaskExecutor = threadPoolTaskExecutor;
    }

    public MultiThreadRulesEngine(RulesEngineParameters parameters) {
        super(parameters);
    }

    @Override
    public void fire(Rules rules, Facts facts) {
        triggerListenersBeforeRules(rules, facts);
        doFire(rules, facts);
        triggerListenersAfterRules(rules, facts);
    }

    void doFire(Rules rules, Facts facts) {
        if (rules.isEmpty()) {
            log.warn("No rules registered! Nothing to apply");
            return;
        }
        logEngineParameters();
        log(rules);
        log(facts);
        log.debug("Rules evaluation started");

        //key : priority ; value : List<Rule>
        Map<Integer, List<Rule>> priorityRuleMap = new HashMap<>();
        for (Rule rule : rules) {
            List<Rule> ruleList = priorityRuleMap.get(rule.getPriority());
            if (Objects.isNull(ruleList)) {
                ruleList = new ArrayList<>();
                priorityRuleMap.put(rule.getPriority(), ruleList);
            }
            ruleList.add(rule);
        }

        //按priority 升序执行
        priorityRuleMap.keySet().stream().sorted().forEach(x -> {
            List<Rule> ruleLists = priorityRuleMap.get(x);
            List<CompletableFuture<Void>> completableFutureList = new ArrayList<>();
            for (Rule rule : ruleLists) {
                completableFutureList.add(CompletableFuture.runAsync(() -> doFire(rule, facts), threadPoolTaskExecutor));
            }

            for (CompletableFuture<Void> completableFuture : completableFutureList) {
                completableFuture.join();
            }
        });
    }

    private void doFire(Rule rule, Facts facts){
        log.debug("DoFire  -> rule :{}, threadname :{}",rule.getName(),Thread.currentThread().getName());

        final String name = rule.getName();
        final int priority = rule.getPriority();
        if (priority > parameters.getPriorityThreshold()) {
            log.debug("Rule priority threshold ({}) exceeded at rule '{}' with priority={}, this rules will be skipped",
                    parameters.getPriorityThreshold(), name, priority);
            return;
        }
        if (!shouldBeEvaluated(rule, facts)) {
            log.debug("Rule '{}' has been skipped before being evaluated",
                    name);
            return;
        }
        if (rule.evaluate(facts)) {
            log.debug("Rule '{}' triggered", name);
            triggerListenersAfterEvaluate(rule, facts, true);
            try {
                triggerListenersBeforeExecute(rule, facts);
                rule.execute(facts);
                log.debug("Rule '{}' performed successfully", name);
                triggerListenersOnSuccess(rule, facts);
//                if (parameters.isSkipOnFirstAppliedRule()) {
//                    log.debug("Next rules will be skipped since parameter skipOnFirstAppliedRule is set");
//                    break;
//                }
            } catch (Exception exception) {
                log.error("Rule '" + name + "' performed with error", exception);
                triggerListenersOnFailure(rule, exception, facts);
//                if (parameters.isSkipOnFirstFailedRule()) {
//                    log.debug("Next rules will be skipped since parameter skipOnFirstFailedRule is set");
//                    break;
//                }
            }
        } else {
            log.debug("Rule '{}' has been evaluated to false, it has not been executed", name);
            triggerListenersAfterEvaluate(rule, facts, false);
//            if (parameters.isSkipOnFirstNonTriggeredRule()) {
//                log.debug("Next rules will be skipped since parameter skipOnFirstNonTriggeredRule is set");
//                break;
//            }
        }
    }

    private void logEngineParameters() {
        log.debug(parameters.toString());
    }

    private void log(Rules rules) {
        log.debug("Registered rules:");
        for (Rule rule : rules) {
            log.debug("Rule { name = '{}', description = '{}', priority = '{}'}",
                    rule.getName(), rule.getDescription(), rule.getPriority());
        }
    }

    private void log(Facts facts) {
        log.debug("Known facts:");
        for (Map.Entry<String, Object> fact : facts) {
            log.debug("Fact { {} : {} }",
                    fact.getKey(), fact.getValue());
        }
    }

    @Override
    public Map<Rule, Boolean> check(Rules rules, Facts facts) {
        triggerListenersBeforeRules(rules, facts);
        Map<Rule, Boolean> result = doCheck(rules, facts);
        triggerListenersAfterRules(rules, facts);
        return result;
    }

    private Map<Rule, Boolean> doCheck(Rules rules, Facts facts) {
        log.debug("Checking rules");
        Map<Rule, Boolean> result = new HashMap<>();
        for (Rule rule : rules) {
            if (shouldBeEvaluated(rule, facts)) {
                result.put(rule, rule.evaluate(facts));
            }
        }
        return result;
    }

    private void triggerListenersOnFailure(final Rule rule, final Exception exception, Facts facts) {
        for (RuleListener ruleListener : ruleListeners) {
            ruleListener.onFailure(rule, facts, exception);
        }
    }

    private void triggerListenersOnSuccess(final Rule rule, Facts facts) {
        for (RuleListener ruleListener : ruleListeners) {
            ruleListener.onSuccess(rule, facts);
        }
    }

    private void triggerListenersBeforeExecute(final Rule rule, Facts facts) {
        for (RuleListener ruleListener : ruleListeners) {
            ruleListener.beforeExecute(rule, facts);
        }
    }

    private boolean triggerListenersBeforeEvaluate(Rule rule, Facts facts) {
        for (RuleListener ruleListener : ruleListeners) {
            if (!ruleListener.beforeEvaluate(rule, facts)) {
                return false;
            }
        }
        return true;
    }

    private void triggerListenersAfterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {
        for (RuleListener ruleListener : ruleListeners) {
            ruleListener.afterEvaluate(rule, facts, evaluationResult);
        }
    }

    private void triggerListenersBeforeRules(Rules rule, Facts facts) {
        for (RulesEngineListener rulesEngineListener : rulesEngineListeners) {
            rulesEngineListener.beforeEvaluate(rule, facts);
        }
    }

    private void triggerListenersAfterRules(Rules rule, Facts facts) {
        for (RulesEngineListener rulesEngineListener : rulesEngineListeners) {
            rulesEngineListener.afterExecute(rule, facts);
        }
    }

    private boolean shouldBeEvaluated(Rule rule, Facts facts) {
        return triggerListenersBeforeEvaluate(rule, facts);
    }
}
