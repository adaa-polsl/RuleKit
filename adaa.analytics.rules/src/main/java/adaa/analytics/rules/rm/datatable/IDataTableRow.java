package adaa.analytics.rules.rm.datatable;

public interface IDataTableRow {
    String getId();

    double getValue(int var1);

    int getNumberOfValues();
}
