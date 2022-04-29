package com.xkzk.multithreadeasyrule.engine;


import org.jeasy.rules.api.RuleListener;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.api.RulesEngineListener;
import org.jeasy.rules.core.RulesEngineParameters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
  * @Author: ligang1
  * @version: v1.0
  **/
public abstract class AbstractRulesEngine implements RulesEngine {
    protected RulesEngineParameters parameters;
    protected List<RuleListener> ruleListeners;
    protected List<RulesEngineListener> rulesEngineListeners;

    protected AbstractRulesEngine() {
        this(new RulesEngineParameters());
    }

    protected AbstractRulesEngine(final RulesEngineParameters parameters) {
        this.parameters = parameters;
        this.ruleListeners = new ArrayList<>();
        this.rulesEngineListeners = new ArrayList<>();
    }

    @Override
    public RulesEngineParameters getParameters() {
        return new RulesEngineParameters(
                parameters.isSkipOnFirstAppliedRule(),
                parameters.isSkipOnFirstFailedRule(),
                parameters.isSkipOnFirstNonTriggeredRule(),
                parameters.getPriorityThreshold()
        );
    }

    @Override
    public List<RuleListener> getRuleListeners() {
        return Collections.unmodifiableList(ruleListeners);
    }

    @Override
    public List<RulesEngineListener> getRulesEngineListeners() {
        return Collections.unmodifiableList(rulesEngineListeners);
    }

    public void registerRuleListener(RuleListener ruleListener) {
        ruleListeners.add(ruleListener);
    }

    public void registerRuleListeners(List<RuleListener> ruleListeners) {
        this.ruleListeners.addAll(ruleListeners);
    }

    public void registerRulesEngineListener(RulesEngineListener rulesEngineListener) {
        rulesEngineListeners.add(rulesEngineListener);
    }

    public void registerRulesEngineListeners(List<RulesEngineListener> rulesEngineListeners) {
        this.rulesEngineListeners.addAll(rulesEngineListeners);
    }
}
