package adaa.analytics.rules.data.condition;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

public class StringCondition extends AbstractCondition {

    public StringCondition(String colName, EComparisonOperator compOperator, String value) {
        this.colName = colName;
        this.compOperator = compOperator;
        this.value = value;
    }

    public Selection createSelection(Table table) {
        StringColumn strCol = table.stringColumn("");
        Selection selection = null;
        switch(compOperator) {
            case EQUALS:
                selection = strCol.isEqualTo((String)value);
                break;
            case NOT_EQUAL:
                selection = strCol.isNotEqualTo((String)value);
                break;
        }
        return selection;
    }
}
