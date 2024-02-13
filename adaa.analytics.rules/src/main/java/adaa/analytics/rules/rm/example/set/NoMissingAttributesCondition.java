package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;

public class NoMissingAttributesCondition implements ICondition {
    private static final long serialVersionUID = 631871757551493977L;

    public NoMissingAttributesCondition(IExampleSet exampleSet, String parameterString) {
    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public boolean conditionOk(Example example) {
        Iterator var2 = example.getAttributes().iterator();

        IAttribute attribute;
        do {
            if (!var2.hasNext()) {
                return true;
            }

            attribute = (IAttribute)var2.next();
        } while(!Double.isNaN(example.getValue(attribute)));

        return false;
    }
}
