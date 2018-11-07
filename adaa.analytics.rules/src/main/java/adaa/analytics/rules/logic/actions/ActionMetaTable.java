package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Logger;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;

public class ActionMetaTable {

	protected ActionRangeDistribution dist;
	protected int exampleSize;
	protected int tableSize;
	protected Set<MetaExample> metaExamples;
	
	
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
		
		metaExamples = cartesianProduct(sets);
		
	}

	public Set<MetaExample> getExamples() {
		return metaExamples;
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
	
	//For given example returns respective meta-example and contre-meta-example of opposite class
	public AnalysisResult analyze(Example ex, int fromClass, int toClass, ExampleSet examples) {
		MetaExample primeMe = null;
		MetaExample contraMe = new MetaExample();
		
		for (MetaExample me : metaExamples) {
			
			if (me.covers(ex)) {
				primeMe = me;
				break;
			}
		}
		
		if (primeMe == null) {
			throw new RuntimeException("The example ex was not covered by any metaexample");
		}
		
		
		

		Set<MetaExample> toSearch = new HashSet<MetaExample>(metaExamples);
		toSearch.remove(primeMe);
		
		
		
		
		for (Attribute atr: ex.getAttributes()) {
			
			String atrName = atr.getName();

			double currQ = Double.NEGATIVE_INFINITY;
			
			if (primeMe.get(atrName) == null) {
				continue;
			}
			
			Double value = ex.getValue(atr);
			ClassificationMeasure precision = new ClassificationMeasure(ClassificationMeasure.Precision);
			ClassificationMeasure coverage = new ClassificationMeasure(ClassificationMeasure.Coverage);
			MetaValue candidate = null;
			
			for (MetaExample me : toSearch) {
				double Lplus = me.getCountOfRulesPointingToClass(atrName, toClass);
				double Lminus = me.getCountOfRulesPointingToClass(atrName, fromClass);
				
				MetaValue mv = me.get(atrName);
				if (mv.contains(value)) {
					continue;
					// the meta-value cannot cover the example
				}
				contraMe.add(mv);
				
				Pair<Covering, Covering> coverings = contraMe.getCoverage(examples, toClass, fromClass);
				Covering fromCov = coverings.getFirst();
				Covering toCov = coverings.getSecond();
				
				double precPlus = precision.calculate(toCov);
				double precMinus = precision.calculate(fromCov);
				
				double covPlus = coverage.calculate(toCov);
				double covMinus = coverage.calculate(fromCov);
				
				double quality =(Lplus * (precPlus * covPlus)) - (Lminus * (precMinus * covMinus));
				//double quality = Lplus - Lminus;
				Logger.log("Quality " + quality + " recorded for metaexample " + me, Level.INFO);
				if (quality >= currQ) {
					currQ = quality;
					candidate = mv;
				}
				
				contraMe.remove(mv);
			}
			
			if (candidate == null) {
				//try with next attribute
				continue;
			}
			
			contraMe.add(candidate);	

		}
		
		return new AnalysisResult(ex, primeMe, contraMe);
	}
}
