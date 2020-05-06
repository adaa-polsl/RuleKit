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
package adaa.analytics.rules.logic.induction;


import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.*;
import com.rapidminer.tools.Ontology;

import java.util.*;

/**
 *  Separate'n'conquer algorithm for generating survival rule sets using transformation to classification problem.
 * 	@author Adam Gudys
 *
 */
public class SurvivalClassificationSnC extends ClassificationSnC {
	
	protected DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY);
	
	public SurvivalClassificationSnC(ClassificationFinder finder, InductionParameters params) {
		super(finder, params);
	}

	@Override
	public RuleSetBase run(ExampleSet trainSet) {
	
		SurvivalRuleSet survSet = new SurvivalRuleSet(trainSet, true, params, null);
		
		Attribute survTime = trainSet.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE);
		Attribute survStat = trainSet.getAttributes().getLabel();
	
		// get Tmax (max time for censored observations
		double Tmax = 0;
		for (Example ex : trainSet) {
			if (ex.getValue(survStat) == 0 && ex.getValue(survTime) > Tmax) {
				Tmax = ex.getValue(survTime);
			}
		}
		double STmax = survSet.getTrainingEstimator().getProbabilityAt(Tmax);
		
		// prepare classification dataset
		Attributes survivalAttributes = (Attributes) trainSet.getAttributes().clone();
		survivalAttributes.remove(survivalAttributes.findRoleBySpecialName(SurvivalRule.SURVIVAL_TIME_ROLE));
		survivalAttributes.remove(survivalAttributes.getLabel());
		
		Attribute survivalWeight = AttributeFactory.createAttribute("survivalWeight", Ontology.NUMERICAL);
		
		Attribute label = AttributeFactory.createAttribute("survivalStatus", Ontology.BINOMINAL);
		NominalMapping mp = new BinominalMapping();
		mp.setMapping("not_survived", 0);
		mp.setMapping("survived", 1);
		label.setMapping(mp);
		
		survivalAttributes.setWeight(survivalWeight);
		survivalAttributes.setLabel(label);
		
		List<Attribute> lst = new ArrayList<Attribute>();
		Iterator<Attribute> it = survivalAttributes.allAttributes();
		
		while (it.hasNext()) {
			Attribute a = it.next();
			lst.add(a);
		}
	
		MemoryExampleTable table = new MemoryExampleTable(lst);
		ExampleSet transformed = table.createExampleSet(label, survivalWeight, null);

		for (Example example : trainSet) {
			
			// create positive anyways
			DataRow positive = factory.create(transformed.getAttributes().allSize());
			
			// if observation is censored, create also negative
			DataRow negative = null;
			if (example.getValue(survStat) == 0) {
				negative = factory.create(transformed.getAttributes().allSize());
			}
			
			// fill conditional attributes
			for (Attribute a: table.getAttributes()) {
				if (a != label && a != survivalWeight) {
					// get corresponding attribute from training table
					Attribute ta = trainSet.getAttributes().get(a.getName());
					positive.set(a, example.getValue(ta));
					if (negative != null) { 
						negative.set(a, example.getValue(ta));
					}
				}
			}
			
			if (negative != null) {
				double Ti = example.getValue(survTime);
				double STi = survSet.getTrainingEstimator().getProbabilityAt(Ti);
				double w = STmax / STi;

				positive.set(survivalWeight, 1 - w);
				negative.set(survivalWeight, w);

				negative.set(label, 0.0);

				table.addDataRow(negative);
			} else {
				positive.set(survivalWeight, 1.0);
			}
			
			positive.set(label, 1.0);
			table.addDataRow(positive);
		//	break;
		}

		ClassificationRuleSet set = (ClassificationRuleSet) super.run(transformed);
		
		for (Rule r : set.getRules()) {
			Covering cov = r.covers(trainSet);
			Set<Integer> indices = new HashSet<Integer>();
			indices.addAll(cov.positives);
			indices.addAll(cov.negatives);
			KaplanMeierEstimator kms = new KaplanMeierEstimator(trainSet, indices);
			
			SurvivalRule sr = new SurvivalRule((ClassificationRule)r, kms); 
			survSet.addRule(sr);
		}
		
		return survSet;
	}

}
