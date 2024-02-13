package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NotIntersectingActionInductionRangeStrategy extends ActionInductionRangeStrategy {

	public NotIntersectingActionInductionRangeStrategy(ElementaryCondition pattern, IExampleSet dataset) {
		super(pattern, dataset);
	}
	
	protected Stream<ElementaryCondition> filterInternal(Set<ElementaryCondition> toFilter) {
		return toFilter.stream()
			.filter(
					x -> !x.getValueSet().intersects(_pattern.getValueSet())
					);
	}

	@Override
	public Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter) {
		return filterInternal(toFilter).collect(Collectors.toSet());
	}

}
