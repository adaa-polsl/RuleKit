package adaa.analytics.rules.data;

import tech.tablesaw.api.DoubleColumn;

import java.util.Map;

public class DataColumnDoubleAdapter implements IDataColumnAdapter {


    private DoubleColumn colNum;


    public DataColumnDoubleAdapter(DoubleColumn colNum)
    {
        this.colNum = colNum;
    }

    public double getDoubleValue(int rowIndex)
    {
        return colNum.getDouble(rowIndex);
    }
}
