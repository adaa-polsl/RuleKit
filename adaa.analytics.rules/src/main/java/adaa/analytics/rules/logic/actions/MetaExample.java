package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.math3.util.Pair;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Rule;

public class MetaExample {
	private Map<String, MetaValue> data;
	private HashSet<Rule> rules = new HashSet<Rule>();
	
	public class MetaCoverage {
		protected double LS;
		protected double LT;
		
		public MetaCoverage(double LSource, double LTarget) {
			LS = LSource;
			LT = LTarget;
		}
		
		public double getCoverageOfTargetClass() {
			return LS;
		}
		
		public double getCoverageNotOfTargetClass() {
			return LT;
		}
	}

	public MetaExample() {
		data = new HashMap<String,MetaValue>();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(
				data.values()
				.stream()
				.map(x->x.toValueString())
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

	public Map<String, ElementaryCondition> toPremise() {
		return data.keySet().stream().map(
				x -> {
					MetaValue mv = data.get(x);
					return mv.getValue();
				}
				).collect(Collectors.toMap(x -> x.getAttribute(), x->x));
	}
	
	public void add(MetaValue metaValue) {
		data.put(metaValue.getAttribute(), metaValue);
		
		Stream<Rule> supportingRules = metaValue.getSupportingRules();
		
		List<ElementaryCondition> vals = data.values()
			.stream()
			.map(x->x.getValue())
			.collect(Collectors.toList());

		
		
		rules.removeIf(x -> !containsInAnyOrder(x.getPremise().getSubconditions(), vals));
		List<Rule> filteredRulesToAdd = supportingRules.filter(x -> !containsInAnyOrder(x.getPremise().getSubconditions(), vals)).collect(Collectors.toList());
		rules.addAll(filteredRulesToAdd);
		
	}
	
	public void remove(MetaValue metaValue) {
		data.remove(metaValue.getAttribute());
		
		rules.removeAll(
				metaValue.getSupportingRules().collect(Collectors.toList())
				);
		
	}
	
	public MetaValue get(String attribute) {
		if (data.containsKey(attribute)) {
			return data.get(attribute);
		}
		return null;
	}
	
	public Collection<MetaValue> getAllValues() {
		return data.values();
	}
	
	public Set<String> getAttributeNames() {
		return data.keySet();
	}
	
	public boolean covers(Example ex) {
		
		boolean partial = false;
		
		for (Map.Entry<String, MetaValue> mv : data.entrySet()) {
			
			MetaValue metaVal = mv.getValue();
			
			partial = metaVal.contains(ex);
			
			if (!partial) {
				return false;
			}
		}
		
		return partial;
	}
	
	public double getCountOfRulesPointingToClass(double targetClass) {
		return data.keySet()
				.stream()
				.mapToDouble(x -> getCountOfRulesPointingToClass(x, targetClass))
				.sum();
	}
	
	public MetaCoverage getMetaCoverage(double targetClass) {
		double lt = getMetaCoverageForClass(targetClass, false).size();
		double ls = getMetaCoverageForClass(targetClass, true).size();
		return new MetaCoverage(lt, ls);
	}
	
	public double getMetaCoverageValue(double targetClass) {
		return getMetaCoverageForClass(targetClass, false).size();
	}
	
	public double getCountOfRulesPointingToClass(String atrName, double targetClass) {
		MetaValue mv = data.get(atrName);
		
		if (mv == null) {
			return Double.NEGATIVE_INFINITY;
		}
		Optional<List<Rule>> rules = mv.getSupportingRulesPointingToClass(targetClass);
		
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


	public int getCountOfSupportingRules() {
		return rules.size();
	}

	private Set<Rule> getMetaCoverageForClass(double targetClass, boolean negate) {
		
		List<HashSet<Rule>> interim =null;
		if (negate) {
			interim = data.keySet()
			.stream()
			.map(x -> data.getOrDefault(x, MetaValue.EMPTY).getSupportingRulesNotOfClass(targetClass).orElse(new ArrayList<>()))
			.map(x -> new HashSet<Rule>(x))
			.sorted(Comparator.comparingInt(Set::size))
			.collect(Collectors.toList());
		}
		else {
			interim = data.keySet()
			.stream()
			.map(x -> data.getOrDefault(x, MetaValue.EMPTY).getSupportingRulesPointingToClass(targetClass).orElse(new ArrayList<>()))
			.map(x -> new HashSet<Rule>(x))
			.sorted(Comparator.comparingInt(Set::size))
			.collect(Collectors.toList());
		}
		
		Iterator<HashSet<Rule>> it = interim.iterator();
		if (!it.hasNext()) return Collections.emptySet();
		
		HashSet<Rule> ret = new HashSet<Rule>(it.next());
		while(it.hasNext()) {
			ret.retainAll(it.next());
		}
		return ret;
		
	}

	private boolean containsInAnyOrder(List<ConditionBase> conditionsA, List<ElementaryCondition> conditionB) {
		for (ConditionBase cnd : conditionsA) {
			if (!conditionB.contains(cnd)) {
				return false;
			}
		}
		return true;
	}
}
