package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.tools.Ontology;
import adaa.analytics.rules.rm.tools.Tools;

import java.io.Serializable;
import java.util.*;

public class Example implements Serializable, Map<String, Object> {
    private static final long serialVersionUID = 7761687908683290928L;
    public static final String SEPARATOR = " ";
    public static final String SPARSE_SEPARATOR = ":";
    private DataRow data;
    private IExampleSet parentExampleSet;

    public Example(DataRow data, IExampleSet parentExampleSet) {
        this.data = data;
        this.parentExampleSet = parentExampleSet;
    }

    public DataRow getDataRow() {
        return this.data;
    }

    public IAttributes getAttributes() {
        return this.parentExampleSet.getAttributes();
    }

    public double getValue(IAttribute a) {
        return this.data.get(a);
    }

    public String getNominalValue(IAttribute a) {
        if (!a.isNominal()) {
            throw new AttributeTypeException("Extraction of nominal example value for non-nominal attribute '" + a.getName() + "' is not possible.");
        } else {
            double value = this.getValue(a);
            return Double.isNaN(value) ? "?" : a.getMapping().mapIndex((int)value);
        }
    }

    public double getNumericalValue(IAttribute a) {
        if (!a.isNumerical()) {
            throw new AttributeTypeException("Extraction of numerical example value for non-numerical attribute '" + a.getName() + "' is not possible.");
        } else {
            return this.getValue(a);
        }
    }

    public Date getDateValue(IAttribute a) {
        if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(), 9)) {
            throw new AttributeTypeException("Extraction of date example value for non-date attribute '" + a.getName() + "' is not possible.");
        } else {
            return new Date((long)this.getValue(a));
        }
    }

    public void setValue(IAttribute a, double value) {
        this.data.set(a, value);
    }

    public void setValue(IAttribute a, String str) {
        if (!a.isNominal()) {
            throw new AttributeTypeException("setValue(Attribute, String) only supported for nominal values!");
        } else {
            if (str != null) {
                this.setValue(a, (double)a.getMapping().mapString(str));
            } else {
                this.setValue(a, Double.NaN);
            }

        }
    }

    public boolean equalValue(IAttribute first, IAttribute second) {
        if (first.isNominal() && second.isNominal()) {
            return this.getValueAsString(first).equals(this.getValueAsString(second));
        } else {
            return !first.isNominal() && !second.isNominal() ? Tools.isEqual(this.getValue(first), this.getValue(second)) : false;
        }
    }

    public double getLabel() {
        return this.getValue(this.getAttributes().getLabel());
    }

    public void setLabel(double value) {
        this.setValue(this.getAttributes().getLabel(), value);
    }

    public double getPredictedLabel() {
        return this.getValue(this.getAttributes().getPredictedLabel());
    }

    public void setPredictedLabel(double value) {
        this.setValue(this.getAttributes().getPredictedLabel(), value);
    }

    public double getId() {
        return this.getValue(this.getAttributes().getId());
    }

    public void setId(double value) {
        this.setValue(this.getAttributes().getId(), value);
    }

    public double getWeight() {
        return this.getValue(this.getAttributes().getWeight());
    }

    public void setWeight(double value) {
        this.setValue(this.getAttributes().getWeight(), value);
    }

    public double getConfidence(String classValue) {
        return this.getValue(this.getAttributes().getConfidence(classValue));
    }

    public void setConfidence(String classValue, double confidence) {
        this.setValue(this.getAttributes().getSpecial("confidence_" + classValue), confidence);
    }

    public String getValueAsString(IAttribute attribute) {
        return this.getValueAsString(attribute, -2, false);
    }

    public String getValueAsString(IAttribute attribute, int fractionDigits, boolean quoteNominal) {
        double value = this.getValue(attribute);
        return attribute.getAsString(value, fractionDigits, quoteNominal);
    }

    public String toString() {
        return this.toDenseString(-2, true);
    }

    public String toDenseString(int fractionDigits, boolean quoteNominal) {
        StringBuffer result = new StringBuffer();
        Iterator<IAttribute> a = this.getAttributes().allAttributes();

        for(boolean first = true; a.hasNext(); result.append(this.getValueAsString((IAttribute)a.next(), fractionDigits, quoteNominal))) {
            if (first) {
                first = false;
            } else {
                result.append(" ");
            }
        }

        return result.toString();
    }

    public String toSparseString(int format, int fractionDigits, boolean quoteNominal) {
        StringBuffer str = new StringBuffer();
        IAttribute labelAttribute = this.getAttributes().getSpecial("label");
        if (format == 1 && labelAttribute != null) {
            str.append(this.getValueAsString(labelAttribute, fractionDigits, quoteNominal) + " ");
        }

        IAttribute idAttribute = this.getAttributes().getSpecial("id");
        if (idAttribute != null) {
            str.append("id:" + this.getValueAsString(idAttribute, fractionDigits, quoteNominal) + " ");
        }

        IAttribute weightAttribute = this.getAttributes().getSpecial("weight");
        if (weightAttribute != null) {
            str.append("w:" + this.getValueAsString(weightAttribute, fractionDigits, quoteNominal) + " ");
        }

        IAttribute batchAttribute = this.getAttributes().getSpecial("batch");
        if (batchAttribute != null) {
            str.append("b:" + this.getValueAsString(batchAttribute, fractionDigits, quoteNominal) + " ");
        }

        str.append(this.getAttributesAsSparseString(" ", ":", fractionDigits, quoteNominal) + " ");
        if (format == 2 && labelAttribute != null) {
            str.append("l:" + this.getValueAsString(labelAttribute, fractionDigits, quoteNominal));
        }

        if (format == 0 && labelAttribute != null) {
            str.append(this.getValueAsString(labelAttribute, fractionDigits, quoteNominal));
        }

        return str.toString();
    }

    String getAttributesAsSparseString(String separator, String indexValueSeparator, int fractionDigits, boolean quoteNominal) {
        StringBuffer str = new StringBuffer();
        boolean first = true;
        int counter = 1;

        for(Iterator var8 = this.getAttributes().iterator(); var8.hasNext(); ++counter) {
            IAttribute attribute = (IAttribute)var8.next();
            double value = this.getValue(attribute);
            if (!Tools.isDefault(attribute.getDefault(), value)) {
                if (!first) {
                    str.append(separator);
                }

                first = false;
                str.append(counter + indexValueSeparator + this.getValueAsString(attribute, fractionDigits, quoteNominal));
            }
        }

        return str.toString();
    }

    public Object get(Object key) {
        IAttribute attribute = null;
        if (key instanceof String) {
            attribute = this.parentExampleSet.getAttributes().get((String)key);
        }

        double value = this.getValue(attribute);
        if (Double.isNaN(value)) {
            return "?";
        } else if (attribute == null) {
            return null;
        } else if (attribute.isNominal()) {
            return this.getValueAsString(attribute);
        } else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), 3)) {
            return (int)this.getValue(attribute);
        } else {
            return Ontology.ATTRIBUTE_VALUE_TYPE.isA(attribute.getValueType(), 9) ? new Date((long)this.getValue(attribute)) : this.getValue(attribute);
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
                this.setValue(attribute, (double)attribute.getMapping().mapString(value.toString()));
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
            attribute = this.parentExampleSet.getAttributes().get((String)key);
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
        return this.parentExampleSet.getAttributes().allSize() == 0;
    }

    public Set<String> keySet() {
        Set<String> allKeys = new HashSet();
        Iterator<IAttribute> a = this.parentExampleSet.getAttributes().allAttributes();

        while(a.hasNext()) {
            allKeys.add(((IAttribute)a.next()).getName());
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
        return this.parentExampleSet.getAttributes().allSize();
    }

    public Collection<Object> values() {
        throw new UnsupportedOperationException("Values is not supported by Example.");
    }
}