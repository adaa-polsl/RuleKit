package adaa.analytics.rules.rm.example;

public class UnknownStatistics implements IStatistics {
    private static final long serialVersionUID = 217609774484151520L;
    private int unknownCounter = 0;

    public UnknownStatistics() {
    }

    private UnknownStatistics(UnknownStatistics other) {
        this.unknownCounter = other.unknownCounter;
    }

    public Object clone() {
        return new UnknownStatistics(this);
    }

    public void startCounting(IAttribute attribute) {
        this.unknownCounter = 0;
    }

    public void count(double value, double weight) {
        if (Double.isNaN(value)) {
            ++this.unknownCounter;
        }

    }

    public double getStatistics(IAttribute attribute, String statisticsName, String parameter) {
        if ("unknown".equals(statisticsName)) {
            return (double)this.unknownCounter;
        } else {
            // @TODO Logi
//            LogService.getRoot().log(Level.WARNING, "com.rapidminer.example.UnknownStatistics.calculating_statistics_unknown_type_error", statisticsName);
            return Double.NaN;
        }
    }

    public boolean handleStatistics(String statisticsName) {
        return "unknown".equals(statisticsName);
    }

    public String toString() {
        return "unknown: " + this.unknownCounter;
    }
}
