package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetaExample {
	private Map<String, MetaValue> data;
	private HashSet<Rule> rules = new HashSet<>();

	public MetaExample() {
		data = new HashMap<>();
	}


	public MetaExample(MetaExample contra) {
		data = new HashMap<>(contra.data);
		rules = new HashSet<>(contra.rules);
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
		 MetaExample that = (MetaExample)obj;

		 if (data.keySet().size() != that.data.keySet().size()) return false;
		 boolean partial = true;
		 for (String key : data.keySet()) {
			 partial = partial & that.data.containsKey(key);
		 }
		 if (!partial) return false;

		 for (String key: data.keySet()) {
		 	MetaValue thatMv = that.data.get(key);
		 	MetaValue thisMv = data.get(key);
		 	if (!thisMv.equals(thatMv))
		 		return false;
		 }

		 return true;
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

	public int getSize() {
		return data.size();
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
		//conjuction only, default (if empty) true
		boolean partial = true;
		
		for (Map.Entry<String, MetaValue> mv : data.entrySet()) {
			
			MetaValue metaVal = mv.getValue();
			
			partial = metaVal.contains(ex);
			
			if (!partial) {
				return false;
			}
		}
		
		return partial;
	}
	
	public Covering getCoverageForClass(IExampleSet examples, int fixedClass, Set<Integer> positives, Set<Integer> negatives) {
		
		assert(positives != null);
		assert(negatives != null);
		int id = 0;
		Covering cov = new Covering();
		for (Example ex: examples) {
			
			boolean classAgree = Double.compare(ex.getLabel(), fixedClass) == 0;
			
			if (classAgree) {
				cov.weighted_P++;
			} else {
				cov.weighted_N++;
			}
		
			if (this.covers(ex)) {
				if (classAgree) {
					cov.weighted_p++;
					positives.add(id);
				} else {
					cov.weighted_n++;
					negatives.add(id);
				}
			}
			
			id++;
		}
		return cov;
	}
	
	public Pair<Covering, Covering> getCoverage(IExampleSet examples, int toClass, int fromClass, Set<Integer> covered) {
		
		Covering classToCov = new Covering();
		Covering classFromCov = new Covering();
		
		for (int id : covered) {
			Example ex = examples.getExample(id);
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

	public boolean isCoveredBy(Rule rule) {
		boolean decision = true;
		int covered = 0;
		for(String atr : data.keySet()) {
			MetaValue mv = data.get(atr);

			//boolean partial =
			Optional<ElementaryCondition> opt = rule.getPremise().getSubconditions()
					.stream()
					.map(ElementaryCondition.class::cast)
					.filter(x -> x.getAttribute().equals(atr))
					.findFirst();

			if (opt.isPresent()) {
				decision &= opt.get().getValueSet().intersects(mv.getValue().getValueSet());
				if (decision) {
					covered++;
				}
			} else {
				continue;
			}
		}

		return decision && covered > 0;
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
