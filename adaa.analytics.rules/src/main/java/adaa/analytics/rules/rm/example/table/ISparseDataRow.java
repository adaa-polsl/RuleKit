package adaa.analytics.rules.rm.example.table;

public interface ISparseDataRow {
    int[] getNonDefaultIndices();

    double[] getNonDefaultValues();
}
