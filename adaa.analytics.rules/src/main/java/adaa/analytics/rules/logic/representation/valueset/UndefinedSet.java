package adaa.analytics.rules.logic.representation.valueset;

import adaa.analytics.rules.utils.DoubleFormatter;

import java.util.List;

public class UndefinedSet extends SingletonSet{

    /** Gets {@link #value} */
    public double getValue() { throw new RuntimeException("Illegal call for UndefinedSet: getValue"); }
    /** Sets {@link #value} */
    public void setValue(double v) { throw new RuntimeException("Illegal call for UndefinedSet: setValue"); }

    /** Gets {@link #value} as string */
    public String getValueAsString() { throw new RuntimeException("Illegal call for UndefinedSet: getValueAsString"); }

    /** Gets {@link #mapping} */
    public List<String> getMapping() { throw new RuntimeException("Illegal call for UndefinedSet: getMapping" ); }
    /** Sets {@link #mapping} */
    public void setMapping(List<String> v) { throw new RuntimeException("Illegal call for UndefinedSet: setMapping"); }

    public UndefinedSet() {
        super(Double.NaN, null);
    }

    @Override
    public String toString() {
       return "";
    }
}
