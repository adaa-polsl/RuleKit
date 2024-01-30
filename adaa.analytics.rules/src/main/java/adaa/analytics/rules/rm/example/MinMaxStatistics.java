package adaa.analytics.rules.rm.example;

import java.util.logging.Level;

public class MinMaxStatistics implements IStatistics {
    private static final long serialVersionUID = 1027895282018510951L;
    private double minimum = Double.POSITIVE_INFINITY;
    private double maximum = Double.NEGATIVE_INFINITY;

    public MinMaxStatistics() {
    }

    private MinMaxStatistics(MinMaxStatistics other) {
        this.minimum = other.minimum;
        this.maximum = other.maximum;
    }

    public Object clone() {
        return new MinMaxStatistics(this);
    }

    public void count(double value, double weight) {
        if (!Double.isNaN(value)) {
            if (this.minimum > value) {
                this.minimum = value;
            }

            if (this.maximum < value) {
                this.maximum = value;
            }
        }

    }

    public double getStatistics(IAttribute attribute, String name, String parameter) {
        if ("minimum".equals(name)) {
            return this.minimum;
        } else if ("maximum".equals(name)) {
            return this.maximum;
        } else {
            // @TODO Loger
//            LogService.getRoot().log(Level.WARNING, "com.rapidminer.example.MinMaxStatistics.calculating_statistics_error", name);
            return Double.NaN;
        }
    }

    public boolean handleStatistics(String name) {
        return "minimum".equals(name) || "maximum".equals(name);
    }

    public void startCounting(IAttribute attribute) {
        this.minimum = Double.POSITIVE_INFINITY;
        this.maximum = Double.NEGATIVE_INFINITY;
    }
}
