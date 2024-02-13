package adaa.analytics.rules.logic.actions.descriptors;

import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
import java.util.function.Function;

public abstract class ActionRuleSetDescriptorBase<RetType> {

    private final String name;
    private final Function<ActionRuleSet, RetType> func;

    protected ActionRuleSetDescriptorBase() {
        name = "pure virtual call";
        func = null;
    }

    protected ActionRuleSetDescriptorBase(String name) {
        this.name = name;
        func = this::descriptor;
    }

    public ActionRuleSetDescriptorBase(String name, Function<ActionRuleSet, RetType> functor) {
        this.name = name;
        func = functor;
    }

    protected abstract RetType descriptor(ActionRuleSet ruleSet);

    public RetType apply(ActionRuleSet ruleSet) {
        return func.apply(ruleSet);
    }

    public String getName(){
        return name;
    }

}
