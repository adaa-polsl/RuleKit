package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.ColumnMetaData;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.metadata.ColumnMetadataMap;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;

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
