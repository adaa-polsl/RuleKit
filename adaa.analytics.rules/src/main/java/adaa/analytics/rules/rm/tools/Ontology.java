package adaa.analytics.rules.rm.tools;

public class Ontology {
    public static final int VALUE_TYPE = 0;
    public static final int BLOCK_TYPE = 1;
    private final int[] parentId;
    private final String[] names;
    public static final int NO_PARENT = -1;
    public static final int ATTRIBUTE_VALUE = 0;
    public static final int NOMINAL = 1;
    public static final int NUMERICAL = 2;
    public static final int INTEGER = 3;
    public static final int REAL = 4;
    public static final int STRING = 5;
    public static final int BINOMINAL = 6;
    public static final int POLYNOMINAL = 7;
    public static final int FILE_PATH = 8;
    public static final int DATE_TIME = 9;
    public static final int DATE = 10;
    public static final int TIME = 11;
    public static final String[] VALUE_TYPE_NAMES = new String[]{"attribute_value", "nominal", "numeric", "integer", "real", "text", "binominal", "polynominal", "file_path", "date_time", "date", "time"};
    public static final Ontology ATTRIBUTE_VALUE_TYPE;
    public static final int ATTRIBUTE_BLOCK = 0;
    public static final int SINGLE_VALUE = 1;
    public static final int VALUE_SERIES = 2;
    public static final int VALUE_SERIES_START = 3;
    public static final int VALUE_SERIES_END = 4;
    public static final int VALUE_MATRIX = 5;
    public static final int VALUE_MATRIX_START = 6;
    public static final int VALUE_MATRIX_END = 7;
    public static final int VALUE_MATRIX_ROW_START = 8;
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

    public int mapName(String name) {
        for(int i = 0; i < this.names.length; ++i) {
            if (this.names[i].equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public String mapIndex(int index) {
        return index >= 0 && index < this.names.length ? this.names[index] : null;
    }

    public String mapIndexToDisplayName(int index) {
        String valueTypeString = this.mapIndex(index);
        valueTypeString = valueTypeString.replaceAll("_", " ");
        valueTypeString = String.valueOf(valueTypeString.charAt(0)).toUpperCase() + valueTypeString.substring(1);
        return valueTypeString;
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
