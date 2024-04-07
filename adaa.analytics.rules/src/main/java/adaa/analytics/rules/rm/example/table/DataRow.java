package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.example.IAttribute;

import java.io.Serializable;

public class DataRow implements Serializable {
    private DataTable dataTable;
    private int rowIndex;

    public DataRow(DataTable dataTable, int rowIndex) {
        this.dataTable = dataTable;
        this.rowIndex = rowIndex;
    }

    public String toString(){
        return "";
    }

    public double get(IAttribute attribute) {
        if (attribute == null) {
            return Double.NaN;
        } else {
            try {
                return dataTable.getDoubleValue(attribute.getName(), rowIndex, 0.0);
            } catch (ArrayIndexOutOfBoundsException var3) {
                throw new ArrayIndexOutOfBoundsException("DataRow: table index " + attribute.getTableIndex() + " of Attribute " + attribute.getName() + " is out of bounds.");
            }
        }
    }

    public void set(IAttribute attribute, double value) {
        dataTable.setDoubleValue(attribute.getName(), rowIndex, value);
    }
}
