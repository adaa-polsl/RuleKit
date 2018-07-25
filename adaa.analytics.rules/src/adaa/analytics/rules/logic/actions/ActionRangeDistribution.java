package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.Rule;

public class ActionRangeDistribution {
	
	protected class DistributionEntry {

		protected Map<String, List<Rule>> distribution;
		
		public DistributionEntry(List<String> values) {
			distribution = new HashMap<String, List<Rule>>(values.size());
			values.stream().forEach(x -> distribution.put(x, new LinkedList<Rule>()));
		}
		
		public void add(String classValue, Rule rule) {
			distribution.get(classValue).add(rule);
		}
		
	}
	
	protected ActionRuleSet actions;
	
	protected Map<IValueSet, DistributionEntry> distribution = new HashMap<IValueSet, DistributionEntry>();

	public ActionRangeDistribution(ActionRuleSet ruleset, ExampleSet dataset) {
		actions = ruleset;
		List<String> classValues = dataset.getAttributes().getLabel().getMapping().getValues();
		
		
		//Gather all elementary conditions to one list
		
		Stream<ElementaryCondition> conditions = Stream.empty();
		for (Rule rule : ruleset.getRules() ) {
			
			ActionRule actionRule = (ActionRule)rule;
			
			Rule left = actionRule.getLeftRule();
			Rule right = actionRule.getRightRule();
			
			conditions = Stream.concat(conditions, Stream.concat(
					left.getPremise().getSubconditions().parallelStream().map(ElementaryCondition.class::cast),
					right.getPremise().getSubconditions().parallelStream().map(ElementaryCondition.class::cast)
					));
			
		}
		
		//Split them by attribute
		
		Map<String, List<ElementaryCondition>> grouped = conditions.collect(Collectors.groupingBy(ElementaryCondition::getAttribute));
		
		//Calculate intersections of ranges in each attribute bin
		for (Map.Entry<String, List<ElementaryCondition>> entry : grouped.entrySet()) {
			Set<ElementaryCondition> uniques = new HashSet<ElementaryCondition>();
			List<ElementaryCondition> cnds = entry.getValue();
			
		}
		
	}

}
