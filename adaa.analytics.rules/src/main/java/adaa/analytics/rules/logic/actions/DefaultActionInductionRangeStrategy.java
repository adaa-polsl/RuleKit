package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import com.rapidminer.example.ExampleSet;

import java.util.Set;

public class DefaultActionInductionRangeStrategy extends ActionInductionRangeStrategy {

	public DefaultActionInductionRangeStrategy(ElementaryCondition pattern, ExampleSet dataset) {
		super(pattern, dataset);
	}

	@Override
	public Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter) {
		return toFilter;
	}

}
