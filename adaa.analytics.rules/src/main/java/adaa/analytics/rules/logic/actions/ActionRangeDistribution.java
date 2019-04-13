package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.learner.subgroups.RuleSet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActionRangeDistribution {

	protected Map<String, Map<ElementaryCondition, DistributionEntry>> dist = new HashMap<String, Map<ElementaryCondition, DistributionEntry>>();

	protected class ConditionWithClass {

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
	protected HashSet<Rule> splittedRules = new HashSet<Rule>();

	protected Map<IValueSet, DistributionEntry> distribution = new HashMap<IValueSet, DistributionEntry>();

	public ActionRangeDistribution(ActionRuleSet ruleset, ExampleSet dataset) {
		actions = ruleset;
		set = dataset;
	}
	
	public List<Set<MetaValue>> getMetaValuesByAttribute() {
		List<Set<MetaValue>> sets = new ArrayList<Set<MetaValue>>(dist.size());
		
		for (String key : dist.keySet()) {
			sets.add(
					dist
					.get(key)
					.entrySet()
					.stream()
					.map(x -> new MetaValue(x.getKey(), x.getValue()))
					.collect(Collectors.toSet())
					);
		}
		
		return sets;
	}
	
	protected void calculateActionDistribution() {

		// Gather all elementary conditions to one list
		Stream<ConditionWithClass> conditions = Stream.empty();
		for (Rule rule : actions.getRules()) {

			ActionRule actionRule = (ActionRule) rule;

			Rule left = actionRule.getLeftRule();
			Rule right = actionRule.getRightRule();
			splittedRules.add(left);
			splittedRules.add(right);
			conditions = Stream.concat(conditions,
					Stream.concat(
							left.getPremise().getSubconditions().parallelStream().map(ElementaryCondition.class::cast)
									.map(x -> new ConditionWithClass(x, left)),
							right.getPremise().getSubconditions().parallelStream().map(ElementaryCondition.class::cast)
									.map(x -> new ConditionWithClass(x, right))));

		}

		// Split them by attribute
		Map<String, List<ConditionWithClass>> grouped = conditions
				.collect(Collectors.groupingBy(x -> x.getCondition().getAttribute()));

		// Calculate intersections of ranges in each attribute bin
		for (Map.Entry<String, List<ConditionWithClass>> entry : grouped.entrySet()) {

			List<ConditionWithClass> cnds = entry.getValue();
			List<Interval> ivals = null;
			List<Interval> result = new ArrayList<Interval>();
			String atr = entry.getKey();
			Map<ElementaryCondition, DistributionEntry> split = new HashMap<ElementaryCondition, DistributionEntry>();

			Attribute currAttribute = set.getAttributes().get(atr);
			
			if (!currAttribute.isNumerical()) {
				NominalMapping mapping = currAttribute.getMapping();
				
				mapping.getValues()
					.stream()
					.forEach(
							x -> split.put(
									new ElementaryCondition(
											currAttribute.getName(),
											new SingletonSet(mapping.getIndex(x), mapping.getValues())
											),
									new DistributionEntry()
								)
							);
				
				for (ConditionWithClass cnd : cnds) {
					split.get(cnd.getCondition()).add(((SingletonSet) cnd.getKlass()).getValue(),
							cnd.getRule());
				}

			} else {
				// extracts just sorted by left and unique intervals
				ivals = cnds
						.stream()
						.map(ConditionWithClass::getCondition)
						.map(ElementaryCondition::getValueSet)
						.map(Interval.class::cast)
						.sorted(Comparator.comparing(Interval::getLeft))
						.distinct()
						.collect(Collectors.toList());

				// finds all intersection point
				IntersectionFinder finder = new IntersectionFinder();
				result = finder.calculateAllIntersectionsOf(ivals);

				// counting the distribution

				for (Interval i : result) {
					ElementaryCondition ec = new ElementaryCondition(atr, i);
					for (ConditionWithClass cnd : cnds) {

						if (i.intersects(cnd.getCondition().getValueSet())) {

							if (!split.containsKey(ec)) {

								split.put(ec, new DistributionEntry());
							}

							split.get(ec).add(((SingletonSet) cnd.getKlass()).getValue(), cnd.getRule());
						}
					}
				}
			}

			dist.put(atr, split);
		}

	}

}
