package adaa.analytics.rules.operator;

import adaa.analytics.rules.logic.induction.IFinderObserver;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.Rule;

import java.util.ArrayList;
import java.util.List;

public class OperatorCommandProxy {

    private List<ICommandProxyClient> commandProxyList = new ArrayList<>();

    public void onNewRule(Rule r) {
        for (ICommandProxyClient commandProxyClient: commandProxyList) {
            commandProxyClient.onNewRule(r);
        }
    }

    public void addCommandProxyClient(ICommandProxyClient commandProxyClient)
    {
        commandProxyList.add(commandProxyClient);
    }

    public void onProgressChange(int totalRules, int uncoveredRules)
    {
        for (ICommandProxyClient commandProxyClient: commandProxyList) {
            commandProxyClient.onProgress(totalRules,  uncoveredRules);
        }
    }

    public boolean isRequestStop()
    {
        for (ICommandProxyClient commandProxyClient: commandProxyList) {
            if (commandProxyClient.isRequestStop())
                return true;
        }
        return false;
    }
}
