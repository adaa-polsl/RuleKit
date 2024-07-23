package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.*;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.logic.representation.IntegerBitSet;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.DoubleColumn;

import java.util.*;
import java.util.function.Consumer;

public class RegressionExampleSet extends ExampleSetWrapper {

    public double[] labels;
    public double[] weights;
    public double[] labelsWeighted;
    public double[] totalWeightsBefore;
    public double meanLabel = 0;

    public Map<IAttribute, IntegerBitSet> nonMissingVals = new HashMap<>();

    public RegressionExampleSet(IExampleSet parent) {
        super(parent);
        IAttribute label = parent.getAttributes().getLabel();
        sortBy(label.getName(), EColumnSortDirections.INCREASING);
        fillLabelsAndWeights();
    }

    protected final void fillLabelsAndWeights() {
        labels = new double[this.size()];
        labelsWeighted = new double[this.size()];
        weights = new double[this.size()];
        totalWeightsBefore = new double[this.size() + 1];

        boolean weighted = getAttributes().getWeight() != null;

        for (IAttribute a: this.getAttributes()) {
            nonMissingVals.put(a, new IntegerBitSet(this.size()));
        }

        int i = 0;
        int sumWeights = 0;

        for (Example e: this) {
            double y = e.getLabelValue();
            double w = weighted ? e.getWeightValue() : 1.0;

            labels[i] = y;
            weights[i] = w;
            labelsWeighted[i] = y * w;
            totalWeightsBefore[i] = sumWeights;
            meanLabel += y;

            for (IAttribute a: this.getAttributes()) {
                if (!Double.isNaN(e.getValue(a))) {
                    nonMissingVals.get(a).add(i);
                }
            }

            ++i;
            sumWeights += w;
        }

        totalWeightsBefore[size()] = sumWeights;
        meanLabel /= size();
    }
}
