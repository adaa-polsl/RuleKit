package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Set;

public class DefaultActionInductionRangeStrategy extends ActionInductionRangeStrategy {

	public DefaultActionInductionRangeStrategy(ElementaryCondition pattern, IExampleSet dataset) {
		super(pattern, dataset);
	}

	@Override
	public Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter) {
		return toFilter;
	}

}
