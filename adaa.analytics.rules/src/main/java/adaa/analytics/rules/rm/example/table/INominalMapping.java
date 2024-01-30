package adaa.analytics.rules.rm.example.table;

import java.io.Serializable;
import java.util.List;

public interface INominalMapping extends Cloneable, Serializable {
    boolean equals(INominalMapping var1);

    Object clone();

    int getPositiveIndex();

    String getPositiveString();

    int getNegativeIndex();

    String getNegativeString();

    int getIndex(String var1);

    int mapString(String var1);

    String mapIndex(int var1);

    void setMapping(String var1, int var2);

    List<String> getValues();

    int size();

    void sortMappings();

    void clear();
}
