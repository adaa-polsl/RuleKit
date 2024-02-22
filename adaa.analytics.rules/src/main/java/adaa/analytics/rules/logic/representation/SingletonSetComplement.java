package adaa.analytics.rules.logic.representation;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.List;

public class SingletonSetComplement extends SingletonSet {
    /**
     * Initializes members with arguments.
     *
     * @param v       Singleton value.
     * @param mapping Mapping from value to label (can be null).
     */
    public SingletonSetComplement(double v, List<String> mapping) {
        super(v, mapping);
    }

    /**
     * Checks whether the set contains a given value. If the value is missing (NaN), the behaviour depends on the missing value policy
     * (see {@link adaa.analytics.rules.logic.representation.MissingValuesHandler}).
     * @param value Value to be checked.
     * @return Test result.
     */
    @Override
    public boolean contains(double value) {
        return (value != this.value) || (Double.isNaN(value) && MissingValuesHandler.ignore);
    }

    /**
     * Checks if the value set intersects with another one.
     * @param set Other value set.
     * @return Test result.
     */
    @Override
    public boolean intersects(IValueSet set) {
        SingletonSet ss = (set instanceof SingletonSet) ? (SingletonSet)set : null;
        if (ss != null) {
            return this.value != ss.value;
        }
        return true;
    }

    /**
     * Checks if the value set equals to other one.
     * @param obj Object co cmopare with.
     * @return Test result.
     */
    @Override
    public boolean equals(Object obj) {
        SingletonSetComplement ref = (obj instanceof SingletonSetComplement) ? (SingletonSetComplement) obj : null;

        if (ref != null) {
            EqualsBuilder builder = new EqualsBuilder();
            builder.append(value,  ref.value);
            builder.append(mapping, ref.mapping);
            return builder.isEquals();
        } else {
            return false;
        }
    }

    /**
     * Converts the value set to string.
     * @return Text representation of the value set.
     */
    @Override
    public String toString() {
        String s = "!{" + ((mapping == null) ? DoubleFormatter.format(value) : mapping.get((int)value)) + "}";
        return s;
    }

}
