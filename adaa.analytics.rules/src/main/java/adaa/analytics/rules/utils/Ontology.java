package adaa.analytics.rules.utils;

public class Ontology {
    private final int[] parentId;
    private final String[] names;
    public static final int ATTRIBUTE_VALUE = 0;
    public static final int NOMINAL = 1;
    public static final int NUMERICAL = 2;
    public static final int REAL = 4;
    public static final int STRING = 5;
    public static final String[] VALUE_TYPE_NAMES = new String[]{"attribute_value", "nominal", "numeric", "integer", "real", "text", "binominal", "polynominal", "file_path", "date_time", "date", "time"};
    public static final Ontology ATTRIBUTE_VALUE_TYPE;
    public static final String[] BLOCK_TYPE_NAMES;
    public static final Ontology ATTRIBUTE_BLOCK_TYPE;

    private Ontology(int[] parents, String[] names) {
        this.parentId = parents;
        this.names = names;
    }

    public boolean isA(int child, int parent) {
        while(true) {
            if (child != parent) {
                child = this.parentId[child];
                if (child != -1) {
                    continue;
                }

                return false;
            }

            return true;
        }
    }


    public String mapIndex(int index) {
        return index >= 0 && index < this.names.length ? this.names[index] : null;
    }


    public String[] getNames() {
        return this.names;
    }

    public int getParent(int child) {
        return this.parentId[child];
    }

    static {
        ATTRIBUTE_VALUE_TYPE = new Ontology(new int[]{-1, 0, 0, 2, 2, 1, 1, 1, 1, 0, 9, 9}, VALUE_TYPE_NAMES);
        BLOCK_TYPE_NAMES = new String[]{"attribute_block", "single_value", "value_series", "value_series_start", "value_series_end", "value_matrix", "value_matrix_start", "value_matrix_end", "value_matrix_row_start"};
        ATTRIBUTE_BLOCK_TYPE = new Ontology(new int[]{-1, 0, 0, 2, 2, 0, 5, 5, 5}, BLOCK_TYPE_NAMES);
    }
}
