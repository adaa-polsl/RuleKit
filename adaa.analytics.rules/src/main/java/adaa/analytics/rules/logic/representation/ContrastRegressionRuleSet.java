package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.InductionParameters;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

import java.security.InvalidParameterException;
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

        ContrastRegressionExampleSet cer = (exampleSet instanceof ContrastRegressionExampleSet) ? (ContrastRegressionExampleSet)exampleSet : null;
        if (cer == null) {
            throw new InvalidParameterException("ContrastRegressionRuleSet supports only ContrastRegressionExampleSet instances");
        }

        trainingMean = cer.getTrainingEstimator();
        groupMeans.addAll(cer.getGroupEstimators());
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
