package adaa.analytics.rules.stream;

import adaa.analytics.rules.logic.induction.AbstractSeparateAndConquer;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleStreamAnalyzer extends AbstractStreamAnalyzer {

	public SimpleStreamAnalyzer(AbstractSeparateAndConquer snc, int batchSize) {
		super(snc, batchSize, null);
	}
	private float avgAcc = 0.0f;
	@Override
	protected void mergeRules(RuleSetBase rs, int numClasses) {
		avgAcc = 0.0f;
		ClassificationMeasure measure = new ClassificationMeasure(ClassificationMeasure.Accuracy);
		
		int ruleSetSize = rs.getRules().size();
		double[] measures = new double[ruleSetSize];
		int idx = 0;
		for (Rule rule : rs.getRules()) {
			measures[idx] = measure.calculate(rule.getWeighted_p(), rule.getWeighted_n(), 
					rule.getWeighted_P(), rule.getWeighted_N()); 
		}
		
		List<Rule> rl = rs.getRules();
		
		int[] bestIndices = new int[numClasses];
		
		for (int i = 0; i < numClasses; i++) {
		
			bestIndices[i] = -1;
		}
		
		for (int j = 0; j < measures.length; j++) {
			
			Rule rule = rl.get(j);
			
			int classIdx = -1;
			
			for (int i = 0; i < numClasses; i++) {
				
				if(rule.getConsequence().getValueSet().contains((double)i)) {
					classIdx = i;
					
					if (bestIndices[classIdx] == -1) {
						bestIndices[classIdx] = j;
						break;
					}
					
					double currMeasure = measures[bestIndices[classIdx]];
					double ruleMeasure = measures[j] ;
					
					if (ruleMeasure > currMeasure) {
						bestIndices[classIdx] = j;
						break;
					}
				}
			}	
		}
		
		List<Rule> best = new ArrayList<Rule>();
		
		for (int index : bestIndices) {
			best.add(rl.get(index));
		}
		
		//this.rules.getRules().clear();
		//rl.stream().forEach(this.rules::addRule);
		if (this.rules == null) {
			this.rules = rs;
			this.rules.getRules().clear();
		}
		
		
		rl.stream().forEach(this.rules::addRule);
		
		double[] acc = new double[this.rules.getRules().size()];
		avgAcc = 0.0f;
		for (int i = 0; i < acc.length; i++) {
			Rule rule = rules.getRules().get(i);
			double m =  measure.calculate(rule.getWeighted_p(), rule.getWeighted_n(), 
					rule.getWeighted_P(), rule.getWeighted_N());
			avgAcc += m;
			acc[i] = m;
		}
		
		avgAcc /= acc.length;
		List<?super List<Rule>> newRules = new ArrayList<List<Rule>>();
		for (int i = 0; i < numClasses; i++) {
			
			final int k = i;
			List<Rule> rr = rules.getRules().stream()
					.filter(r -> r.getConsequence().getValueSet().contains(k))
					.filter(
					 r -> measure.calculate(
							 r.getWeighted_p(), r.getWeighted_n(),
							 r.getWeighted_P(), r.getWeighted_N()) < avgAcc - 0.3*avgAcc 
					 )
					.collect(Collectors.toList());
			newRules.add(rr);
		}
		
		this.rules.getRules().clear();
		
		for (int i = 0; i < numClasses; i++) {
			for (Rule r : (List<Rule>)newRules.get(i)) {
				this.rules.addRule(r);
			}
		}
	}

}
