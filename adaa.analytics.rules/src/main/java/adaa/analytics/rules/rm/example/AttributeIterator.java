package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.data.ColumnMetaData;
import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.comp.TsAttribute;

import java.util.Iterator;

public class AttributeIterator implements Iterator<IAttribute> {

    private Iterator<ColumnMetaData> iColumnMetaData;

    public AttributeIterator(DataTable dataTable) {
        iColumnMetaData = dataTable.getMetaDataTable().iterator();
    }

    public AttributeIterator(DataTable dataTable, String eColRole) {
        iColumnMetaData = dataTable.getMetaDataTable(eColRole).iterator();
    }

    @Override
    public boolean hasNext() {
        return iColumnMetaData.hasNext();
    }

    @Override
    public IAttribute next() {
        return new TsAttribute(iColumnMetaData.next());
    }
}
