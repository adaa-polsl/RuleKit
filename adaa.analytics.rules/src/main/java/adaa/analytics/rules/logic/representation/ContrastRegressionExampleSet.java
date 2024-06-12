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
import java.util.logging.Level;

public class ContrastRegressionExampleSet extends SortedExampleSetEx implements IContrastExampleSet {

    protected Attribute contrastAttribute;

    /** Training set estimator. */
    protected double trainingEstimator;

    /** Collection of Kaplan-Meier estimators for contrast groups. */
    protected List<Double> groupEstimators = new ArrayList<Double>();

    public Attribute getContrastAttribute() { return contrastAttribute; }

    /** Gets {@link #groupEstimators} */
    public List<Double> getGroupEstimators() { return groupEstimators; }

    /** Gets {@link #trainingEstimator}}. */
    public double getTrainingEstimator() { return trainingEstimator; }

    public ContrastRegressionExampleSet(SimpleExampleSet exampleSet) {
        super(exampleSet, exampleSet.getAttributes().getLabel(), SortedExampleSetEx.INCREASING);

        contrastAttribute = (exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

        String averageName = (exampleSet.getAttributes().getWeight() != null)
                ? Statistics.AVERAGE_WEIGHTED : Statistics.AVERAGE;

        // establish training  estimator
        Attribute label = exampleSet.getAttributes().getLabel();
        exampleSet.recalculateAttributeStatistics(label);
        trainingEstimator = exampleSet.getStatistics(label, averageName);

        Logger.log("Training estimator: " + trainingEstimator + "\n", Level.FINE);

        // establish contrast groups  estimator
        try {
            NominalMapping mapping = contrastAttribute.getMapping();

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttribute, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                ExampleSet conditionedSet = new ConditionedExampleSet(exampleSet,cnd);
                conditionedSet.recalculateAttributeStatistics(label);
                groupEstimators.add(conditionedSet.getStatistics(label, averageName));

                Logger.log("Group estimator [" + mapping.mapIndex(i) + "]: " +  groupEstimators.get(i) + "\n", Level.FINE);
            }

        } catch (ExpressionEvaluationException e) {
            e.printStackTrace();
        }
    }

    public ContrastRegressionExampleSet(ContrastRegressionExampleSet rhs) {
        super(rhs);
        this.contrastAttribute = rhs.contrastAttribute;
        this.trainingEstimator = rhs.trainingEstimator;
        this.groupEstimators = rhs.groupEstimators;
    }
}
