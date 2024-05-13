package adaa.analytics.rules.data.row;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.metadata.Ontology;

import java.io.Serializable;
import java.util.*;

public class Example implements Serializable, Map<String, Object> {

    private int rowIndex;
    private IExampleSet parentExampleSet;

    public Example(int rowIndex, IExampleSet parentExampleSet) {
        this.rowIndex = rowIndex;
        this.parentExampleSet = parentExampleSet;
    }


    public IAttributes getAttributes() {
        return this.parentExampleSet.getAttributes();
    }

    public double getValue(String columnName) {
        return parentExampleSet.getDoubleValue(columnName, parentExampleSet.getColumnIndex(columnName), rowIndex, Double.NaN);
    }

    public double getValue(IAttribute a) {
        if (a == null) {
            return Double.NaN;
        } else {
            return parentExampleSet.getDoubleValue(a.getName(), a.getTableIndex(), rowIndex, Double.NaN);
        }
    }

    public String getNominalValue(IAttribute a) {
        if (!a.isNominal()) {
            throw new IllegalStateException("Extraction of nominal example value for non-nominal attribute '" + a.getName() + "' is not possible.");
        } else {
            double value = this.getValue(a);
            return Double.isNaN(value) ? "?" : a.getMapping().mapIndex((int) value);
        }
    }


    public void setValue(IAttribute a, double value) {
        parentExampleSet.setDoubleValue(a, rowIndex, value);
    }

    public void setValue(IAttribute a, String str) {
        if (!a.isNominal()) {
            throw new IllegalStateException("setValue(Attribute, String) only supported for nominal values!");
        } else {
            if (str != null) {
                this.setValue(a, (double) a.getMapping().mapString(str));
            } else {
                this.setValue(a, Double.NaN);
            }
        }
    }

    public double getLabel() {
        return this.getValue(this.getAttributes().getLabel());
    }

    public double getPredictedLabel() {
        return this.getValue(this.getAttributes().getPredictedLabel());
    }

    public double getWeight() {
        return this.getValue(this.getAttributes().getWeight());
    }

    public double getConfidence(String classValue) {
        return this.getValue(this.getAttributes().getConfidence(classValue));
    }

    public String getValueAsString(IAttribute attribute) {
        return this.getValueAsString(attribute, false);
    }

    public String getValueAsString(IAttribute attribute, boolean quoteNominal) {
        double value = this.getValue(attribute);
        return attribute.getAsString(value, quoteNominal);
    }

    public String toString() {
        return this.toDenseString(true);
    }

    public String toDenseString(boolean quoteNominal) {
        StringBuffer result = new StringBuffer();
        Iterator<IAttribute> a = this.getAttributes().allAttributes();

        for (boolean first = true; a.hasNext(); result.append(this.getValueAsString((IAttribute) a.next(), quoteNominal))) {
            if (first) {
                first = false;
            } else {
                result.append(" ");
            }
        }

        return result.toString();
    }

    public Object get(Object key) {
        IAttribute attribute = null;
        if (key instanceof String) {
            attribute = this.parentExampleSet.getAttributes().get((String) key);
        }

        double value = this.getValue(attribute);
        if (Double.isNaN(value)) {
            return "?";
        } else if (attribute == null) {
            return null;
        } else if (attribute.isNominal()) {
            return this.getValueAsString(attribute);
        } else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), 3)) {
            return (int) this.getValue(attribute);
        } else {
            return Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), 9) ? new Date((long) this.getValue(attribute)) : this.getValue(attribute);
        }
    }

    public Object put(String attributeName, Object value) {
        IAttribute attribute = this.parentExampleSet.getAttributes().get(attributeName);
        if (attribute == null) {
            throw new IllegalArgumentException("Unknown attribute name: '" + attributeName + "'");
        } else {
            if (attribute.isNumerical()) {
                if (value == null) {
                    this.setValue(attribute, Double.NaN);
                } else {
                    try {
                        double doubleValue = Double.parseDouble(value.toString());
                        this.setValue(attribute, doubleValue);
                    } catch (NumberFormatException var6) {
                        throw new IllegalArgumentException("Only numerical values are allowed for numerical attribute: '" + attributeName + "', was '" + value + "'");
                    }
                }
            } else if (value == null) {
                this.setValue(attribute, Double.NaN);
            } else {
                this.setValue(attribute, (double) attribute.getMapping().mapString(value.toString()));
            }

            return value;
        }
    }

    public void clear() {
        throw new UnsupportedOperationException("Clear is not supported by Example.");
    }

    public boolean containsKey(Object key) {
        IAttribute attribute = null;
        if (key instanceof String) {
            attribute = this.parentExampleSet.getAttributes().get((String) key);
        }

        return attribute != null;
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("ContainsValue is not supported by Example.");
    }

    public Set<Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("EntrySet is not supported by Example.");
    }

    public boolean isEmpty() {
        return this.parentExampleSet.columnCount() == 0;
    }

    public Set<String> keySet() {
        Set<String> allKeys = new HashSet<>();
        Iterator<IAttribute> a = this.parentExampleSet.getAttributes().allAttributes();

        while (a.hasNext()) {
            allKeys.add(((IAttribute) a.next()).getName());
        }

        return allKeys;
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        throw new UnsupportedOperationException("PutAll is not supported by Example.");
    }

    public String remove(Object key) {
        throw new UnsupportedOperationException("Remove is not supported by Example.");
    }

    public int size() {
        return this.parentExampleSet.columnCount();
    }

    public Collection<Object> values() {
        throw new UnsupportedOperationException("Values is not supported by Example.");
    }
}
