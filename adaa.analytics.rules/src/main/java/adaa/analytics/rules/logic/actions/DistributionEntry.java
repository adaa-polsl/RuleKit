package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.Rule;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class DistributionEntry {

	protected Map<Double, List<Rule>> distribution;

	public DistributionEntry() {
		distribution = new HashMap<>();
	}

	public DistributionEntry(DistributionEntry rhs) {
		distribution = new HashMap<>();
		for(Double entry : rhs.distribution.keySet()) {
			distribution.put(entry, new LinkedList<>());
			for (Rule r : rhs.distribution.get(entry)) {
				distribution.get(entry).add(r);
			}
		}
	}

	public void add(Double classValue, Rule rule) {
		if (!distribution.containsKey(classValue)) {
			distribution.put(classValue, new LinkedList<Rule>());
		}
		distribution.get(classValue).add(rule);
	}
	
	public Stream<Rule> getAllRules() {
		return distribution.values().stream().flatMap(x -> x.stream());
	}
	
	public Optional<List<Rule>> getRulesOfClass(double classId) {
		return Optional.ofNullable(distribution.get(classId));
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