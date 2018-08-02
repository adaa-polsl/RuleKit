package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import adaa.analytics.rules.logic.actions.ActionRangeDistribution.DistributionEntry;
import adaa.analytics.rules.logic.representation.IValueSet;

public class ActionMetaTable {

	protected ActionRangeDistribution dist;
	protected int exampleSize;
	protected int tableSize;
	protected Set<Set<Object>> examples;
	
	
	protected class Value {
		public IValueSet value;
		public DistributionEntry distribution;
		public Value(IValueSet v, DistributionEntry d) {
			value = v;
			distribution = d;
		}
	}
	
	public ActionMetaTable(ActionRangeDistribution distribution) {
		dist = distribution;
		exampleSize = dist.getDistribution().size();
		
		tableSize = dist.getDistribution().entrySet().stream().mapToInt(x -> x.getValue().size()).sum();
	}

	
	protected Set<Set<Object>> cartesianProduct(Set<?>... sets) {
	    if (sets.length < 2)
	        throw new IllegalArgumentException(
	                "Can't have a product of fewer than two sets (got " +
	                sets.length + ")");

	    return _cartesianProduct(0, sets);
	}

	private Set<Set<Object>> _cartesianProduct(int index, Set<?>... sets) {
	    Set<Set<Object>> ret = new HashSet<Set<Object>>();
	    if (index == sets.length) {
	        ret.add(new HashSet<Object>());
	    } else {
	        for (Object obj : sets[index]) {
	            for (Set<Object> set : _cartesianProduct(index+1, sets)) {
	                set.add(obj);
	                ret.add(set);
	            }
	        }
	    }
	    return ret;
	}
	
	private void generate() {
		
		Map<String, Map<IValueSet, DistributionEntry>> map = dist.getDistribution();
		
		String[] attributes = (String[]) map.keySet().stream().toArray();
		Map<IValueSet, DistributionEntry>[] dists = (Map<IValueSet, DistributionEntry>[]) map.values().toArray();
		
		Set<Set<Value>> sets = new HashSet<Set<Value>>(map.size());
		
		for (String key : map.keySet()) {
			sets.add(map.get(key).entrySet().stream().map(x -> new Value(x.getKey(), x.getValue())).collect(Collectors.toSet()));
		}
	
		
		examples = cartesianProduct(sets);
		
	}

	public Set<Set<Object>> getExamples() {
		return examples;
	}
}
