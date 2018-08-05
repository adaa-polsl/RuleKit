package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import adaa.analytics.rules.logic.actions.ActionMetaTable.Value;
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
		generate();
	}

	
	protected Set<Set<Object>> cartesianProduct(List<Set<Value>> sets) {
	    if (sets.size() < 2)
	        throw new IllegalArgumentException(
	                "Can't have a product of fewer than two sets (got " +
	                sets.size() + ")");

	    return _cartesianProduct(0, sets);
	}

	private Set<Set<Object>> _cartesianProduct(int index, List<Set<Value>> sets) {
	    Set<Set<Object>> ret = new HashSet<Set<Object>>();
	    if (index == sets.size()) {
	        ret.add(new HashSet<Object>());
	    } else {
	        for (Object obj : sets.get(index)) {
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
		
	//	String[] attributes = (String[]) map.keySet().toArray(new String[0]);
	//	Map<IValueSet, DistributionEntry>[] dists = 
	//			(Map<IValueSet, DistributionEntry>[]) map.values().toArray((Map<IValueSet, DistributionEntry>[])new Map[0]);
		
		List<Set<Value>> sets = new ArrayList<Set<Value>>(map.size());
		
		for (String key : map.keySet()) {
			sets.add(map.get(key).entrySet().stream().map(x -> new Value(x.getKey(), x.getValue())).collect(Collectors.toSet()));
		}
	
		
		examples = cartesianProduct(sets);
		
	}

	public Set<Set<Object>> getExamples() {
		return examples;
	}
}
