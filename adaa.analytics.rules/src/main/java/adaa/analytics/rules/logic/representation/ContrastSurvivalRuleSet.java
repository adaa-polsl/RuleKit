package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.InductionParameters;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.List;

public class ContrastSurvivalRuleSet extends ContrastRuleSet {

    private static final long serialVersionUID = 3133147991180875664L;

    /** Training set estimator. */
    protected KaplanMeierEstimator trainingEstimator;

    /** Collection of Kaplan-Meier estimators for contrast groups. */
    protected List<KaplanMeierEstimator> groupEstimators = new ArrayList<KaplanMeierEstimator>();

    /** Gets {@link #groupEstimators} */
    public List<KaplanMeierEstimator> getGroupEstimators() { return groupEstimators; }

    /** Gets {@link #trainingEstimator}}. */
    public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }

    /**
     * Invokes base class constructor.
     *
     * @param exampleSet Training set.
     * @param isVoting   Voting flag.
     * @param params     Induction parameters.
     * @param knowledge  User's knowledge.
     */
    public ContrastSurvivalRuleSet(ExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
        super(exampleSet, isVoting, params, knowledge);

        // establish training survival estimator
        trainingEstimator = new KaplanMeierEstimator(exampleSet);

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
                groupEstimators.add(new KaplanMeierEstimator(conditionedSet));
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
        sb.append("\nEstimator:\n");

        // get times from training estimator
        ArrayList<Double> times = trainingEstimator.getTimes();

        // build header
        sb.append("time,entire-set");
        for (int i = 0; i < groupEstimators.size(); ++i) {
            sb.append(",group-" + (i + 1));
        }

        for (int i = 0; i < rules.size(); ++i) {
            sb.append(",cs-" + (i + 1));
        }
        sb.append("\n");

        for (double t : times) {
            // training set estimator
            sb.append(t + "," + trainingEstimator.getProbabilityAt(t));

            // group estimators
           for (KaplanMeierEstimator ge: groupEstimators) {
                sb.append("," + ge.getProbabilityAt(t));
            }

            // rule estimators
            for (Rule r: rules) {
                KaplanMeierEstimator kme = ((ContrastSurvivalRule)r).getEstimator();
                sb.append("," + kme.getProbabilityAt(t));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
