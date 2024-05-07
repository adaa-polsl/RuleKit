package adaa.analytics.rules.data;

import adaa.analytics.rules.rm.example.IAttribute;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Map;

public class DataColumnDoubleAdapter {

    private ColumnMetaData colMetaData;

    private DoubleColumn colNum;

    private StringColumn colStr;

    private IAttribute attr;

    private double defaultValue;

    public DataColumnDoubleAdapter(Table table, Map<String, ColumnMetaData> columnMetaData, IAttribute attr, double defaultValue)
    {
        this.attr = attr;
        this.defaultValue = defaultValue;
        String colName = attr!=null?attr.getName():null;
        if (colName!=null)
            this.colMetaData = columnMetaData.get(colName);
        if (colMetaData!=null) {
            if (colMetaData.isNominal()) {
                this.colStr = table.stringColumn(colName);

            }else
            {
                this.colNum = (DoubleColumn) table.column(colName);
            }
        }


    }

    public String getNominalValue(int rowIndex) {
        if (!attr.isNominal()) {
            throw new IllegalStateException("Extraction of nominal example value for non-nominal attribute '" + attr.getName() + "' is not possible.");
        } else {
            double value = getDoubleValue(rowIndex);
            return Double.isNaN(value) ? "?" : attr.getMapping().mapIndex((int)value);
        }
    }

    public double getDoubleValue(int rowIndex)
    {
        if(colMetaData == null) {
            return defaultValue;
        }
        if (colStr!=null) {
            String value = colStr.get(rowIndex);
            Integer iVal = colMetaData.getMapping().getIndex(value);
            return iVal == null ? defaultValue : iVal.doubleValue();
        }else if (colNum!=null)
        {
            return colNum.getDouble(rowIndex);
        }
        return defaultValue;
    }
}
