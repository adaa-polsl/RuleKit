package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.renjin.repackaged.guava.collect.Sets;

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
		private int from;
		private int to;
		private ExampleSet sourceExamples;
		
		public AnalysisResult(Example ex, MetaExample prime, MetaExample contre, int fromClass, int toClass, ExampleSet set) {
			example = ex;
			primeMetaExample = prime;
			contraMetaExample = contre;
			from = fromClass;
			to = toClass;
			sourceExamples = set;
		}
		
		public ActionRule getActionRule() {
			
			ActionRule rule = new ActionRule();
			rule.setPremise(new CompoundCondition());
			
			Map<String, ElementaryCondition> premiseLeft = primeMetaExample.toPremise();
			Map<String, ElementaryCondition> premiseRight = contraMetaExample.toPremise();
			
			//full actions
			Sets.intersection(premiseLeft.keySet(), premiseRight.keySet())
				.stream()
				.map(x -> new Action(premiseLeft.get(x), premiseRight.get(x)))
				.forEach(x -> rule.getPremise().addSubcondition(x));
			
			//handle cases when right (contra meta example) does not contain meta-value for given attribute
			Sets.difference(premiseLeft.keySet(), premiseRight.keySet())
				.stream()
				.map(x -> new Action(premiseLeft.get(x), premiseLeft.get(x)))
				.forEach(x -> rule.getPremise().addSubcondition(x));
			
			
			Attribute classAtr = sourceExamples.getAttributes().get("class");
			
			IValueSet sourceClass = new SingletonSet((double)from, classAtr.getMapping().getValues());
			IValueSet targetClass = new SingletonSet((double)to, classAtr.getMapping().getValues());
			
			rule.setConsequence(new Action(classAtr.getName(), sourceClass, targetClass));
			
			return rule;
		}
	}
	
	private double rankMetaPremise(MetaExample metaPremise, int fromClass, int toClass, ExampleSet examples) {
		ClassificationMeasure C2 = new ClassificationMeasure(ClassificationMeasure.C2);
		
		//double Lplus = metaPremise.getCountOfRulesPointingToClass(toClass);
		//double Lminus = metaPremise.getCountOfRulesPointingToClass(fromClass);
		
		double Lplus = metaPremise.getMetaCoverageValue(toClass);
		double Lminus = metaPremise.getMetaCoverageValue(fromClass);
		
		Pair<Covering, Covering> coverings = metaPremise.getCoverage(examples, toClass, fromClass);
		Covering fromCov = coverings.getFirst();
		Covering toCov = coverings.getSecond();
		
		double quality =(Lplus * (C2.calculate(toCov))) - (Lminus * (C2.calculate(fromCov)));
	//	double quality = Lplus - Lminus;
		//double quality = metaPremise.getQualityOfRulesPointingToClass(toClass) - metaPremise.getQualityOfRulesPointingToClass(fromClass);
		return quality;
	}
	
	private MetaValue getBestMetaValue(Set<String> allowedAttributes, 
			MetaExample contra,
			MetaExample prime,
			Set<MetaExample> metas,
			Example example,
			ExampleSet examples,
			int fromClass,
			int toClass) {
		
		MetaValue candidate = null;
		double Q = Double.NEGATIVE_INFINITY;
		double exampleValue;
		
		for (MetaExample meta : metas) {
			
			for (String attribute : allowedAttributes) {
				
				exampleValue = example.getValue(example.getAttributes().get(attribute));
				MetaValue cand = meta.get(attribute);
				
				if (cand == null || cand.contains(exampleValue)) {
					continue;
				}
				
				contra.add(cand);
				
				double quality = rankMetaPremise(contra, fromClass, toClass, examples);

				Logger.log("Quality " + quality + " recorded for metaexample " + meta, Level.INFO);
				if (quality >= Q) {
					Q = quality;
					candidate = cand;
				}
				
				contra.remove(cand);
			}
		}

		return candidate;
	}
	
	
	//For given example returns respective meta-example and contre-meta-example of opposite class
	public AnalysisResult analyze(Example ex, int fromClass, int toClass, ExampleSet trainExamples) {
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
		
		HashSet<String> allowedAttributes = new HashSet<String>();
		
		Iterator<Attribute> it = trainExamples.getAttributes().allAttributes();
		
		it.forEachRemaining(x -> {
				if (x.equals(trainExamples.getAttributes().getLabel())) { return;}
				allowedAttributes.add(x.getName());
			}
		);
		
		boolean grown = true;
		double bestQ = rankMetaPremise(contraMe, fromClass, toClass, trainExamples);
		while (grown) {
			
			MetaValue best = getBestMetaValue(allowedAttributes,
					contraMe, primeMe,
					toSearch,
					ex, trainExamples,
					fromClass, toClass);
		
			if (best == null) {
				break;
			}
			contraMe.add(best);
			double currQ = rankMetaPremise(contraMe, fromClass, toClass, trainExamples);
			
			if (currQ >= bestQ) {
				allowedAttributes.remove(best.value.getAttribute());
				bestQ = currQ;
				grown = true;
			} else {
				contraMe.remove(best);
				grown = false;
			}
		}
		/////pruning
		
		Set<String> attributes = new HashSet<String>(contraMe.getAttributeNames());
		boolean pruned = true;
		
		while (pruned) {
			MetaValue candidateToRemoval = null;
			double currQ = 0.0;
			for(String atr : attributes) {
				
				MetaValue cand = contraMe.get(atr);
				if (cand == null) {
					continue;
				}
				
				contraMe.remove(cand);
				
				double q = rankMetaPremise(contraMe, fromClass, toClass, trainExamples);
				
				if (q >= currQ) {
					currQ = q;
					candidateToRemoval = cand;
				}
				
				contraMe.add(cand);
			}
			
			if (candidateToRemoval != null && currQ >= bestQ) {
				contraMe.remove(candidateToRemoval);
				bestQ = currQ;
			} else {
				pruned = false;
			}
			
		}
		
		return new AnalysisResult(ex, primeMe, contraMe, fromClass, toClass, dist.set);
	}
}
