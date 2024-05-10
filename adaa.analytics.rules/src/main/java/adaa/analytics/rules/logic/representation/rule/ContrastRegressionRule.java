package adaa.analytics.rules.logic.representation.rule;

import adaa.analytics.rules.logic.representation.condition.CompoundCondition;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;

public class ContrastRegressionRule extends ContrastRule {

    private static final long serialVersionUID = -365903121658911104L;

    /** Mean label of covered examples. */
    protected double meanLabel;

    /** Gets {@link #meanLabel}. */
    public double getMeanLabel() { return meanLabel; }
    /** Sets {@link #meanLabel}. */
    public void setMeanLabel(double v) { meanLabel = v; }

    /**
     * Creates contrast rule with a given premise and a consequence.
     * @param premise Rule premise.
     * @param consequence Rule consequence.
     */
    public ContrastRegressionRule(CompoundCondition premise, ElementaryCondition consequence) {
        super(premise, consequence);
    }

}
