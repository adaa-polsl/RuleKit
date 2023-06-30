package adaa.analytics.rules.operator;

import adaa.analytics.rules.logic.representation.Rule;

public interface ICommandProxyClient {

    public void onNewRule(Rule r);

    public void onProgress(int totalRules, int uncoveredRules);

    public boolean isRequestStop();
}
