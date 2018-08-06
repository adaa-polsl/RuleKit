package adaa.analytics.rules.logic.actions;

import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.representation.ElementaryCondition;

public abstract class ActionInductionRangeStrategy {

	protected ElementaryCondition _pattern;
	protected Attribute _attribute;
	
	public ActionInductionRangeStrategy(ElementaryCondition pattern, ExampleSet dataset) {
		_pattern = pattern;
		_attribute = dataset.getAttributes().get(_pattern.getAttribute());
	}
	
	public abstract Set<ElementaryCondition> filter(Set<ElementaryCondition> toFilter);
}
