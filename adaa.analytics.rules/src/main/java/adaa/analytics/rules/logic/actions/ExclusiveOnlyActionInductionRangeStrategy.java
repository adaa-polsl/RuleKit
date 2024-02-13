package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExclusiveOnlyActionInductionRangeStrategy extends NotIntersectingActionInductionRangeStrategy{

	public ExclusiveOnlyActionInductionRangeStrategy(ElementaryCondition pattern, IExampleSet dataset) {
		super(pattern, dataset);
	}

	@Override
	public Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter) {
		Stream<ElementaryCondition> exclusive = super.filterInternal(toFilter);
		
		Stream<ElementaryCondition> differences = toFilter.stream()
			.filter(x -> x.getValueSet().intersects(_pattern.getValueSet()))
			.map(x -> x.getValueSet().getDifference(_pattern.getValueSet()).stream())
			.flatMap(Function.identity())
			.map(x -> new ElementaryCondition(_attribute.getName(), x));
			
		
		return Stream.concat(exclusive, differences).collect(Collectors.toSet());
	}
}
