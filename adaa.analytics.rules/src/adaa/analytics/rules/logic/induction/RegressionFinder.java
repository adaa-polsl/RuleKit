package adaa.analytics.rules.logic.induction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;

import adaa.analytics.rules.logic.induction.AbstractFinder.QualityAndPValue;
import adaa.analytics.rules.logic.quality.ChiSquareVarianceTest;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.StatisticalTestResult;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;

/**
 * Algorithm for growing and pruning regression rules.
 * @author Adam
 *
 */
public class RegressionFinder extends AbstractFinder {
	
	/**
	 * Helper class for storing information about evaluated condition. 
	 * @author Adam
	 *
	 */
	class ConditionEvaluation {
		public ConditionBase condition = null;
		public Covering covering = null;
		public double quality = -Double.MAX_VALUE;
		public double covered = 0;
	}
	
	public RegressionFinder(final InductionParameters params) {
		super(params);
	}
	
	protected ElementaryCondition induceCondition(
		Rule rule,
		ExampleSet dataset,
		Set<Integer> uncovered, 
		Set<Integer> covered, 
		Set<Attribute> allowedAttributes) {
		
		ConditionEvaluation bestEvaluation = new ConditionEvaluation();
		Attribute ignoreCandidate = null;
		
		// iterate over all possible decision attributes
		for (Attribute attr : allowedAttributes) {
			Logger.log("Analysing attribute: " + attr.getName() + "\n", Level.FINEST);
			
			// check if attribute is numerical or nominal
			if (attr.isNumerical()) {
				Map<Double, List<Integer>> values2ids = new TreeMap<Double, List<Integer>>();
				
				// get all distinctive values of attribute
				for (int id : covered) {
					Example ex = dataset.getExample(id);
					double val = ex.getValue(attr);
					
					if (!values2ids.containsKey(val)) {
						values2ids.put(val, new ArrayList<Integer>());
					} 
					values2ids.get(val).add(id);
				}
				
				Double [] keys = values2ids.keySet().toArray(new Double[values2ids.size()]);
	
				// check all possible midpoints
				for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
					double key = keys[keyId];
					double next = keys[keyId + 1];
					double midpoint = (key + next) / 2;
					
					// evaluate left-side condition a < v
					ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint)); 
					if (checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation)) {
						ignoreCandidate = null;
					}
					
					// evaluate right-side condition v <= a
					candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint)); 
					if (checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation)) {
						ignoreCandidate = null;
					}
				}
			} else {
				// try all possible conditions
				for (int i = 0; i < attr.getMapping().size(); ++i) {
					ElementaryCondition candidate = new ElementaryCondition(
							attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues())); 
					
					if (checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation)) {
						ignoreCandidate = attr;
					}
				}
			}
		}
		
		if (ignoreCandidate != null) {
			allowedAttributes.remove(ignoreCandidate);
		}

		return (ElementaryCondition)bestEvaluation.condition;
	}

	
	protected boolean checkCandidate(
			ExampleSet dataset, 
			Rule rule,
			ElementaryCondition candidate,
			Set<Integer> uncovered, 
			ConditionEvaluation currentBest) {
		return checkCandidateCoverage(dataset, rule, candidate, uncovered, currentBest);
	}
	
	protected boolean checkCandidateCoverage(
			ExampleSet dataset, 
			Rule rule,
			ConditionBase candidate,
			Set<Integer> uncovered, 
			ConditionEvaluation currentBest) {
		
		Logger.log("Evaluating candidate: " + candidate, Level.FINEST);
		rule.getPremise().addSubcondition(candidate);
		Covering cov = rule.covers(dataset);
		rule.getPremise().removeSubcondition(candidate);
		
		double newlyCovered = 0;
		if (dataset.getAttributes().getWeight() == null) {
			// unweighted examples
			newlyCovered = SetHelper.intersectionSize(uncovered, cov.positives) +
					SetHelper.intersectionSize(uncovered, cov.negatives);
		} else {
			// calculate weights of newly covered examples
			for (int id : cov.positives) {
				newlyCovered += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
			}
			for (int id : cov.negatives) {
				newlyCovered += uncovered.contains(id) ? dataset.getExample(id).getWeight() : 0;
			}
		}
		
		if (newlyCovered > 0 && newlyCovered >= params.getMinimumCovered()) {
			double quality = calculateQuality(dataset, cov, params.getInductionMeasure());
			
			Logger.log(", q=" + quality, Level.FINEST);
			
			if (quality > currentBest.quality || (quality == currentBest.quality && newlyCovered > currentBest.covered)) {
				currentBest.quality = quality;
				currentBest.condition = candidate;
				currentBest.covered = newlyCovered;
				currentBest.covering = cov;
				Logger.log(", approved!\n", Level.FINEST);
				//rule.setWeight(quality);
				return true;
			} 
		}
		
		Logger.log("\n", Level.FINEST);
		return false;
	}
	
	@Override
	protected QualityAndPValue calculateQualityAndPValue(ExampleSet trainSet, Covering cov, IQualityMeasure measure) {
		QualityAndPValue res = new QualityAndPValue();
		res.quality = super.calculateQuality(trainSet, cov, measure);
		
/*		double[] covY = new double[cov.getSize()];
		double[] uncovY = new double[trainSet.size() - cov.getSize()];
		
		int ic = 0;
		int iuc = 0;
		
		for (int ie = 0; ie < trainSet.size(); ++ie) {
			 
			if (cov.positives.contains(ie) || cov.negatives.contains(ie)) {
				covY[ic++] = trainSet.getExample(ie).getLabel();
			} else {
				uncovY[iuc++] = trainSet.getExample(ie).getLabel();
			}
		}
		
		MannWhitneyUTest test = new MannWhitneyUTest();
		res.pvalue = test.mannWhitneyUTest(covY, uncovY);
		
		//KolmogorovSmirnovTest test = new KolmogorovSmirnovTest();
		//res.pvalue = test.kolmogorovSmirnovTest(covY, uncovY);
*/
		ChiSquareVarianceTest test = new ChiSquareVarianceTest();
		double expectedDev = Math.sqrt(trainSet.getStatistics(trainSet.getAttributes().getLabel(), Statistics.VARIANCE));
		StatisticalTestResult sts = test.calculateLower(expectedDev, cov.stddev_y, cov.getSize());
		
		res.pvalue =  sts.pvalue;
		
		return res;
	}
}
