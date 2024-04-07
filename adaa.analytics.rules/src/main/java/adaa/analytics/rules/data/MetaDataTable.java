package adaa.analytics.rules.data;

import java.util.*;

public class MetaDataTable {

    private Map<String, ColumnMetaData> attributeMetaData = new LinkedHashMap<>();

    MetaDataTable(Collection<ColumnMetaData> colMetaData) {
        for(ColumnMetaData cmd : colMetaData) {
            attributeMetaData.put(cmd.getName(), cmd);
        }
    }

    public Set<String> getColumnNames() {
        return attributeMetaData.keySet();
    }

    public ColumnMetaData getColumnMetaData(String colName) {
        return attributeMetaData.get(colName);
    }

    public Iterator<ColumnMetaData> iterator() {
        return attributeMetaData.values().iterator();
    }
}
