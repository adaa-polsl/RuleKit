package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.ColumnMetaData;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.metadata.ColumnMetadataMap;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

import java.util.Map;

public class DataColumnDoubleAdapter {


    private DoubleColumn colNum;

    private StringColumn colStr;

    private IAttribute attr;

    private double defaultValue;

    public DataColumnDoubleAdapter(IAttribute attr, DoubleColumn colNum, StringColumn colStr, double defaultValue)
    {
        this.attr = attr;
        this.defaultValue = defaultValue;
        this.colStr = colStr;
        this.colNum = colNum;
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
        if(attr == null) {
            return defaultValue;
        }
        if (colStr!=null) {
            String value = colStr.get(rowIndex);
            Integer iVal = attr.getMapping().getIndex(value);
            return iVal == null ? defaultValue : iVal.doubleValue();
        }else if (colNum!=null)
        {
            return colNum.getDouble(rowIndex);
        }
        return defaultValue;
    }
}
