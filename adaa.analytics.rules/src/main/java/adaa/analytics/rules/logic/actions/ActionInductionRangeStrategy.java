package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.Set;

public abstract class ActionInductionRangeStrategy {

	protected ElementaryCondition _pattern;
	protected IAttribute _attribute;
	
	public ActionInductionRangeStrategy(ElementaryCondition pattern, IExampleSet dataset) {
		_pattern = pattern;
		_attribute = dataset.getAttributes().get(_pattern.getAttribute());
	}
	
	public abstract Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter);
}
