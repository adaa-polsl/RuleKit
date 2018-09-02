package adaa.analytics.rules.logic.actions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import adaa.analytics.rules.logic.representation.Rule;

class DistributionEntry {

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
	
	public Optional<List<Rule>> getRulesOfClass(double classId) {
		return Optional.ofNullable(distribution.get(classId));
	}
	
	public Map<Double, List<Rule>> getRulesNotOfClass(double classId) {
		 Map<Double, List<Rule>> others = new HashMap<Double, List<Rule>>(distribution);
		 others.remove(classId);
		 return others;
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