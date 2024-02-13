package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

public class NoMissingLabelsCondition implements ICondition {
    private static final long serialVersionUID = 8047504208389222350L;

    public NoMissingLabelsCondition(IExampleSet exampleSet, String parameterString) {
    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public boolean conditionOk(Example example) {
        return !Double.isNaN(example.getValue(example.getAttributes().getLabel()));
    }
}
