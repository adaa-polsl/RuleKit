package adaa.analytics.rules.data;

import java.io.Serializable;
import java.util.Iterator;

public interface IAttributes  extends Iterable<IAttribute>,  Serializable {

    Iterator<IAttribute> allAttributes();

    int regularSize();

    void setRegularRole(IAttribute var1);

    void removeRegularRole(IAttribute var1);

    IAttribute get(String columnName);

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

    IAttribute getColumnByRoleUnsafe(String role);

    IAttribute getLabelUnsafe();
}