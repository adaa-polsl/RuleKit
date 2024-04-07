package adaa.analytics.rules.data.condition;

import tech.tablesaw.api.Table;
import tech.tablesaw.selection.Selection;

public interface ICondition {

    Selection createSelection(Table table);
}
