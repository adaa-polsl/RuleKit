package ioutils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.metadata.EColumnRole;
import adaa.analytics.rules.data.metadata.EColumnType;
import adaa.analytics.rules.data.IAttribute;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Row;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.util.List;

public abstract class TableSawLoader extends ExamplesetFileLoader {

    protected DataTable loadDataTable(
            CsvReadOptions.Builder builder,
            String labelParameterName,
            String survivalTimeParameter,
            List<AttributeInfo> attributesInfo
    ) {
        // TableSaw CSV builder for reading
        builder = builder
                .separator(',')
                .quoteChar('\'')
                .missingValueIndicator("?");

        DataTable dataTable = new DataTable(builder, attributesInfo);
        dataTable.setRole(labelParameterName, EColumnRole.label.name());
        dataTable.setRole(survivalTimeParameter, EColumnRole.survival_time.name());
        dataTable.convertStringColsToDouble();

        return dataTable;
    }

    protected EColumnType getCellType(@NotNull ColumnType type) {

        if (type.equals(ColumnType.INTEGER) ||
                type.equals(ColumnType.FLOAT) ||
                type.equals(ColumnType.DOUBLE) ||
                type.equals(ColumnType.SHORT) ||
                type.equals(ColumnType.LONG) ||
                type.equals(ColumnType.BOOLEAN)) {
            return EColumnType.NUMERICAL;
        }
        else if (type.equals(ColumnType.STRING) ||
                type.equals(ColumnType.TEXT)) {
            return EColumnType.NOMINAL;
        }
        else if (type.equals(ColumnType.LOCAL_DATE) ||
                type.equals(ColumnType.LOCAL_DATE_TIME) ||
                type.equals(ColumnType.LOCAL_TIME)) {
            return EColumnType.DATE;
        }
        return EColumnType.OTHER;
    }

    protected String [] rowToStringArray(@NotNull Row row, @NotNull IAttribute[] attributes) {

        String [] stringRow = new String[row.columnCount()];

        for(int i=0 ; i<attributes.length ; i++) {

            String colName = attributes[i].getName();
            if(getCellType(row.getColumnType(colName)) == EColumnType.NOMINAL) {

                stringRow[i] = row.getString(colName);
            }
            else if(getCellType(row.getColumnType(colName)) == EColumnType.NUMERICAL) {

                stringRow[i] = Double.toString(row.getNumber(colName));
            }
            else if(getCellType(row.getColumnType(colName)) == EColumnType.DATE) {

                stringRow[i] = Double.toString(row.getPackedDateTime(colName));
            }
        }

        return stringRow;
    }

//    protected DataRow rowToDataRow(Row row, IAttribute[] attributes) {
//
//        String [] strRow = rowToStringArray(row, attributes);
//
//        return dataRowFactory.create(strRow, attributes);
//    }
}
