package adaa.analytics.rules.data.metadata;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.columns.Column;

import java.io.Serializable;

public enum EColumnType implements Serializable {
    OTHER,
    NOMINAL,
    NUMERICAL,
    DATE;

    public static EColumnType fromTsColumn(Column<?> column) {

        if(column.type() == ColumnType.DOUBLE ||
                column.type() == ColumnType.FLOAT ||
                column.type() == ColumnType.LONG ||
                column.type() == ColumnType.INTEGER ||
                column.type() == ColumnType.BOOLEAN ||
                column.type() == ColumnType.SHORT) {

            return EColumnType.NUMERICAL;
        }
        else if(column.type() == ColumnType.TEXT ||
                column.type() == ColumnType.STRING) {

            return EColumnType.NOMINAL;
        }
        else if(column.type() == ColumnType.LOCAL_DATE_TIME ||
                column.type() == ColumnType.LOCAL_DATE ||
                column.type() == ColumnType.LOCAL_TIME) {

            return EColumnType.DATE;
        }

        return EColumnType.OTHER;
    }
}
