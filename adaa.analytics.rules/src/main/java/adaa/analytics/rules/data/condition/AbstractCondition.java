package adaa.analytics.rules.data.condition;

public abstract class AbstractCondition implements ICondition {

    public enum EComparisonOperator {
        EQUALS,
        NOT_EQUAL,
        LESS,
        GREATER,
        LESS_EQUAL,
        GREATER_EQUAL
    }

    protected String colName;
    protected AbstractCondition.EComparisonOperator compOperator;
    protected Object value;
}
