package adaa.analytics.rules.data;

import java.io.Serializable;

public interface IStatistics extends Serializable {
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

}
