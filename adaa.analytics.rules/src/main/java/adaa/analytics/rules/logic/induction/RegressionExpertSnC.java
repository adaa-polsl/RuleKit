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
import adaa.analytics.rules.logic.representation.condition.CompoundCondition;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;
import adaa.analytics.rules.logic.representation.exampleset.ExampleSetFactory;
import adaa.analytics.rules.logic.representation.exampleset.RegressionExampleSet;
import adaa.analytics.rules.logic.representation.rule.RuleType;
import adaa.analytics.rules.logic.representation.ruleset.RegressionRuleSet;
import adaa.analytics.rules.logic.representation.ruleset.RuleSetBase;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.logic.representation.rule.Rule;
import adaa.analytics.rules.logic.representation.valueset.SingletonSet;
import adaa.analytics.rules.utils.Logger;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import tech.tablesaw.api.DoubleColumn;

import java.util.Set;
import java.util.logging.Level;

/**
 * User-guided separate'n'conquer algorithm for generating regression rule sets.
 *
 * @author Adam Gudys
 *
 */
public class RegressionExpertSnC extends RegressionSnC {

	protected Knowledge knowledge;
	
	public RegressionExpertSnC(RegressionFinder finder,
			InductionParameters params,
			Knowledge knowledge) {
		super(finder, params);
		factory = new RuleFactory(RuleType.REGRESSION,  params, knowledge);
		setFactory = new ExampleSetFactory(RuleType.REGRESSION);
		this.knowledge = (Knowledge)SerializationUtils.clone(knowledge);
		RegressionExpertFinder erf = (RegressionExpertFinder)finder;
		erf.setKnowledge(this.knowledge);
	}
	
	@Override
	public RuleSetBase run(final IExampleSet dataset) {
		
		Logger.log("RegressionExpertSnC.run()\n", Level.FINE);
		double beginTime;
		beginTime = System.nanoTime();

		IExampleSet sortedDataset = setFactory.create(dataset);
		RuleSetBase ruleset = factory.create(sortedDataset);
		finder.preprocess(dataset);
		IAttribute label = dataset.getAttributes().getLabel();
		//SortedExampleSet ses = new SortedExampleSetEx(dataset, label, SortedExampleSet.INCREASING);

		sortedDataset.getAttributes().getLabel().recalculateStatistics();
		
		if (factory.getType() == RuleType.REGRESSION) {
			double median = sortedDataset.getExample(sortedDataset.size() / 2).getLabelValue();
			RegressionRuleSet tmp = (RegressionRuleSet)ruleset;
			tmp.setDefaultValue(median);
		}
		
		//Set<Integer> uncovered = new HashSet<Integer>();
		Set<Integer> uncovered = new IntegerBitSet(dataset.size());
		double weighted_PN = 0;
		// at the beginning rule set does not cover any examples
		DoubleColumn weightDataColumnDoubleAdapter = sortedDataset.getDoubleColumn(sortedDataset.getAttributes().getWeight());

		for (int id = 0; id < sortedDataset.size(); ++id) {
			uncovered.add(id);
			double w = sortedDataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDouble(id);
			weighted_PN += w;
		}
		
		int totalExpertRules = 0;
		int totalAutoRules = 0;
		boolean carryOn = true; 
		double uncovered_pn = weighted_PN;
		Logger.log("Processing expert rules...\n", Level.FINE);
		RegressionExpertFinder erf = (RegressionExpertFinder)finder;

		// add expert rules to the ruleset and try to refine them
		for (Rule r : knowledge.getRules()) {
			Logger.log("Uncovered positive weight: " + uncovered_pn +  "/" + weighted_PN + "\n", Level.FINE);
			Rule rule = (Rule) SerializationUtils.clone(r);

			// rule covers everything at the beginning (adjustment phase corrects covering)
			rule.setWeighted_P(weighted_PN);
			rule.setWeighted_N(0);
			rule.setWeighted_p(weighted_PN);
			rule.setWeighted_n(0);

			rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
			rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
			rule.getCoveredPositives().setAll();

			erf.adjust(rule, sortedDataset, uncovered);

			Logger.log("Expert rule: " + rule.toString() + "\n", Level.FINE);
			double t = System.nanoTime();
			finder.grow(rule, sortedDataset, uncovered);
			ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
						
			if (params.isPruningEnabled()) {
				Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
				t = System.nanoTime();
				finder.prune(rule, sortedDataset, uncovered);
				ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
			}
			Logger.log("Candidate rule: " + rule.toString() + "\n", Level.FINE);

			Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
			Logger.log("\t" + totalExpertRules + " expert rules, " + (++totalAutoRules) + " auto rules" , Level.INFO);

			finder.postprocess(rule, sortedDataset);
			ruleset.addRule(rule);

			// remove examples covered by the rule and update statistics
			uncovered.removeAll(rule.getCoveredPositives());
			uncovered.removeAll(rule.getCoveredNegatives());
			uncovered_pn = 0;
			for (int id : uncovered) {
				uncovered_pn += sortedDataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDouble(id);
			}
		}
		
		// try to generate new rules
		Logger.log("Processing other rules...\n", Level.FINE);
		carryOn = uncovered.size() > 0;
		Covering cov = new Covering();

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

				// remove covered examples
				int previouslyUncovered = uncovered.size();
				uncovered.removeAll(rule.getCoveredPositives());
				uncovered.removeAll(rule.getCoveredNegatives());
				
				uncovered_pn = 0;
				for (int id : uncovered) {
					uncovered_pn += sortedDataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDouble(id);
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
					Logger.log("\t" + totalExpertRules + " expert rules, " + (++totalAutoRules) + " auto rules" , Level.INFO);
				}

				//report to operator command proxy
				this.operatorCommandProxy.onNewRule(rule);
				this.operatorCommandProxy.onProgressChange(dataset.size(), uncovered.size());
			}

			if (this.operatorCommandProxy.isRequestStop()) {
				carryOn = false;
			}
		}
		
		ruleset.setTotalTime((System.nanoTime() - beginTime) / 1e9);
		return ruleset;
	}

}
