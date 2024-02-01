package adaa.analytics.rules.logic.rulegenerator;

import adaa.analytics.rules.logic.representation.Rule;

import java.util.ArrayList;
import java.util.List;

public class OperatorCommandProxy {

    private List<ICommandListener> commandListenerList = new ArrayList<>();

    public void onNewRule(Rule r) {
        for (ICommandListener commandProxyClient: commandListenerList) {
            commandProxyClient.onNewRule(r);
        }
    }

    public void addCommandListener(ICommandListener commandListener)
    {
        commandListenerList.add(commandListener);
    }

    public void onProgressChange(int totalRules, int uncoveredRules)
    {
        for (ICommandListener commandProxyClient: commandListenerList) {
            commandProxyClient.onProgress(totalRules,  uncoveredRules);
        }
    }

    public boolean isRequestStop()
    {
        for (ICommandListener commandProxyClient: commandListenerList) {
            if (commandProxyClient.isRequestStop())
                return true;
        }
        return false;
    }
}
