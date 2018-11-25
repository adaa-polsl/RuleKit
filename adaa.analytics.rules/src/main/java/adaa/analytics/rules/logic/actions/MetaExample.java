package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.Rule;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

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
	
	public Map<String, ElementaryCondition> toPremise() {
		return data.keySet().stream().map(
				x -> {
					MetaValue mv = data.get(x);
					return mv.value;
				}
				).collect(Collectors.toMap(x -> x.getAttribute(), x->x));
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
	
	public void remove(MetaValue value) {
		data.remove(value.value.getAttribute());
		
		rules.removeAll(
				value.distribution.distribution.values().stream().flatMap(x->x.stream()).collect(Collectors.toList())
				);
		
	}
	
	public MetaValue get(String attribute) {
		if (data.containsKey(attribute)) {
			return data.get(attribute);
		}
		return null;
	}
	
	public boolean covers(Example ex) {
		
		boolean partial = false;
		
		for (Map.Entry<String, MetaValue> mv : data.entrySet()) {
			
			double val = ex.getValue(ex.getAttributes().get(mv.getKey()));
			
			MetaValue metaVal = mv.getValue();
			
			partial = metaVal.contains(val);
			
			if (!partial) {
				return false;
			}
		}
		
		return partial;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(
				data.values()
				.stream()
				.map(x->x.value.toString())
				.collect(Collectors.joining(" AND "))
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
	
	public double getCountOfRulesPointingToClass(String atrName, double targetClass) {
		MetaValue mv = data.get(atrName);
		
		if (mv == null) {
			return Double.NEGATIVE_INFINITY;
		}
		Optional<List<Rule>> rules = mv.distribution.getRulesOfClass(targetClass);
		
		return rules.orElse(new ArrayList<Rule>()).size();
	}
	
	public Pair<Covering, Covering> getCoverage(ExampleSet examples, int toClass, int fromClass) {
		
		Covering classToCov = new Covering();
		Covering classFromCov = new Covering();
		
		for (Example ex : examples) {
			
			if (this.covers(ex)) {
				classToCov.weighted_p++;
				classFromCov.weighted_p++;
			} else {
				classToCov.weighted_n++;
				classFromCov.weighted_n++;
			}
			
			if (Double.compare(ex.getLabel(), toClass) == 0 ) {
				classToCov.weighted_P++;
				classFromCov.weighted_N++;
			} else if (Double.compare(ex.getLabel(), fromClass) == 0) {
				classToCov.weighted_N++;
				classFromCov.weighted_P++;
			} else {
				classToCov.weighted_N++;
				classFromCov.weighted_N++;
			}
		}
		
		return new Pair<Covering, Covering>(classFromCov, classToCov);
	}

	public double getQualityOf(Double value, String atrName, double targetClass) {
		
		MetaValue mv = data.get(atrName);
		
		if (mv == null || mv.contains(value)) {
			return Double.NEGATIVE_INFINITY;
		}
		
		Optional<List<Rule>> rules = mv.distribution.getRulesOfClass(targetClass);
		Map<Double, List<Rule>> otherRules = mv.distribution.getRulesNotOfClass(targetClass);
		// assume weight = quality
		double positiveQualitySum = rules
					.orElse(new ArrayList<Rule>())
					.stream()
					.mapToDouble(x -> x.getWeight())
					.sum();
		
		
		
		double negativeQualitySum = Double.NEGATIVE_INFINITY;
		if (!otherRules.isEmpty()) {
			negativeQualitySum = otherRules.entrySet().stream()
														.flatMap(x -> x.getValue().stream())
														.mapToDouble(x->x.getWeight())
														.sum();
		}
		//obviously gonna change
		return positiveQualitySum - negativeQualitySum;
		
	}
}
