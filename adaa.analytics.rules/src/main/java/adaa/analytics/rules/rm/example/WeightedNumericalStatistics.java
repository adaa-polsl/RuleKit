package adaa.analytics.rules.rm.example;

public class WeightedNumericalStatistics implements IStatistics {
    private static final long serialVersionUID = -6283236022093847887L;
    private double sum = 0.0;
    private double squaredSum = 0.0;
    private double totalWeight = 0.0;
    private double count = 0.0;

    public WeightedNumericalStatistics() {
    }

    private WeightedNumericalStatistics(WeightedNumericalStatistics other) {
        this.sum = other.sum;
        this.squaredSum = other.squaredSum;
        this.totalWeight = other.totalWeight;
        this.count = other.count;
    }

    public Object clone() {
        return new WeightedNumericalStatistics(this);
    }

    public void startCounting(IAttribute attribute) {
        this.sum = 0.0;
        this.squaredSum = 0.0;
        this.totalWeight = 0.0;
        this.count = 0.0;
    }

    public void count(double value, double weight) {
        if (Double.isNaN(weight)) {
            weight = 1.0;
        }

        if (!Double.isNaN(value)) {
            this.sum += weight * value;
            this.squaredSum += weight * value * value;
            this.totalWeight += weight;
            ++this.count;
        }

    }

    public boolean handleStatistics(String name) {
        return "average_weighted".equals(name) || "variance_weighted".equals(name) || "sum_weighted".equals(name);
    }

    public double getStatistics(IAttribute attribute, String name, String parameter) {
        if ("average_weighted".equals(name)) {
            return this.sum / this.totalWeight;
        } else if ("variance_weighted".equals(name)) {
            return this.count <= 1.0 ? 0.0 : (this.squaredSum - this.sum * this.sum / this.totalWeight) / ((this.count - 1.0) / this.count * this.totalWeight);
        } else if ("sum_weighted".equals(name)) {
            return this.sum;
        } else {
            // @TODO Logger
//            LogService.getRoot().log(Level.WARNING, "WeightedNumericalStatistics.calculating_statistics_unknown_type_error", name);
            return Double.NaN;
        }
    }
}
