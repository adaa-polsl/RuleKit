package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.rm.example.table.DataRow;
import adaa.analytics.rules.rm.example.table.INominalMapping;
//import adaa.analytics.rules.rm.operator.Annotations;

import java.io.Serializable;
import java.util.Iterator;

public interface IAttribute extends Cloneable, Serializable {
    int UNDEFINED_ATTRIBUTE_INDEX = -1;
    int VIEW_ATTRIBUTE_INDEX = -2;
    String MISSING_NOMINAL_VALUE = "?";

    boolean equals(Object var1);

    int hashCode();

    Object clone();

    String getName();

    void setName(String var1);

    int getTableIndex();

    void setTableIndex(int var1);

    void addOwner(IAttributes var1);

    void removeOwner(IAttributes var1);

    double getValue(DataRow var1);

    void setValue(DataRow var1, double var2);

    void addTransformation(IAttributeTransformation var1);

    IAttributeTransformation getLastTransformation();

    void clearTransformations();

    Iterator<IStatistics> getAllStatistics();

    void registerStatistics(IStatistics var1);

    /** @deprecated */
    @Deprecated
    double getStatistics(String var1);

    /** @deprecated */
    @Deprecated
    double getStatistics(String var1, String var2);

    String getConstruction();

    void setConstruction(String var1);

    INominalMapping getMapping();

    void setMapping(INominalMapping var1);

    int getBlockType();

    void setBlockType(int var1);

    int getValueType();

    String toString();

    void setDefault(double var1);

    double getDefault();

    boolean isNominal();

    boolean isNumerical();

    boolean isDateTime();

    String getAsString(double var1, int var3, boolean var4);

//    Annotations getAnnotations();
}
