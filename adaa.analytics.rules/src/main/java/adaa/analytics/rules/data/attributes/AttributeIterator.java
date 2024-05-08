package adaa.analytics.rules.data.attributes;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.metadata.ColumnMetaData;
import adaa.analytics.rules.data.IAttribute;

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
        return iColumnMetaData.next();
    }
}
