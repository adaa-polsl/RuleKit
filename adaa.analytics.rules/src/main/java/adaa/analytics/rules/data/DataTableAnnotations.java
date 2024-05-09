package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.ColumnMetaData;
import adaa.analytics.rules.data.metadata.ColumnMetadataMap;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataTableAnnotations implements Serializable, Cloneable {

    private Map<String, String> annotations = new LinkedHashMap<>();


    public void setAnnotation(String key, String value) {
        annotations.put(key, value);
    }

    public String getAnnotation(String key) {
        return annotations.get(key);
    }

    public boolean containsKey(String key) {
        return annotations.containsKey(key);
    }

    public DataTableAnnotations clone()
    {
        DataTableAnnotations cloned = new DataTableAnnotations();

        new LinkedHashMap<>();
        for(Map.Entry<String, String> entry : annotations.entrySet()) {
            cloned.annotations.put(entry.getKey(), entry.getValue());
        }
        return cloned;
    }
}
