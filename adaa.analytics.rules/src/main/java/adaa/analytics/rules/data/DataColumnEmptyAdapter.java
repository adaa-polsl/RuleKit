package adaa.analytics.rules.data;

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
