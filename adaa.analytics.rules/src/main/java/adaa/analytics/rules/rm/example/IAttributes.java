package adaa.analytics.rules.rm.example;

import java.io.Serializable;
import java.util.Iterator;

public interface IAttributes  extends Iterable<IAttribute>, Cloneable, Serializable {
    int REGULAR = 0;
    int SPECIAL = 1;
    int ALL = 2;
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
    String[] KNOWN_ATTRIBUTE_TYPES = new String[]{"attribute", "label", "id", "weight", "batch", "cluster", "prediction", "outlier", "cost", "base_value"};
    int TYPE_ATTRIBUTE = 0;
    int TYPE_LABEL = 1;
    int TYPE_ID = 2;
    int TYPE_WEIGHT = 3;
    int TYPE_BATCH = 4;
    int TYPE_CLUSTER = 5;
    int TYPE_PREDICTION = 6;
    int TYPE_OUTLIER = 7;
    int TYPE_COST = 8;
    int TYPE_BASE_VALUE = 9;

    Object clone();

    boolean equals(Object var1);

    int hashCode();

    Iterator<IAttribute> iterator();

    Iterator<IAttribute> allAttributes();

    Iterator<AttributeRole> allAttributeRoles();

    Iterator<AttributeRole> specialAttributes();

    Iterator<AttributeRole> regularAttributes();

    boolean contains(IAttribute var1);

    int size();

    int specialSize();

    int allSize();

    void add(AttributeRole var1);

    void addRegular(IAttribute var1);

    boolean remove(AttributeRole var1);

    boolean remove(IAttribute var1);

    void clearRegular();

    void clearSpecial();

    IAttribute replace(IAttribute var1, IAttribute var2);

    IAttribute get(String var1);

    IAttribute get(String var1, boolean var2);

    IAttribute getRegular(String var1);

    IAttribute getSpecial(String var1);

    AttributeRole getRole(IAttribute var1);

    AttributeRole getRole(String var1);

    IAttribute getLabel();

    void setLabel(IAttribute var1);

    IAttribute getPredictedLabel();

    IAttribute getConfidence(String var1);

    void setPredictedLabel(IAttribute var1);

    IAttribute getId();

    void setId(IAttribute var1);

    IAttribute getWeight();

    void setWeight(IAttribute var1);

    IAttribute getCluster();

    void setCluster(IAttribute var1);

    IAttribute getOutlier();

    void setOutlier(IAttribute var1);

    IAttribute getCost();

    void setCost(IAttribute var1);

    void setSpecialAttribute(IAttribute var1, String var2);

    IAttribute[] createRegularAttributeArray();

    String toString();

    AttributeRole findRoleByName(String var1);

    AttributeRole findRoleByName(String var1, boolean var2);

    AttributeRole findRoleBySpecialName(String var1);

    AttributeRole findRoleBySpecialName(String var1, boolean var2);

    void rename(AttributeRole var1, String var2);

    void rename(IAttribute var1, String var2);
}