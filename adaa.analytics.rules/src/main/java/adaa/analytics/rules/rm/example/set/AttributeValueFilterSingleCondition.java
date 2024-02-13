package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.tools.Tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.regex.PatternSyntaxException;

public class AttributeValueFilterSingleCondition implements ICondition {
    private static final long serialVersionUID = 1537763901048986863L;
    private static final String[] COMPARISON_TYPES = new String[]{"<=", ">=", "!=", "<>", "=", "<", ">"};
    private static final String MISSING_ENCODING = "\\?";
    public static final int LEQ = 0;
    public static final int GEQ = 1;
    public static final int NEQ1 = 2;
    public static final int NEQ2 = 3;
    public static final int EQUALS = 4;
    public static final int LESS = 5;
    public static final int GREATER = 6;
    public static String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
    private int comparisonType = 4;
    private IAttribute attribute;
    private double numericalValue;
    private String nominalValue;
    private Date dateValue;
    private HashSet<Integer> allowedNominalValueIndices;
    private boolean isMissingAllowed = false;

    public AttributeValueFilterSingleCondition(IAttribute attribute, int comparisonType, String value) {
        this.attribute = attribute;
        this.comparisonType = comparisonType;
        this.setValue(value);
    }

    public AttributeValueFilterSingleCondition(IExampleSet exampleSet, String parameterString) {
        if (parameterString != null && parameterString.length() != 0) {
            int compIndex = -1;

            for(this.comparisonType = 0; this.comparisonType < COMPARISON_TYPES.length; ++this.comparisonType) {
                compIndex = parameterString.indexOf(COMPARISON_TYPES[this.comparisonType]);
                if (compIndex != -1) {
                    break;
                }
            }

            if (compIndex == -1) {
                throw new IllegalArgumentException("Parameter string must have the form 'attribute {=|<|>|<=|>=|!=} value'");
            } else {
                String attName = parameterString.substring(0, compIndex).trim();
                String valueStr = parameterString.substring(compIndex + COMPARISON_TYPES[this.comparisonType].length()).trim();
                if (attName.length() != 0 && valueStr.length() != 0) {
                    this.attribute = exampleSet.getAttributes().get(attName);
                    if (this.attribute == null) {
                        throw new IllegalArgumentException("Unknown attribute: '" + attName + "'");
                    } else {
                        this.setValue(valueStr);
                    }
                } else {
                    throw new IllegalArgumentException("Parameter string must have the form 'attribute {=|<|>|<=|>=|!=} value'");
                }
            }
        } else {
            throw new IllegalArgumentException("Parameter string must not be empty!");
        }
    }

    private void setValue(String value) {
        if (this.attribute.isNominal()) {
            if (this.comparisonType != 4 && this.comparisonType != 2 && this.comparisonType != 3) {
                throw new IllegalArgumentException("For nominal attributes only '=' and '!=' or '<>' is allowed!");
            }

            this.nominalValue = value;
            this.isMissingAllowed = this.nominalValue.equals("\\?");
            this.allowedNominalValueIndices = new HashSet(this.attribute.getMapping().size());
            Iterator var2 = this.attribute.getMapping().getValues().iterator();

            while(var2.hasNext()) {
                String attributeValue = (String)var2.next();

                try {
                    if (attributeValue.equals(this.nominalValue) || attributeValue.matches(this.nominalValue)) {
                        this.allowedNominalValueIndices.add(this.attribute.getMapping().mapString(attributeValue));
                    }
                } catch (PatternSyntaxException var7) {
                }
            }
        } else if (this.attribute.isNumerical()) {
            if (value.equals("?")) {
                this.numericalValue = Double.NaN;
            } else {
                try {
                    this.numericalValue = Double.parseDouble(value);
                } catch (NumberFormatException var6) {
                    throw new IllegalArgumentException("Value for attribute '" + this.attribute.getName() + "' must be numerical, but was '" + value + "'!");
                }
            }
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);

            try {
                if (value.equals("?")) {
                    this.dateValue = null;
                } else {
                    this.dateValue = dateFormat.parse(value);
                }
            } catch (ParseException var5) {
                throw new IllegalArgumentException("Could not parse value '" + value + "' with date pattern " + DATE_PATTERN);
            }
        }

    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public String toString() {
        return this.attribute.getName() + " " + COMPARISON_TYPES[this.comparisonType] + " " + (this.attribute.isNominal() ? this.nominalValue : "" + this.numericalValue);
    }

    public boolean conditionOk(Example e) {
        if (this.attribute.isNominal()) {
            double doubleValue = e.getValue(this.attribute);
            if (Double.isNaN(doubleValue)) {
                switch (this.comparisonType) {
                    case 2:
                    case 3:
                        return !this.isMissingAllowed;
                    case 4:
                        return this.isMissingAllowed;
                    default:
                        return false;
                }
            } else {
                int value = (int)doubleValue;
                switch (this.comparisonType) {
                    case 2:
                    case 3:
                        return !this.allowedNominalValueIndices.contains(value);
                    case 4:
                        return this.allowedNominalValueIndices.contains(value);
                    default:
                        return false;
                }
            }
        } else if (this.attribute.isNumerical()) {
            switch (this.comparisonType) {
                case 0:
                    return Tools.isLessEqual(e.getNumericalValue(this.attribute), this.numericalValue);
                case 1:
                    return Tools.isGreaterEqual(e.getNumericalValue(this.attribute), this.numericalValue);
                case 2:
                case 3:
                    return Tools.isNotEqual(e.getNumericalValue(this.attribute), this.numericalValue);
                case 4:
                    return Tools.isEqual(e.getNumericalValue(this.attribute), this.numericalValue);
                case 5:
                    return Tools.isLess(e.getNumericalValue(this.attribute), this.numericalValue);
                case 6:
                    return Tools.isGreater(e.getNumericalValue(this.attribute), this.numericalValue);
                default:
                    return false;
            }
        } else {
            Date currentDateValue;
            if (Double.isNaN(e.getValue(this.attribute))) {
                currentDateValue = null;
            } else {
                currentDateValue = e.getDateValue(this.attribute);
            }

            switch (this.comparisonType) {
                case 0:
                    return Tools.isLessEqual(currentDateValue, this.dateValue);
                case 1:
                    return Tools.isGreaterEqual(currentDateValue, this.dateValue);
                case 2:
                case 3:
                    return Tools.isNotEqual(currentDateValue, this.dateValue);
                case 4:
                    return Tools.isEqual(currentDateValue, this.dateValue);
                case 5:
                    return Tools.isLess(currentDateValue, this.dateValue);
                case 6:
                    return Tools.isGreater(currentDateValue, this.dateValue);
                default:
                    return false;
            }
        }
    }
}
