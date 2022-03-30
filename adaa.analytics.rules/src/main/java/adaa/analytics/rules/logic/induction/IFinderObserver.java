package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Rule;

public interface IFinderObserver {

    public void growingStarted(Rule r);

    public void growingFinished(Rule r);

    public void conditionAdded(ConditionBase cnd);

    public void conditionRemoved(ConditionBase cnd);

    public void ruleReady(Rule r);
}
