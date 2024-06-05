package adaa.analytics.rules.data.condition;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

public class StringCondition extends AbstractCondition {

    public StringCondition(String colName, EComparisonOperator compOperator, Double value) {
        this.colName = colName;
        this.compOperator = compOperator;
        this.value = value;
    }

    public Selection createSelection(Table table) {
        DoubleColumn   strCol = table.doubleColumn(colName);
        Selection selection = null;
        switch(compOperator) {
            case EQUALS:
                selection = strCol.isEqualTo((Double)value);
                break;
            case NOT_EQUAL:
                selection = strCol.isNotEqualTo((Double)value);
                break;
        }
        return selection;
    }
}
