package adaa.analytics.rules.logic.actions;

import java.util.Set;

import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.representation.ElementaryCondition;

public class DefaultActionInductionRangeStrategy extends ActionInductionRangeStrategy {

	public DefaultActionInductionRangeStrategy(ElementaryCondition pattern, ExampleSet dataset) {
		super(pattern, dataset);
	}

	@Override
	public Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter) {
		return toFilter;
	}

}
