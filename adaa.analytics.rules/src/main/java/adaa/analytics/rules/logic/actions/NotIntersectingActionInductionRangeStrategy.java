package adaa.analytics.rules.logic.actions;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.representation.ElementaryCondition;

public class NotIntersectingActionInductionRangeStrategy extends ActionInductionRangeStrategy {

	public NotIntersectingActionInductionRangeStrategy(ElementaryCondition pattern, ExampleSet dataset) {
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
