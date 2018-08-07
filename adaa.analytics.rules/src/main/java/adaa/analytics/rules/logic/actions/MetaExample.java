package adaa.analytics.rules.logic.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Rule;

public class MetaExample {
	Map<String, MetaValue> data;
	HashSet<Rule> rules = new HashSet<Rule>();
	
	public MetaExample() {
		data = new HashMap<String,MetaValue>();
	}
	
	private boolean containsInAnyOrder(List<ConditionBase> conditionsA, List<ElementaryCondition> conditionB) {
		for (ConditionBase cnd : conditionsA) {
			if (!conditionB.contains(cnd)) {
				return false;
			}
		}
		return true;
	}
	
	public void add(MetaValue value) {
		data.put(value.value.getAttribute(), value);
		
		rules.addAll(value.distribution.distribution.values().stream().flatMap(x->x.stream()).collect(Collectors.toList()));
		
		List<ElementaryCondition> vals = data.values()
			.stream()
			.map(x->x.value)
			.collect(Collectors.toList());
		
		rules.removeIf(x -> !containsInAnyOrder(x.getPremise().getSubconditions(), vals));
	}
	
	public MetaValue get(String attribute) {
		if (data.containsKey(attribute)) {
			return data.get(attribute);
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(
				data.values()
				.stream()
				.map(x->x.value.toString())
				.collect(Collectors.joining(";"))
				);
		
		sb.append("[ ");
		sb.append(
				rules
				.stream()
				.map(x -> x.toString())
				.collect(Collectors.joining(";"))
		);
		sb.append(" ]");
		
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder b = new HashCodeBuilder(13,19);
		b.append(data);
		return b.toHashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		 if (obj == null) { return false; }
		 if (obj == this) { return true; }
		 if (obj.getClass() != getClass()) {
		     return false;
		 }
		 MetaExample me = (MetaExample)obj;
		 return new EqualsBuilder()
				 .append(data, me.data)
				 .isEquals();
		 
	}
}
