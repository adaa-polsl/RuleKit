package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.SortedExampleSet;

import java.util.HashMap;
import java.util.Map;

public class SortedExampleSetEx extends SortedExampleSet {

    public double[] labels;
    public double[] weights;
    public double[] labelsWeighted;
    public double[] totalWeightsBefore;
    public double[] survivalTimes;

    public double meanLabel = 0;

    public Map<IAttribute, IntegerBitSet> nonMissingVals = new HashMap<>();

    public SortedExampleSetEx(IExampleSet parent, IAttribute sortingAttribute, int sortingDirection) {
        super(parent, sortingAttribute, sortingDirection);
        fillLabelsAndWeights();
    }

//    public SortedExampleSetEx(IExampleSet parent, IAttribute sortingAttribute, int sortingDirection, OperatorProgress progress) throws ProcessStoppedException {
//        super(parent, sortingAttribute, sortingDirection, progress);
//        fillLabelsAndWeights();
//    }

    public SortedExampleSetEx(IExampleSet parent, int[] mapping) {
        super(parent, mapping);
        fillLabelsAndWeights();
    }

    public SortedExampleSetEx(SortedExampleSet exampleSet) {
        super(exampleSet);
        fillLabelsAndWeights();
    }

    protected final void fillLabelsAndWeights() {
        labels = new double[this.size()];
        labelsWeighted = new double[this.size()];
        weights = new double[this.size()];
        totalWeightsBefore = new double[this.size() + 1];

        IAttribute survTime = this.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE);
        if (survTime != null) {
            survivalTimes = new double[this.size()];
        }

        boolean weighted = getAttributes().getWeight() != null;

        for (IAttribute a: this.getAttributes()) {
            nonMissingVals.put(a, new IntegerBitSet(this.size()));
        }

        int i = 0;
        int sumWeights = 0;

        for (Example e: this) {
            double y = e.getLabel();
            double w = weighted ? e.getWeight() : 1.0;

            labels[i] = y;
            weights[i] = w;
            labelsWeighted[i] = y * w;
            totalWeightsBefore[i] = sumWeights;
            meanLabel += y;

            if (survTime != null) {
                survivalTimes[i] = e.getValue(survTime);
            }

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
