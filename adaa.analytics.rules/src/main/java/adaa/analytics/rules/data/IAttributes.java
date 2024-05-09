package adaa.analytics.rules.data;

import java.io.Serializable;
import java.util.Iterator;

public interface IAttributes  extends Iterable<IAttribute>,  Serializable {
    String CONFIDENCE_NAME = "confidence";
    String ATTRIBUTE_NAME = "attribute";
    String ID_NAME = "id";
    String LABEL_NAME = "label";
    String PREDICTION_NAME = "prediction";
    String CLUSTER_NAME = "cluster";
    String WEIGHT_NAME = "weight";
    String BATCH_NAME = "batch";
    String OUTLIER_NAME = "outlier";
    String CLASSIFICATION_COST = "cost";
    String BASE_VALUE = "base_value";


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

}