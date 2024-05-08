package adaa.analytics.rules.data;

public class DataTableAnnotations {

    private DataTable dataTable;

    public DataTableAnnotations(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void setAnnotation(String key, String value) {
        dataTable.setAnnotation(key, value);
    }

    public String getAnnotation(String key) {
        return dataTable.getAnnotation(key);
    }

    public int size() {
        return dataTable.sizeAnnotations();
    }

    public void clear() {
        dataTable.clearAnnotations();
    }

    public boolean containsKey(String key) {
        return dataTable.containsAnnotationKey(key);
    }

    public String get(Object key) {
        return key instanceof String ? this.getAnnotation((String)key) : null;
    }

    public String put(String key, String value) {
        this.setAnnotation(key, value);
        return value;
    }
}
