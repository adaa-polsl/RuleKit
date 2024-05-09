package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.IAttribute;

import java.util.Iterator;

public class AttributeIterator implements Iterator<IAttribute> {

    private Iterator<ColumnMetaData> iColumnMetaData;

    public AttributeIterator(ColumnMetadataMap columnMetadataMap) {
        iColumnMetaData = columnMetadataMap.getAllColumnMetaData().iterator();
    }

    public AttributeIterator(ColumnMetadataMap columnMetadataMap, String eColRole) {
        //TODO filtrowanie po eColRole - chyba zrobione
        //n new ColumnMetadataMap(columnMetaData.values().stream().filter(c -> c.getRole() == eColRole).collect(Collectors.toList()));
//        ColumnMetadataMap columnMetadataMap
        iColumnMetaData = columnMetadataMap.getColumnsByRole(eColRole).iterator();
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
