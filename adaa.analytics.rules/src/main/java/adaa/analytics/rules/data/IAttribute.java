package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.EStatisticType;

import java.io.Serializable;

public interface IAttribute extends Cloneable, Serializable {

    boolean equals(Object var1);

    int hashCode();

    Object clone();

    String getName();

    void setName(String var1);

    int getTableIndex();

    INominalMapping getMapping();

    int getValueType();

    String toString();

    boolean isNominal();

    boolean isNumerical();

    String getAsString(double value);

    String getAsString(double value, boolean quoteNominal);

    void setRole(String role);

    String getRole();

    void recalculateStatistics();

    double getStatistic(EStatisticType statType);
}
