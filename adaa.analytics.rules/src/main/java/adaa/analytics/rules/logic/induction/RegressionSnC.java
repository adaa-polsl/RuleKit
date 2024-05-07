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

import adaa.analytics.rules.data.DataColumnDoubleAdapter;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.RegressionRuleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.logging.Level;

/**
 * Separate'n'conquer algorithm for generating regression rule sets.
 * @author Adam
 *
 */
public class RegressionSnC extends AbstractSeparateAndConquer {

	protected final RegressionFinder finder;

	public RegressionSnC(final RegressionFinder finder, final InductionParameters params) {
		super(params);
		this.finder = finder;
		factory = new RuleFactory(RuleFactory.REGRESSION,  params, null);
	}

	@Override
	public RuleSetBase run(final IExampleSet dataset) {

		Logger.log("RegressionSnC.run()\n", Level.FINE);

		SortedExampleSetEx sortedDataset = (SortedExampleSetEx)finder.preprocess(dataset);
		RuleSetBase ruleset = factory.create(sortedDataset);
		IAttribute label = dataset.getAttributes().getLabel();
		//SortedExampleSetEx ses = new SortedExampleSetEx(dataset, label, SortedExampleSet.INCREASING);

		sortedDataset.recalculateAttributeStatistics(sortedDataset.getAttributes().getLabel());
			
		if (factory.getType() == RuleFactory.REGRESSION) {
			double median = sortedDataset.getExample(sortedDataset.size() / 2).getLabel();
			RegressionRuleSet tmp = (RegressionRuleSet)ruleset;
			tmp.setDefaultValue(median); // use this even in mean-based variant
		}
		
		//Set<Integer> uncovered = new HashSet<Integer>();
		Set<Integer> uncovered = new IntegerBitSet(sortedDataset.size());
		double weighted_PN = 0;
		DataColumnDoubleAdapter weightDataColumnDoubleAdapter = sortedDataset.getDataTable().getDataColumnDoubleAdapter(sortedDataset.getAttributes().getWeight(), Double.NaN);

		// at the beginning rule set does not cover any examples
		for (int id = 0; id < sortedDataset.size(); ++id) {
			uncovered.add(id);
			double w = sortedDataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDoubleValue(id);
			weighted_PN += w;
		}
		
		int totalRules = 0;
		boolean carryOn = true; 
		double uncovered_pn = weighted_PN;
		
		while (carryOn) {
			Logger.log("Uncovered positive weight: " + uncovered_pn +  "/" + weighted_PN + "\n", Level.FINE);
			
			Rule rule = factory.create(
					new CompoundCondition(),
					new ElementaryCondition(label.getName(), new SingletonSet(Double.NaN, null)));

			// rule covers everything at the beginning
			rule.setWeighted_P(weighted_PN);
			rule.setWeighted_N(0);
			rule.setWeighted_p(weighted_PN);
			rule.setWeighted_n(0);

			rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
			rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
			rule.getCoveredPositives().setAll();
			rule.setRuleOrderNum(ruleset.getRules().size());

			double t = System.nanoTime();
			carryOn = (finder.grow(rule, sortedDataset, uncovered) > 0);
			ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
			
			if (carryOn) {
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
					t = System.nanoTime();
					finder.prune(rule, sortedDataset, uncovered);
					ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
				}
				Logger.log("Candidate rule: " + rule.toString() + "\n", Level.FINE);
				Logger.log(".", Level.INFO);

				Covering covering = new Covering();
				rule.covers(sortedDataset, covering, covering.positives, covering.negatives);
				
				// remove covered examples
				int previouslyUncovered = uncovered.size();
				uncovered.removeAll(covering.positives);
				uncovered.removeAll(covering.negatives);
				
				uncovered_pn = 0;
				for (int id : uncovered) {
					uncovered_pn += dataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDoubleValue(id);
				}
				
				// stop if number of examples remaining is less than threshold
				if (uncovered_pn <= params.getMaximumUncoveredFraction() * weighted_PN) {
					carryOn = false; 
				}
				
				// stop and ignore last rule if no new examples covered
				if (uncovered.size() == previouslyUncovered) {
					carryOn = false; 
				} else {
					finder.postprocess(rule, sortedDataset);
					ruleset.addRule(rule);
					Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
					Logger.log("\t" + (++totalRules) + " rules" , Level.INFO);
				}

				//report to operator command proxy
				this.operatorCommandProxy.onNewRule(rule);
				this.operatorCommandProxy.onProgressChange(dataset.size(), uncovered.size());
			}

			if (this.operatorCommandProxy.isRequestStop()) {
				carryOn = false;
			}
		}

		return ruleset;
	}
}
