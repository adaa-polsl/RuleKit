package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.DataColumnDoubleAdapter;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.DataTableAnnotations;
import adaa.analytics.rules.logic.representation.IntegerBitSet;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

public class SortedExampleSetEx implements IExampleSet {

    private IExampleSet delegateExampleSet;

    public double[] labels;
    public double[] weights;
    public double[] labelsWeighted;
    public double[] totalWeightsBefore;
    public double[] survivalTimes;

    public double meanLabel = 0;

    public Map<IAttribute, IntegerBitSet> nonMissingVals = new HashMap<>();

    public SortedExampleSetEx(IExampleSet parent, IAttribute sortingAttribute, EColumnSortDirections sortingDirection) {
        this.delegateExampleSet = parent;
        sortBy(sortingAttribute.getName(), sortingDirection);
        fillLabelsAndWeights();
    }

    protected final void fillLabelsAndWeights() {
        labels = new double[this.size()];
        labelsWeighted = new double[this.size()];
        weights = new double[this.size()];
        totalWeightsBefore = new double[this.size() + 1];

        IAttribute survTime = this.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE);
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




    @Override
    public DataTableAnnotations getAnnotations() {
        return delegateExampleSet.getAnnotations();
    }

    @Override
    public Object clone() {
        return delegateExampleSet.clone();
    }

    @Override
    public boolean equals(Object var1) {
        return delegateExampleSet.equals(var1);
    }

    @Override
    public int hashCode() {
        return delegateExampleSet.hashCode();
    }

    @Override
    public IAttributes getAttributes() {
        return delegateExampleSet.getAttributes();
    }

    @Override
    public int size() {
        return delegateExampleSet.size();
    }

    @Override
    public Example getExample(int var1) {
        return delegateExampleSet.getExample(var1);
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        return delegateExampleSet.filter(cnd);
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        return delegateExampleSet.filterWithOr(cndList);
    }

    @Override
    public IExampleSet updateMapping(IExampleSet mappingSource) {
        return delegateExampleSet.updateMapping(mappingSource);
    }

    @Override
    public Object[] getValues(String colName) {
        return delegateExampleSet.getValues(colName);
    }

    @NotNull
    @Override
    public Iterator<Example> iterator() {
        return delegateExampleSet.iterator();
    }

    @Override
    public void forEach(Consumer<? super Example> action) {
        delegateExampleSet.forEach(action);
    }

    @Override
    public Spliterator<Example> spliterator() {
        return delegateExampleSet.spliterator();
    }


    @Override
    public void sortBy(String columnName, EColumnSortDirections sortDir) {
        delegateExampleSet.sortBy(columnName, sortDir);
    }

    @Override
    public int addAttribute(IAttribute var1) {
        return delegateExampleSet.addAttribute(var1);
    }

    @Override
    public DataColumnDoubleAdapter getDataColumnDoubleAdapter(IAttribute attr, double defaultValue) {
        return delegateExampleSet.getDataColumnDoubleAdapter(attr, defaultValue);
    }

    @Override
    public double getDoubleValue(String colName, int colIdx, int rowIndex, double defaultValue) {
        return delegateExampleSet.getDoubleValue(colName, colIdx, rowIndex, defaultValue);
    }

    @Override
    public void setDoubleValue(IAttribute att, int rowIndex, double value) {
        delegateExampleSet.setDoubleValue(att, rowIndex, value);
    }
}
