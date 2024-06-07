package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

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
                ? Statistics.AVERAGE_WEIGHTED : Statistics.AVERAGE;

        // establish training  estimator
        Attribute label = exampleSet.getAttributes().getLabel();
        exampleSet.recalculateAttributeStatistics(label);
        trainingEstimator = exampleSet.getStatistics(label, averageName);

        // establish contrast groups  estimator
        try {
            NominalMapping mapping = contrastAttribute.getMapping();

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttribute, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                ExampleSet conditionedSet = new ConditionedExampleSet(exampleSet,cnd);
                conditionedSet.recalculateAttributeStatistics(label);
                groupEstimators.add(conditionedSet.getStatistics(label, averageName));
            }

        } catch (ExpressionEvaluationException e) {
            e.printStackTrace();
        }
    }

    public ContrastRegressionExampleSet(ContrastRegressionExampleSet rhs) {
        super(rhs);
        this.trainingEstimator = rhs.trainingEstimator;
        this.groupEstimators = rhs.groupEstimators;
    }
}
