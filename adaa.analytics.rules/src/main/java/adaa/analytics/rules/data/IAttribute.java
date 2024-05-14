package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.EColumnType;
import adaa.analytics.rules.data.metadata.EStatisticType;

import java.io.Serializable;

public interface IAttribute extends Cloneable, Serializable {


    Object clone();

    String getName();

    void setName(String var1);

    int getTableIndex();

    INominalMapping getMapping();

    EColumnType getColumnType();


    boolean isNominal();

    boolean isNumerical();

    boolean isDate();

    String getAsString(double value);

    void setRole(String role);

    String getRole();

    void recalculateStatistics();

    double getStatistic(EStatisticType statType);
}
