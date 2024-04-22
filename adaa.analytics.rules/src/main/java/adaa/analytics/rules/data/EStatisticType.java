package adaa.analytics.rules.data;

import java.io.Serializable;

public enum EStatisticType implements Serializable {
    AVERAGE("average"),
    AVERAGE_WEIGHTED("average_weighted"),
    VARIANCE("variance"),
    MINIMUM("minimum"),
    MAXIMUM("maximum");

    private String stat;

    EStatisticType(String stat) {
        this.stat = stat;
    }

    public String getStatistic() {
        return stat;
    }

    public static EStatisticType fromString(String text) {
        for (EStatisticType b : EStatisticType.values()) {
            if (b.stat.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
