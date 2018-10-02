package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.representation.ElementaryCondition;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;

import java.util.*;
import java.util.stream.Collectors;

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
			sets.add(
					map
					.get(key)
					.entrySet()
					.stream()
					.map(x -> new MetaValue(x.getKey(), x.getValue()))
					.collect(Collectors.toSet())
					);
		}
		
		examples = cartesianProduct(sets);
		
	}

	public Set<MetaExample> getExamples() {
		return examples;
	}
	
	class AnalysisResult {
		public MetaExample primeMetaExample;
		public MetaExample contraMetaExample;
		public Example example;
		
		public AnalysisResult(Example ex, MetaExample prime, MetaExample contre) {
			example = ex;
			primeMetaExample = prime;
			contraMetaExample = contre;
		}
	}
	
	public AnalysisResult analyze(Example ex, int fromClass, int toClass) {
		MetaExample primeMe = null;
		MetaExample contraMe = new MetaExample();
		
		for (MetaExample me : examples) {
			
			if (me.covers(ex)) {
				primeMe = me;
				break;
			}
		}
		
		
		if (primeMe == null) {
			throw new RuntimeException("The example ex was not covered by any metaexample");
		}
		
		
		Set<MetaExample> toSearch = new HashSet<MetaExample>(examples);
		toSearch.remove(primeMe);
		MetaExample currBest = null;
		double currQ = Double.NEGATIVE_INFINITY;
		
		
		for (Attribute atr: ex.getAttributes()) {
			
			
			
			String atrName = atr.getName();
			
			if (primeMe.get(atrName) == null) {
				continue;
			}
			
			Double value = ex.getValue(atr);
			
			for (MetaExample me : toSearch) {
				
				double q = me.getQualityOf(value,  atrName, toClass);
				if (q > currQ) {
					currQ = q;
					currBest = me;
				}
			}
			
			if (currBest == null) {
				throw new RuntimeException("Could not find contre-value");
			}
			
			MetaValue bestMv = currBest.get(atrName);
			
			if (bestMv == null) {
				throw new RuntimeException("Could not find contre-value meta-value");
			}
			
			contraMe.add(bestMv);	
		}
		
		return new AnalysisResult(ex, primeMe, contraMe);
	}
}
