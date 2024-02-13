package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.AttributeTypeException;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IAttributeTransformation;
import adaa.analytics.rules.rm.example.table.INominalMapping;

public class FullAttributeTransformationRemapping implements IAttributeTransformation {
    private static final long serialVersionUID = 1L;
    private INominalMapping baseMapping;

    public FullAttributeTransformationRemapping(INominalMapping baseMapping) {
        this.baseMapping = baseMapping;
    }

    public FullAttributeTransformationRemapping(FullAttributeTransformationRemapping other) {
        this.baseMapping = (INominalMapping)other.baseMapping.clone();
    }

    public Object clone() {
        return new FullAttributeTransformationRemapping(this);
    }

    public void setNominalMapping(INominalMapping mapping) {
        this.baseMapping = mapping;
    }

    public double transform(IAttribute attribute, double value) {
        if (Double.isNaN(value)) {
            return value;
        } else if (attribute.isNominal()) {
            try {
                String nominalValue = this.baseMapping.mapIndex((int)value);
                int index = attribute.getMapping().getIndex(nominalValue);
                return index < 0 ? Double.NaN : (double)index;
            } catch (AttributeTypeException var6) {
                return Double.NaN;
            }
        } else {
            return value;
        }
    }

    public double inverseTransform(IAttribute attribute, double value) {
        if (Double.isNaN(value)) {
            return value;
        } else if (attribute.isNominal()) {
            try {
                String nominalValue = attribute.getMapping().mapIndex((int)value);
                int newValue = this.baseMapping.getIndex(nominalValue);
                return newValue < 0 ? value : (double)newValue;
            } catch (AttributeTypeException var6) {
                return value;
            }
        } else {
            return value;
        }
    }

    public boolean isReversable() {
        return true;
    }
}
