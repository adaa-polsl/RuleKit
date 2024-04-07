package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.data.condition.AbstractCondition;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.condition.StringCondition;
import adaa.analytics.rules.rm.comp.TsExampleSet;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.IStatistics;
import adaa.analytics.rules.rm.example.table.INominalMapping;

import java.util.ArrayList;
import java.util.List;

public class ContrastRegressionExampleSet extends ContrastExampleSet {

    /** Training set estimator. */
    protected double trainingEstimator;

    /** Collection of Kaplan-Meier estimators for contrast groups. */
    protected List<Double> groupEstimators = new ArrayList<Double>();

    /** Gets {@link #groupEstimators} */
    public List<Double> getGroupEstimators() { return groupEstimators; }

    /** Gets {@link #trainingEstimator}}. */
    public double getTrainingEstimator() { return trainingEstimator; }

    public ContrastRegressionExampleSet(TsExampleSet exampleSet) {
        super(exampleSet);

        String averageName = (exampleSet.getAttributes().getWeight() != null)
                ? IStatistics.AVERAGE_WEIGHTED : IStatistics.AVERAGE;

        // establish training  estimator
        IAttribute label = exampleSet.getAttributes().getLabel();
        exampleSet.recalculateAttributeStatistics(label);
        trainingEstimator = exampleSet.getStatistics(label, averageName);

        // establish contrast groups  estimator
        try {
            INominalMapping mapping = contrastAttribute.getMapping();

            for(String value : mapping.getValues()) {
                ICondition cnd = new StringCondition(contrastAttribute.getName(), AbstractCondition.EComparisonOperator.EQUALS, value);

                IExampleSet conditionedSet = exampleSet.filter(cnd);
                conditionedSet.recalculateAttributeStatistics(label);
                groupEstimators.add(conditionedSet.getStatistics(label, averageName));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
//        catch (ExpressionEvaluationException e) {
//            e.printStackTrace();
//        }
    }
}
