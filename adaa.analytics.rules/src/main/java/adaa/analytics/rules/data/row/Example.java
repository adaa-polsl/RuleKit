package adaa.analytics.rules.data.row;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;
import adaa.analytics.rules.data.IExampleSet;

import java.io.Serializable;

public class Example implements Serializable{

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
        return parentExampleSet.getDoubleValue(parentExampleSet.getColumnIndex(columnName), rowIndex);
    }

    public double getValue(IAttribute a) {
        if (a == null) {
            return Double.NaN;
        } else {
            return parentExampleSet.getDoubleValue(a.getTableIndex(), rowIndex);
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

    public double getLabelValue() {
        return this.getValue(this.parentExampleSet.getAttributes().getLabel());
    }

    public double getPredictedLabelValue() {
        return this.getValue(this.parentExampleSet.getAttributes().getPredictedLabel());
    }

    public double getWeightValue() {
        return this.getValue(this.parentExampleSet.getAttributes().getWeight());
    }

    public double getConfidenceValue(String classValue) {
        return this.getValue(this.parentExampleSet.getAttributes().getConfidence(classValue));
    }

    public String getValueAsString(IAttribute attribute) {
        double value = this.getValue(attribute);
        return attribute.getAsString(value);
    }
}
