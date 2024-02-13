package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

public class MissingLabelsCondition implements ICondition {
    private static final long serialVersionUID = 6559275828082706521L;

    public MissingLabelsCondition(IExampleSet exampleSet, String parameterString) {
    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public boolean conditionOk(Example example) {
        return Double.isNaN(example.getValue(example.getAttributes().getLabel()));
    }
}
