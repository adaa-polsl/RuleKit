package adaa.analytics.rules.data;

import adaa.analytics.rules.data.metadata.EColumnRole;

import java.io.Serializable;
import java.util.Iterator;

public interface IAttributes  extends Iterable<IAttribute>,  Serializable {


    boolean equals(Object var1);

    int hashCode();

    Iterator<IAttribute> allAttributes();

    boolean contains(IAttribute var1);

    int regularSize();

    int allSize();

    void setRegularRole(IAttribute var1);

    boolean removeRegularRole(IAttribute var1);

    IAttribute get(String columnName);

    IAttribute get(String columnName, boolean caseSensitive);

    IAttribute getRegular(String columnName);

    IAttribute getColumnByRole(String roleName);

    IAttribute getLabel();

    void setLabel(IAttribute var1);

    IAttribute getPredictedLabel();

    IAttribute getConfidence(String var1);

    void setPredictedLabel(IAttribute var1);

    IAttribute getWeight();

    IAttribute getCost();

    void setSpecialAttribute(IAttribute var1, String role);

    String toString();

    String findRoleBySpecialName(String role);

    IAttribute getColumnByRoleUnsafe(String role);

    IAttribute getLabelUnsafe();
}