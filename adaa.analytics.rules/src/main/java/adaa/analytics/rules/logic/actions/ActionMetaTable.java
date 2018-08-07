package adaa.analytics.rules.logic.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.rapidminer.example.Attribute;

import adaa.analytics.rules.logic.actions.MetaValue;
import adaa.analytics.rules.logic.actions.ActionRangeDistribution.DistributionEntry;
import adaa.analytics.rules.logic.representation.IValueSet;

public class ActionMetaTable {

	protected ActionRangeDistribution dist;
	protected int exampleSize;
	protected int tableSize;
	protected Set<MetaExample> examples;
	
	
	public ActionMetaTable(ActionRangeDistribution distribution) {
		dist = distribution;
		exampleSize = dist.getDistribution().size();
		
		tableSize = dist.getDistribution().entrySet().stream().mapToInt(x -> x.getValue().size()).sum();
		generate();
	}

	
	protected Set<MetaExample> cartesianProduct(List<Set<MetaValue>> sets) {
	    if (sets.size() < 2)
	        throw new IllegalArgumentException(
	                "Can't have a product of fewer than two sets (got " +
	                sets.size() + ")");

	    return _cartesianProduct(0, sets);
	}

	private Set<MetaExample> _cartesianProduct(int index, List<Set<MetaValue>> sets) {
	    Set<MetaExample> ret = new HashSet<MetaExample>();
	    if (index == sets.size()) {
	        ret.add(new MetaExample());
	    } else {
	        for (MetaValue obj : sets.get(index)) {
	            for (MetaExample set : _cartesianProduct(index+1, sets)) {
	                set.add(obj.attribute, obj);
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
		
		List<Set<MetaValue>> sets = new ArrayList<Set<MetaValue>>(map.size());
		
		for (String key : map.keySet()) {
			sets.add(map.get(key).entrySet().stream().map(x -> new MetaValue(x.getKey(), x.getValue(), key)).collect(Collectors.toSet()));
		}
	
		
		examples = cartesianProduct(sets);
		
	}

	public Set<MetaExample> getExamples() {
		return examples;
	}
}
