package adaa.analytics.rules.data.metadata;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.columns.Column;

import java.io.Serializable;

public enum EColumnType implements Serializable {
    OTHER,
    NOMINAL,
    NUMERICAL,
    DATE;


}
