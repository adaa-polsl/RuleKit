package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.Comparator;
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
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class ActionRangeDistribution {
	
	protected Map<String, Map<IValueSet, DistributionEntry>> dist = new HashMap<String, Map<IValueSet, DistributionEntry>>();
	
	protected class DistributionEntry {

		protected Map<Double, List<Rule>> distribution;
		
		public DistributionEntry() {
			distribution = new HashMap<Double, List<Rule>>();
		}
		
		public void add(Double classValue, Rule rule) {
			if (!distribution.containsKey(classValue) ) {
				distribution.put(classValue, new LinkedList<Rule>());
			}
			distribution.get(classValue).add(rule);
		}
		
	}
	
	protected class ConditionWithClass{
		
		private ElementaryCondition cond;
		IValueSet decision;
		Rule rule;
		
		public ConditionWithClass(ElementaryCondition condition, Rule rl) {
			cond = condition;
			rule = rl;
		}
		
		public ElementaryCondition getCondition() {
			return cond;
		}
		
		public IValueSet getKlass() {
			return rule.getConsequence().getValueSet();
		}
		
		public Rule getRule() {
			return rule;
		}
	}
	
	protected final ActionRuleSet actions;
	protected final ExampleSet set;
	
	protected Map<IValueSet, DistributionEntry> distribution = new HashMap<IValueSet, DistributionEntry>();

	public ActionRangeDistribution(ActionRuleSet ruleset, ExampleSet dataset) {
		actions = ruleset;
		set = dataset;
	}
	
	public Map<String, Map<IValueSet, DistributionEntry>> getDistribution() {
		return dist;
	}
	
	protected void calculateActionDistribution() {
		
		List<String> classValues = set.getAttributes().getLabel().getMapping().getValues();
		
		//Gather all elementary conditions to one list
		Stream<ConditionWithClass> conditions = Stream.empty();
		for (Rule rule : actions.getRules() ) {
			
			ActionRule actionRule = (ActionRule)rule;
			
			Rule left = actionRule.getLeftRule();
			Rule right = actionRule.getRightRule();
			
		
			
			conditions = Stream.concat(conditions, Stream.concat(
					left.getPremise().getSubconditions().parallelStream().map(ElementaryCondition.class::cast).map(x -> new ConditionWithClass(x, left)),
					right.getPremise().getSubconditions().parallelStream().map(ElementaryCondition.class::cast).map(x -> new ConditionWithClass(x, right))
					));
			
		}
		
		//Split them by attribute
		Map<String, List<ConditionWithClass>> grouped = 
				conditions
				.collect(Collectors.groupingBy(x -> x.getCondition().getAttribute()));
		
		//Calculate intersections of ranges in each attribute bin
		for (Map.Entry<String, List<ConditionWithClass>> entry : grouped.entrySet()) {
			
			Set<ElementaryCondition> uniques = new HashSet<ElementaryCondition>();
			List<ConditionWithClass> cnds = entry.getValue();
			List<Interval> ivals = null;
			List<Interval> result = new ArrayList<Interval>();
			String atr = entry.getKey();
			
			//extracts just sorted by left and unique intervals
			if (!set.getAttributes().get(atr).isNumerical()) {
				continue;
			} else {
				ivals = cnds.stream()
						.map(ConditionWithClass::getCondition)
						.map(ElementaryCondition::getValueSet)
						.map(Interval.class::cast)
						.sorted(Comparator.comparing(Interval::getLeft))
						.distinct()
						.collect(Collectors.toList());
			}
			
			//finds all intersection point 
			IntersectionFinder finder = new IntersectionFinder();
			result = finder.calculateAllIntersectionsOf(ivals);
			
			//counting the distribution
			Map<IValueSet, DistributionEntry> split = new HashMap<IValueSet, DistributionEntry>(); 
			for (Interval i : result) {
				
				for (ConditionWithClass cnd : cnds) {
					
					if (i.intersects(cnd.getCondition().getValueSet())) {
						
						if (!split.containsKey(i)) {
							split.put(i, new DistributionEntry());						}
						split.get(i).add(((SingletonSet)cnd.getKlass()).getValue(), cnd.getRule());
					}
				}
			}
			
			dist.put(atr, split);	
		}
		
	}

}
