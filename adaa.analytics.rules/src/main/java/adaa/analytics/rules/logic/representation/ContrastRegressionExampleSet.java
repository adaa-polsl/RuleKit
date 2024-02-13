package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.IStatistics;
import adaa.analytics.rules.rm.example.set.AttributeValueFilterSingleCondition;
import adaa.analytics.rules.rm.example.set.ConditionedExampleSet;
import adaa.analytics.rules.rm.example.set.SimpleExampleSet;
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

    public ContrastRegressionExampleSet(SimpleExampleSet exampleSet) {
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

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttribute, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                IExampleSet conditionedSet = new ConditionedExampleSet(exampleSet,cnd);
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
