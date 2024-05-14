package adaa.analytics.rules.data;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;

public class DataColumnEmptyAdapter implements IDataColumnAdapter{
    private double defaultValue;

    public DataColumnEmptyAdapter( double defaultValue)
    {
        this.defaultValue = defaultValue;
    }
    public double getDoubleValue(int rowIndex)
    {

        return defaultValue;
    }
}
