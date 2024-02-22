package adaa.analytics.rules.rm.example.table;

import java.util.Iterator;

public class PolynominalAttribute extends NominalAttribute {
    private static final long serialVersionUID = 3713022530244256813L;
    private static final int MAX_NUMBER_OF_SHOWN_NOMINAL_VALUES = 100;
    private INominalMapping nominalMapping;

    public PolynominalAttribute(String name) {
        this(name, 1);
    }

    PolynominalAttribute(String name, int valueType) {
        super(name, valueType);
        this.nominalMapping = new PolynominalMapping();
    }

    private PolynominalAttribute(PolynominalAttribute a) {
        super(a);
        this.nominalMapping = new PolynominalMapping();
        this.nominalMapping = a.nominalMapping;
    }

    public Object clone() {
        return new PolynominalAttribute(this);
    }

    public INominalMapping getMapping() {
        return this.nominalMapping;
    }

    public void setMapping(INominalMapping newMapping) {
        this.nominalMapping = new PolynominalMapping(newMapping);
    }

    public String toString() {
        StringBuffer result = new StringBuffer(super.toString());
        result.append("/values=[");
        Iterator<String> i = this.nominalMapping.getValues().iterator();

        for(int index = 0; i.hasNext(); ++index) {
            if (index >= 100) {
                result.append(", ... (" + (this.nominalMapping.getValues().size() - 100) + " values) ...");
                break;
            }

            if (index != 0) {
                result.append(", ");
            }

            result.append((String)i.next());
        }

        result.append("]");
        return result.toString();
    }

    public boolean isDateTime() {
        return false;
    }
}
