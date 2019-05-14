package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.AnyValueSet;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.IntegerBitSet;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;

import java.util.*;
import java.util.logging.Level;
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
				.map(x -> {Action a = new Action(premiseLeft.get(x), premiseLeft.get(x)); a.setActionNil(true); return a;})
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


	protected ExampleSet trainSet;
	protected Set<MetaExample> metaExamples;
	
	public ActionMetaTable(ActionRangeDistribution distribution) {
		trainSet = (ExampleSet) distribution.set.clone();
		metaExamples = cartesianProduct(distribution.getMetaValuesByAttribute());
	}

	public Set<MetaExample> getMetaExamples() {
		return metaExamples;
	}

	//For given example returns respective meta-example and contre-meta-example of opposite class
	public AnalysisResult analyze(Example ex, int fromClass, int toClass) {
		MetaExample primeMe = null;
		MetaExample contraMe = new MetaExample();
		
		primeMe = metaExamples.stream().filter(x -> x.covers(ex)).findFirst().orElse(null);
		
		if (primeMe == null) {
			throw new RuntimeException("The example ex was not covered by any metaexample");
		}
		
		Set<MetaExample> toSearch = new HashSet<MetaExample>(metaExamples);
		toSearch.remove(primeMe);
		//don't need to bother with examples not supported by any rule ???
	//	toSearch.removeIf(x -> x.getCountOfSupportingRules() == 0);
		
		///!!!!
		//TODO check if valid!!!
		// premise should be ranked on trainExamples, not test Examples!!!!
		//trainExamples = trainSet; is not final
		//changed the name everywhere - fall back to parameter if needed
	
		Set<Integer> coveredByContra = new IntegerBitSet(trainSet.size());
		Set<Integer> coveredPositive = new IntegerBitSet(trainSet.size());
		Set<Integer> coveredNegative = new IntegerBitSet(trainSet.size());
		
		contraMe.getCoverageForClass(trainSet, toClass, coveredPositive, coveredNegative);
		coveredByContra.addAll(coveredPositive);
		coveredByContra.addAll(coveredNegative);
		
		HashSet<String> allowedAttributes = new HashSet<String>();
		
		Iterator<Attribute> it = trainSet.getAttributes().allAttributes();
		
		it.forEachRemaining(x -> {
				if (x.equals(trainSet.getAttributes().getLabel())) { return;}
				allowedAttributes.add(x.getName());
			}
		);
		Logger.log("Looking for recommendation for example: " + ex + "\r\n", Level.FINE);
		
		
		boolean grown = true;
		double bestQ = rankMetaPremise(contraMe, fromClass, toClass, trainSet);
		Logger.log("Initial contre-meta-example is " + contraMe + " at quality " + bestQ + "\r\n", Level.FINE);
		while (grown) {
			
			MetaValue best = getBestMetaValue(allowedAttributes,
					contraMe, primeMe,
					toSearch,
					ex, trainSet,
					fromClass, toClass);
		
			if (best == null) {
				break;
			}
			contraMe.add(best);
			double currQ = rankMetaPremise(contraMe, fromClass, toClass, trainSet);
			
			if (currQ >= bestQ) {
				allowedAttributes.remove(best.getAttribute());
				
				bestQ = currQ;
				grown = true;
				Logger.log("Found best meta-value: " + best + " at quality " + bestQ + "\r\n", Level.FINE);
			} else {
				contraMe.remove(best);
				grown = false;
			}
			if (Double.compare(currQ, 1.0) == 0) {
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
				
				double q = rankMetaPremise(contraMe, fromClass, toClass, trainSet);
				
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

	
	//fill in the fittness function (quality measure for hill climbing)
	//maybe should be configurable by user
	private double rankMetaPremise(MetaExample metaPremise, int fromClass, 
			int toClass, ExampleSet examples) {
		Set<Integer> pos = new HashSet<Integer>();
		Set<Integer> neg = new HashSet<Integer>();
		Covering covering = metaPremise.getCoverageForClass(examples, toClass, pos, neg);

		if (covering.weighted_p < 1.0) {
			return 0.0;}
		//precision
		ClassificationMeasure precision = new ClassificationMeasure(ClassificationMeasure.Precision);
		
		double quality = precision.calculate(covering);
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
		
		//jaki zbiór bierzemy do oceny nowego meta-warunku?
		//powinien on chyba byæ ograniczony tlyko do przyk³adów, które s¹ ju¿ pokrywane przez 
		//rozwijan¹ w³aœnie kontra-metaprzes³ankê (poniewa¿ w innym wypadku to bêdzie bez sensu,
		//wk³ad do jakoœci potencjalnie bêd¹ mieæ rozdzielne grupy przyk³adów
		//w efekcie precyzja mo¿e nie wzrastaæ
		//jak i gdzie to filtrowaæ dok³adnie ?
		//trzeba to rozrysowaæ i rozpisaæ i zastanowiæ siê na sucho.
		
		//trzeba du¿o przerobæ niestety, bo metaexample musz¹ staæ siê bardziej jak regu³y (jesli chodzi o liczenie pokrycia)
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

	private Set<MetaExample> cartesianProduct(List<Set<MetaValue>> sets) {
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
