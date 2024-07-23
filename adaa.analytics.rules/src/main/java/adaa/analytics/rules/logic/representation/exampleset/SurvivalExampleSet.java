package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;

public class SurvivalExampleSet extends ExampleSetWrapper {

    public SurvivalExampleSet(IExampleSet parent) {
        super(parent);
        IAttribute survTime = parent.getAttributes().getColumnByRole(SurvivalRule.SURVIVAL_TIME_ROLE);
        sortBy(survTime.getName(), EColumnSortDirections.INCREASING);
    }
}
