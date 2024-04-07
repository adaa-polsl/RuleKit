package adaa.analytics.rules.data;

import java.util.Objects;

public class NominalMap implements Cloneable {

    private int index;
    private String value;

    public NominalMap(int index, String value) {
        this.index = index;
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof NominalMap)) {
            return false;
        }
        NominalMap nm = (NominalMap) o;
        return index == nm.getIndex() && value.equals(nm.getValue());
    }

    @Override
    public NominalMap clone() {
        try {
            return (NominalMap) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, value);
    }
}
