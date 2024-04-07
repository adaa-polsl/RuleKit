package adaa.analytics.rules.rm.example;

import java.io.Serializable;
import java.util.Iterator;

public interface IAttributes  extends Iterable<IAttribute>, Cloneable, Serializable {
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

    Object clone();

    boolean equals(Object var1);

    int hashCode();

    Iterator<IAttribute> iterator();

    Iterator<IAttribute> allAttributes();

    boolean contains(IAttribute var1);

    int size();

    int allSize();

    void addRegular(IAttribute var1);

    boolean remove(IAttribute var1);

    IAttribute get(String var1);

    IAttribute get(String var1, boolean var2);

    IAttribute getRegular(String var1);

    IAttribute getSpecial(String var1);

    IAttribute getLabel();

    void setLabel(IAttribute var1);

    IAttribute getPredictedLabel();

    IAttribute getConfidence(String var1);

    void setPredictedLabel(IAttribute var1);

    IAttribute getWeight();

    IAttribute getCost();

    void setSpecialAttribute(IAttribute var1, String var2);

    String toString();

    String findRoleBySpecialName(String var1);
}