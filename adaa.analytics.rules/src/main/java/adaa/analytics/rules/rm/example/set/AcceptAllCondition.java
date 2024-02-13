package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

public class AcceptAllCondition implements ICondition {
    private static final long serialVersionUID = 9217842736819037165L;

    public AcceptAllCondition() {
    }

    public AcceptAllCondition(IExampleSet exampleSet, String parameterString) {
    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public boolean conditionOk(Example example) {
        return true;
    }
}
