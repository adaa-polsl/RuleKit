package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.rm.example.set.AbstractExampleSet;

import java.util.Arrays;
import java.util.logging.Level;

public class NominalStatistics implements IStatistics {
    private static final long serialVersionUID = -7644523717916796701L;
    private long mode = -1L;
    private long maxCounter = 0L;
    private long[] scores;

    public NominalStatistics() {
        AbstractExampleSet asd;
    }

    private NominalStatistics(NominalStatistics other) {
        this.mode = other.mode;
        this.maxCounter = other.maxCounter;
        if (other.scores != null) {
            this.scores = Arrays.copyOf(other.scores, other.scores.length);
        }

    }

    public Object clone() {
        return new NominalStatistics(this);
    }

    public void startCounting(IAttribute attribute) {
        this.scores = new long[attribute.getMapping().size()];
        this.mode = -1L;
        this.maxCounter = 0L;
    }

    public void count(double doubleIndex, double weight) {
        if (!Double.isNaN(doubleIndex)) {
            int index = (int)doubleIndex;
            if (index >= 0) {
                if (index >= this.scores.length) {
                    long[] newScores = new long[index + 1];
                    System.arraycopy(this.scores, 0, newScores, 0, this.scores.length);
                    this.scores = newScores;
                }

                long var10002 = this.scores[index]++;
                if (this.scores[index] > this.maxCounter) {
                    this.maxCounter = this.scores[index];
                    this.mode = (long)index;
                }
            }
        }

    }

    public boolean handleStatistics(String name) {
        return "mode".equals(name) || "count".equals(name) || "least".equals(name);
    }

    public double getStatistics(IAttribute attribute, String name, String parameter) {
        if ("mode".equals(name)) {
            return (double)this.mode;
        } else if ("count".equals(name)) {
            if (parameter != null) {
                return (double)this.getValueCount(attribute, parameter);
            } else {
                // @TODO Logi
//                LogService.getRoot().log(Level.WARNING, "adaa.analytics.rules.rm.example.NominalStatistics.calculating_statistics_count_for_attribute_error", attribute.getName());
                return Double.NaN;
            }
        } else if ("least".equals(name)) {
            long minCounter = 2147483647L;
            long least = 0L;

            for(int i = 0; i < this.scores.length; ++i) {
                if (this.scores[i] < minCounter) {
                    minCounter = this.scores[i];
                    least = (long)i;
                }
            }

            return (double)least;
        } else {
            // @TODO Logi
//            LogService.getRoot().log(Level.WARNING, "adaa.analytics.rules.rm.example.NominalStatistics.calculating_statistics_unknown_type_error", name);
            return Double.NaN;
        }
    }

    private long getValueCount(IAttribute attribute, String value) {
        if (attribute != null && attribute.getMapping() != null) {
            int index = attribute.getMapping().getIndex(value);
            return index < 0 ? -1L : this.scores[index];
        } else {
            return -1L;
        }
    }

    public String toString() {
        return "Counts: " + Arrays.toString(this.scores);
    }
}
