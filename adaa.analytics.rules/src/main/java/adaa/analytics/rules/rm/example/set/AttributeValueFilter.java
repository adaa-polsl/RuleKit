package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class AttributeValueFilter implements ICondition {
    private static final long serialVersionUID = 6977275837081172924L;
    private static final int AND = 0;
    private static final int OR = 1;
    private List<AttributeValueFilterSingleCondition> conditions = new LinkedList();
    private int combinationMode = 0;

    public AttributeValueFilter(IAttribute attribute, int comparisonType, String value) {
        this.addCondition(attribute, comparisonType, value);
    }

    public AttributeValueFilter(IExampleSet exampleSet, String parameterString) {
        if (parameterString != null && parameterString.length() != 0) {
            String[] splitted = parameterString.split("\\|\\|");
            String[] var4;
            int var5;
            int var6;
            String condition;
            if (splitted.length > 1) {
                var4 = splitted;
                var5 = splitted.length;

                for(var6 = 0; var6 < var5; ++var6) {
                    condition = var4[var6];
                    condition = condition.trim();
                    this.addCondition(new AttributeValueFilterSingleCondition(exampleSet, condition));
                }

                this.combinationMode = 1;
            } else {
                splitted = parameterString.split("\\&\\&");
                if (splitted.length > 1) {
                    var4 = splitted;
                    var5 = splitted.length;

                    for(var6 = 0; var6 < var5; ++var6) {
                        condition = var4[var6];
                        condition = condition.trim();
                        this.addCondition(new AttributeValueFilterSingleCondition(exampleSet, condition));
                    }

                    this.combinationMode = 0;
                } else {
                    this.addCondition(new AttributeValueFilterSingleCondition(exampleSet, parameterString));
                    this.combinationMode = 0;
                }
            }

        } else {
            throw new IllegalArgumentException("Parameter string must not be empty!");
        }
    }

    private void addCondition(IAttribute attribute, int comparisonType, String value) {
        this.addCondition(new AttributeValueFilterSingleCondition(attribute, comparisonType, value));
    }

    private void addCondition(AttributeValueFilterSingleCondition condition) {
        this.conditions.add(condition);
    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public String toString() {
        return this.conditions.toString();
    }

    public boolean conditionOk(Example e) {
        Iterator<AttributeValueFilterSingleCondition> i = this.conditions.iterator();

        while(i.hasNext()) {
            AttributeValueFilterSingleCondition condition = (AttributeValueFilterSingleCondition)i.next();
            if (this.combinationMode == 0) {
                if (!condition.conditionOk(e)) {
                    return false;
                }
            } else if (condition.conditionOk(e)) {
                return true;
            }
        }

        if (this.combinationMode == 0) {
            return true;
        } else {
            return false;
        }
    }
}
