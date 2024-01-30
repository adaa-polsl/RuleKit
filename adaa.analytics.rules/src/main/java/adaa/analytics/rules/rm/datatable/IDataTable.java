package adaa.analytics.rules.rm.datatable;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

public interface IDataTable extends Iterable<IDataTableRow> {
    boolean isNominal(int var1);

    boolean isTime(int var1);

    boolean isDate(int var1);

    boolean isDateTime(int var1);

    boolean isNumerical(int var1);

    String mapIndex(int var1, int var2);

    int mapString(int var1, String var2);

    String getColumnName(int var1);

    int getColumnIndex(String var1);

    double getColumnWeight(int var1);

    boolean isSupportingColumnWeights();

    int getNumberOfColumns();

    int getNumberOfSpecialColumns();

    boolean isSpecial(int var1);

    String[] getColumnNames();

    String getName();

    void setName(String var1);

    void add(IDataTableRow var1);

    Iterator<IDataTableRow> iterator();

    IDataTableRow getRow(int var1);

    int getNumberOfRows();

    int getNumberOfValues(int var1);

    String getValueAsString(IDataTableRow var1, int var2);

//    void addDataTableListener(DataTableListener var1);
//
//    void addDataTableListener(DataTableListener var1, boolean var2);
//
//    void removeDataTableListener(DataTableListener var1);

    void write(PrintWriter var1) throws IOException;

    IDataTable sample(int var1);

    boolean containsMissingValues();

    boolean isDeselected(String var1);

//    void setSelection(AbstractChartPanel.Selection var1);

    int getSelectionCount();
}
