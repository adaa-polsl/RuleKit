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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.NominalMapping;

import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class ActionRangeDistribution {

	protected Map<String, Map<ElementaryCondition, DistributionEntry>> dist = new HashMap<String, Map<ElementaryCondition, DistributionEntry>>();

	protected class DistributionEntry {

		protected Map<Double, List<Rule>> distribution;

		public DistributionEntry() {
			distribution = new HashMap<Double, List<Rule>>();
		}

		public void add(Double classValue, Rule rule) {
			if (!distribution.containsKey(classValue)) {
				distribution.put(classValue, new LinkedList<Rule>());
			}
			distribution.get(classValue).add(rule);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) { return false; }
			if (obj == this) { return true; }
			if (obj.getClass() != getClass()) {
				return false;
			}
			
			DistributionEntry de = (DistributionEntry)obj;
			return new EqualsBuilder()
					.append(distribution, de.distribution)
					.isEquals();
		}
		
		@Override
		public int hashCode() {
			return new HashCodeBuilder(13,17)
					.append(distribution)
					.toHashCode();
		}
		
	}

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

	protected Map<IValueSet, DistributionEntry> distribution = new HashMap<IValueSet, DistributionEntry>();

	public ActionRangeDistribution(ActionRuleSet ruleset, ExampleSet dataset) {
		actions = ruleset;
		set = dataset;
	}

	public Map<String, Map<ElementaryCondition, DistributionEntry>> getDistribution() {
		return dist;
	}

	protected void calculateActionDistribution() {

		// Gather all elementary conditions to one list
		Stream<ConditionWithClass> conditions = Stream.empty();
		for (Rule rule : actions.getRules()) {

			ActionRule actionRule = (ActionRule) rule;

			Rule left = actionRule.getLeftRule();
			Rule right = actionRule.getRightRule();

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

			// extracts just sorted by left and unique intervals
			if (!set.getAttributes().get(atr).isNumerical()) {
				
				for (ConditionWithClass cnd : cnds) {

					if (!split.containsKey(cnd.getCondition())) {

						split.put(cnd.getCondition(), new DistributionEntry());
					}
					split.get(cnd.getCondition()).add(((SingletonSet) cnd.getKlass()).getValue(),
							cnd.getRule());
				}

			} else {
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
