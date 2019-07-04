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
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SimplePartitionBuilder;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.Model;

public abstract class AbstractStreamAnalyzer {

	protected RuleSetBase rules = null;
	protected int batchSize = 200;
	protected AbstractSeparateAndConquer snc;
	protected InductionParameters inductionParams;

	public AbstractStreamAnalyzer(AbstractSeparateAndConquer snc, int batchSize, InductionParameters params) {
		this.batchSize = batchSize;
		this.snc = snc;
		this.inductionParams = params;
	}

	public Model learn(ExampleSet exampleSet) {
		Model model = null;
		
		Attribute label = exampleSet.getAttributes().getLabel();
		int numClasses = label.getMapping().size();
		
		try {

			final int size = exampleSet.size();
			final int batchCount = size / batchSize;
			final int rest = size - (batchCount * batchSize);

			Partition partition = new Partition(batchCount, size, new SimplePartitionBuilder());
			SplittedExampleSet splitted = new SplittedExampleSet(exampleSet, partition);
			
			for (int i = 0; i < batchCount; i++) {

				splitted.selectSingleSubset(i);

				RuleSetBase rs = snc.run(splitted);
				
				/*if (rules == null) {
					rules = rs;
					rules.getRules().clear();
				} else {*/
				if (rules != null) {
					rules.getRules().forEach(r -> {
						Covering cov = r.covers(splitted);
						r.setWeighted_N(r.getWeighted_N() + cov.weighted_N);
						r.setWeighted_n(r.getWeighted_n() + cov.weighted_n);
						r.setWeighted_p(r.getWeighted_p() + cov.weighted_p);
						r.setWeighted_P(r.getWeighted_P() + cov.weighted_P);
					});
				}
				mergeRules(rs, numClasses);
				//}
			}

			model = rules;
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
		}

		return model;
	}

	// implements rule merging (fusion) strategy
	protected abstract void mergeRules(RuleSetBase rs, int numClasses);

}
