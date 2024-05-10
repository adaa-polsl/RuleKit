package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.condition.ConditionBase;
import adaa.analytics.rules.logic.representation.rule.Rule;

public interface IFinderObserver {

    public void growingStarted(Rule r);

    public void growingFinished(Rule r);

    public void conditionAdded(ConditionBase cnd);

    public void conditionRemoved(ConditionBase cnd);

    public void ruleReady(Rule r);
}
