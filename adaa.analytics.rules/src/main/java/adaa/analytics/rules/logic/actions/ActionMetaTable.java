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
import com.rapidminer.example.Example;

import adaa.analytics.rules.logic.actions.MetaValue;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
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
	        for (MetaValue metaValue : sets.get(index)) {
	            for (MetaExample metaExample : _cartesianProduct(index+1, sets)) {
	                metaExample.add(metaValue);
	                ret.add(metaExample);
	            }
	        }
	    }
	    return ret;
	}
	
	private void generate() {
		
		Map<String, Map<ElementaryCondition, DistributionEntry>> map = dist.getDistribution();

		List<Set<MetaValue>> sets = new ArrayList<Set<MetaValue>>(map.size());
		
		for (String key : map.keySet()) {
			sets.add(map.get(key).entrySet().stream().map(x -> new MetaValue(x.getKey(), x.getValue())).collect(Collectors.toSet()));
		}
	
		
		examples = cartesianProduct(sets);
		
	}

	public Set<MetaExample> getExamples() {
		return examples;
	}
	
	public void analyze(Example ex, int fromClass, int toClass) {
		MetaExample primeMe = null;
		MetaExample contraMe = null;
		
		for (MetaExample me : examples) {
			
			if (me.covers(ex)) {
				primeMe = me;
				break;
			}
		}
		
		
	}
}
