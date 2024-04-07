package adaa.analytics.rules.rm.comp;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.table.IExampleTable;

public class TsExampleTable implements IExampleTable {

    private DataTable dataTable;

    public TsExampleTable(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    @Override
    public int addAttribute(IAttribute var1) {
        dataTable.addNewColumn(var1.getColumnMetaData());
        return dataTable.getColumnIndex(var1.getName());
    }
}
