package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.actions.MetaExample.MetaCoverage;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.AnyValueSet;
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
			if (from == -1.0) {
				sourceClass = new AnyValueSet();
			}
			IValueSet targetClass = new SingletonSet((double)to, classAtr.getMapping().getValues());
			
			rule.setConsequence(new Action(classAtr.getName(), sourceClass, targetClass));
			
			return rule;
		}
	}

	protected int exampleSize;
	protected int tableSize;
	protected ExampleSet trainSet;
	protected Set<MetaExample> metaExamples;
	
	public ActionMetaTable(ActionRangeDistribution distribution) {
		trainSet = distribution.set;
		metaExamples = cartesianProduct(distribution.getMetaValuesByAttribute());
	}

	public Set<MetaExample> getMetaExamples() {
		return metaExamples;
	}

	protected double rankMetaPremise(MetaExample metaPremise, int fromClass, int toClass, ExampleSet examples) {
		ClassificationMeasure C2 = new ClassificationMeasure(ClassificationMeasure.C2);
		ClassificationMeasure prec = new ClassificationMeasure(ClassificationMeasure.Precision);

		
		Pair<Covering, Covering> coverings = metaPremise.getCoverage(examples, toClass, fromClass);
		Covering fromCov = coverings.getFirst();
		Covering toCov = coverings.getSecond();
		double precVal = prec.calculate(toCov);
		/*
		Pair<Covering, Covering> coverings = metaPremise.getCoverageDontCareForSource(examples, toClass);
		Covering fromCov = coverings.getFirst();
		Covering toCov = coverings.getSecond();
		*/
		//MetaCoverage mc = metaPremise.getMetaCoverage(toClass);
		//double c2Target = C2.calculate(toCov);
	//	if (true) return precVal;
		//if (c2Target < 0) {
	//		return Double.NEGATIVE_INFINITY;
	//	}
		
		//double quality = ((Lplus / allTarget) - (Lminus / allSource)) * c2Target;
		//double quality =(Lplus * (C2.calculate(toCov))) - (Lminus * (C2.calculate(fromCov)));
		
		//double quality =(mc.getCoverageOfTargetClass() * (C2.calculate(toCov))) - (mc.getCoverageNotOfTargetClass() * (C2.calculate(fromCov)));
	//	double quality = Lplus - Lminus;
		//double quality = metaPremise.getQualityOfRulesPointingToClass(toClass) - metaPremise.getQualityOfRulesPointingToClass(fromClass);
		
		double quality = toCov.weighted_p / fromCov.weighted_n;
		
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
		
		for (MetaExample meta : metas) {
			
			for (String attribute : allowedAttributes) {
				
				MetaValue cand = meta.get(attribute);
				
				if (cand == null || cand.contains(example)) {
					continue;
				}
				if (candidate != null && cand.equals(candidate) ) {
					continue;
				}
				
				contra.add(cand);
				
				double quality = rankMetaPremise(contra, fromClass, toClass, examples);

				Logger.log("Quality " + quality + " recorded for metaexample " + meta + "\r\n", Level.FINEST);
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
		//don't need to bother with examples not supported by any rule
		toSearch.removeIf(x -> x.getCountOfSupportingRules() == 0);
		
		HashSet<String> allowedAttributes = new HashSet<String>();
		
		Iterator<Attribute> it = trainExamples.getAttributes().allAttributes();
		
		it.forEachRemaining(x -> {
				if (x.equals(trainExamples.getAttributes().getLabel())) { return;}
				allowedAttributes.add(x.getName());
			}
		);
		Logger.log("Looking for recommendation for example: " + ex + "\r\n", Level.FINE);
		
		metaExamples.forEach(x -> x.recordStandardCoverage(trainExamples, toClass));
		
		boolean grown = true;
		double bestQ = rankMetaPremise(contraMe, fromClass, toClass, trainExamples);
		Logger.log("Initial contre-meta-example is " + contraMe + " at quality " + bestQ + "\r\n", Level.FINE);
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
				allowedAttributes.remove(best.getAttribute());
				
				bestQ = currQ;
				grown = true;
				Logger.log("Found best meta-value: " + best + " at quality " + bestQ + "\r\n", Level.FINE);
			} else {
				contraMe.remove(best);
				grown = false;
			}
		}
		/////pruning
		
		Set<String> attributes = new HashSet<String>(contraMe.getAttributeNames());
		boolean pruned = false;
		
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
		
		return new AnalysisResult(ex, primeMe, contraMe, fromClass, toClass, trainSet);
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
}
