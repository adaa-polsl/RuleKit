package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.InductionParameters;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.List;

public class ContrastRegressionRuleSet extends ContrastRuleSet {

    private static final long serialVersionUID = 2412978472971367002L;

    /** Training set label mean. */
    protected double trainingMean;

    /** Collection of label means for contrast groups. */
    protected List<Double> groupMeans = new ArrayList<>();

    /** Gets {@link #groupMeans} */
    public List<Double> getGroupMeans() { return groupMeans; }

    /** Gets {@link #trainingMean}}. */
    public double getTrainingMean() { return trainingMean; }

    /**
     * Invokes base class constructor.
     *
     * @param exampleSet Training set.
     * @param isVoting   Voting flag.
     * @param params     Induction parameters.
     * @param knowledge  User's knowledge.
     */
    public ContrastRegressionRuleSet(ExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
        super(exampleSet, isVoting, params, knowledge);

        // establish training survival estimator
        exampleSet.recalculateAttributeStatistics(exampleSet.getAttributes().getLabel());
        trainingMean = exampleSet.getStatistics(exampleSet.getAttributes().getLabel(), "average");

        final Attribute contrastAttr = (exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

        // establish contrast groups survival estimator
        try {
            NominalMapping mapping = contrastAttr.getMapping();

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttr, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                ExampleSet conditionedSet = new ConditionedExampleSet(exampleSet,cnd);

                conditionedSet.recalculateAttributeStatistics(exampleSet.getAttributes().getLabel());
                groupMeans.add(conditionedSet.getStatistics(exampleSet.getAttributes().getLabel(), "average"));
            }

        } catch (ExpressionEvaluationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates text representation of the contrast survival rule set. Beside list of rules,
     * it contains survival function estimates of the training set, contrast groups set, and particular rules.
     * @return Rule set in the text form.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
     /*   sb.append("\nLabel means:\n");
        sb.append("training: " + trainingMean + "\n");

        for (int i = 0; i < groupMeans.size(); ++i) {
            sb.append("group-" + (i + 1) + ": " + groupMeans.get(i) + "\n");
        }

        for (int i = 0; i < rules.size(); ++i) {
            sb.append("r" + (i + 1) + ": " + ((ContrastRegressionRule)rules.get(i)).getMeanLabel() + "\n");
        }
        sb.append("\n");
*/
        return sb.toString();
    }
}
