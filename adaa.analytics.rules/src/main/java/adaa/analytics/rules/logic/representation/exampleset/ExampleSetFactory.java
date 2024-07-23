package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.rule.RuleType;
import adaa.analytics.rules.logic.representation.ruleset.*;

public class ExampleSetFactory {

    /**
     * Rule type (classification/regression/survival).
     */
    protected int type;

    /**
     * Gets rule type.
     * @return Rule type.
     */
    public int getType() { return this.type; }


    /**
     * Constructor of example set factory. Initializes with arguments members.
     * @param type Rule type (classification/regression/survival).
     */
    public ExampleSetFactory(int type) {
        this.type = type;
    }

    /**
     * Creates an empty rule set of appropriate type.
     * @param set Training set.
     * @return Empty rule set.
     */
    public IExampleSet create(IExampleSet set) {
        switch (type) {
            case RuleType.CLASSIFICATION:
                return set;
            case RuleType.REGRESSION:
                return new RegressionExampleSet(set);
            case RuleType.SURVIVAL:
                return new SurvivalExampleSet(set);
            case RuleType.CONTRAST:
                return new ContrastExampleSet(set);
            case RuleType.CONTRAST_REGRESSION:
                return new ContrastRegressionExampleSet(set);
            case RuleType.CONTRAST_SURVIVAL:
                return new ContrastSurvivalExampleSet(set);
        }

        return null;
    }
}
