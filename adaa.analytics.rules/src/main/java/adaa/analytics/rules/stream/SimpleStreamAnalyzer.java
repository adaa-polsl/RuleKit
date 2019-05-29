/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
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
