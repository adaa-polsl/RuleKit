package adaa.analytics.rules.data;

import java.io.Serializable;
import java.util.List;

public interface INominalMapping extends Cloneable, Serializable {
    boolean equals(INominalMapping var1);

    Object clone();

    boolean hasIndex(int index);

    String getPositiveString();

    String getValue(int index);

    Integer getIndex(String var1);

    Integer mapString(String var1);

    String mapIndex(int var1);

    List<String> getValues();

    int size();

    void clear();

}
