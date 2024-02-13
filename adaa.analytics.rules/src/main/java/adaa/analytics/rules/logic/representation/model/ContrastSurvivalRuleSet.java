package adaa.analytics.rules.logic.representation.model;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.security.InvalidParameterException;
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
    public ContrastSurvivalRuleSet(IExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
        super(exampleSet, isVoting, params, knowledge);

        ContrastSurvivalExampleSet ces = (exampleSet instanceof ContrastExampleSet) ? (ContrastSurvivalExampleSet)exampleSet : null;
        if (ces == null) {
            throw new InvalidParameterException("ContrastSurvivalRuleSet supports only ContrastExampleSet instances");
        }

        trainingEstimator = ces.getTrainingEstimator();
        groupEstimators.addAll(ces.getGroupEstimators());
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
            for (Rule r: getAllSets()) {
                KaplanMeierEstimator kme = ((ContrastSurvivalRule)r).getEstimator();
                sb.append("," + kme.getProbabilityAt(t));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
