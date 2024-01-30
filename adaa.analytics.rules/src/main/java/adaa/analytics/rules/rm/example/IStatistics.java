package adaa.analytics.rules.rm.example;

import java.io.Serializable;

public interface IStatistics extends Serializable, Cloneable {
    String UNKNOWN = "unknown";
    String AVERAGE = "average";
    String AVERAGE_WEIGHTED = "average_weighted";
    String VARIANCE = "variance";
    String VARIANCE_WEIGHTED = "variance_weighted";
    String MINIMUM = "minimum";
    String MAXIMUM = "maximum";
    String MODE = "mode";
    String LEAST = "least";
    String COUNT = "count";
    String SUM = "sum";
    String SUM_WEIGHTED = "sum_weighted";

    Object clone();

    void startCounting(IAttribute var1);

    void count(double var1, double var3);

    boolean handleStatistics(String var1);

    double getStatistics(IAttribute var1, String var2, String var3);
}
