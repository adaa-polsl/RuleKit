package adaa.analytics.rules.data;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;

public class DataColumnNominalAdapter implements IDataColumnAdapter{


    private StringColumn colStr;

    private IAttribute attr;

    private double defaultValue;

    public DataColumnNominalAdapter(IAttribute attr, StringColumn colStr, double defaultValue)
    {
        this.attr = attr;
        this.defaultValue = defaultValue;
        this.colStr = colStr;
    }

    public double getDoubleValue(int rowIndex)
    {
        String value = colStr.get(rowIndex);
        Integer iVal = attr.getMapping().getIndex(value);
        return iVal == null ? defaultValue : iVal.doubleValue();
    }
}
