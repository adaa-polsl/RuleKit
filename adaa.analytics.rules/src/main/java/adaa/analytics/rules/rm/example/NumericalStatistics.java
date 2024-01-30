package adaa.analytics.rules.rm.example;

public class NumericalStatistics implements IStatistics {
    private static final long serialVersionUID = -6283236022093847887L;
    private double sum = 0.0;
    private double squaredSum = 0.0;
    private int valueCounter = 0;

    public NumericalStatistics() {
    }

    private NumericalStatistics(NumericalStatistics other) {
        this.sum = other.sum;
        this.squaredSum = other.squaredSum;
        this.valueCounter = other.valueCounter;
    }

    public Object clone() {
        return new NumericalStatistics(this);
    }

    public void startCounting(IAttribute attribute) {
        this.sum = 0.0;
        this.squaredSum = 0.0;
        this.valueCounter = 0;
    }

    public void count(double value, double weight) {
        if (!Double.isNaN(value)) {
            this.sum += value;
            this.squaredSum += value * value;
            ++this.valueCounter;
        }

    }

    public boolean handleStatistics(String name) {
        return "average".equals(name) || "variance".equals(name) || "sum".equals(name);
    }

    public double getStatistics(IAttribute attribute, String name, String parameter) {
        if ("average".equals(name)) {
            return this.sum / (double)this.valueCounter;
        } else if ("variance".equals(name)) {
            if (this.valueCounter <= 1) {
                return 0.0;
            } else {
                double variance = (this.squaredSum - this.sum * this.sum / (double)this.valueCounter) / (double)(this.valueCounter - 1);
                return variance < 0.0 ? 0.0 : variance;
            }
        } else if ("sum".equals(name)) {
            return this.sum;
        } else {
            // @TODO Logger
//            LogService.getRoot().log(Level.WARNING, "NumericalStatistics.calculating_statistics_unknown_type_error", name);
            return Double.NaN;
        }
    }
}
