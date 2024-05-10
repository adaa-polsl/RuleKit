package adaa.analytics.rules.logic.rulegenerator;

import adaa.analytics.rules.logic.representation.rule.Rule;

public interface ICommandListener {

    public void onNewRule(Rule r);

    public void onProgress(int totalRules, int uncoveredRules);

    public boolean isRequestStop();
}
