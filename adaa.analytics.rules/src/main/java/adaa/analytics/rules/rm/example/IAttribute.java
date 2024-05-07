package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.data.ColumnMetaData;
import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.example.table.INominalMapping;

import java.io.Serializable;

public interface IAttribute extends Cloneable, Serializable {

    boolean equals(Object var1);

    int hashCode();

    Object clone();

    String getName();

    void setName(String var1);

    int getTableIndex();

    INominalMapping getMapping();

    void setValue(DataRow var1, double var2);

    int getValueType();

    String toString();

    boolean isNominal();

    boolean isNumerical();

    String getAsString(double value);

    String getAsString(double value, int fractionDigits, boolean quoteNominal);

    ColumnMetaData getColumnMetaData();
}
