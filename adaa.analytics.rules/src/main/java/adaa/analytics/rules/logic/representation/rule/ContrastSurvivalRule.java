package adaa.analytics.rules.logic.representation.rule;

import adaa.analytics.rules.logic.representation.condition.CompoundCondition;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;
import adaa.analytics.rules.logic.representation.KaplanMeierEstimator;

public class ContrastSurvivalRule extends ContrastRule {

    private static final long serialVersionUID = -9020482889621959684L;

    /** Kaplan-Meier estimator in a rule consequence. */
    protected KaplanMeierEstimator estimator;

    /** Gets {@link #estimator}. */
    public KaplanMeierEstimator getEstimator() { return estimator; }
    /** Sets {@link #estimator}. */
    public void setEstimator(KaplanMeierEstimator v) { estimator = v; }

    /**
     * Creates contrast rule with a given premise and a consequence.
     * @param premise Rule premise.
     * @param consequence Rule consequence.
     */
    public ContrastSurvivalRule(CompoundCondition premise, ElementaryCondition consequence) {
        super(premise, consequence);
    }


}
