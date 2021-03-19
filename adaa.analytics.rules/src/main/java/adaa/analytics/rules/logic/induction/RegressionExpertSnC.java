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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import adaa.analytics.rules.logic.representation.*;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.tools.container.Pair;

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
		factory = new RuleFactory(RuleFactory.REGRESSION, true, params, knowledge);
		this.knowledge = (Knowledge)SerializationUtils.clone(knowledge);
		RegressionExpertFinder erf = (RegressionExpertFinder)finder;
		erf.setKnowledge(this.knowledge);
	}
	
	@Override
	public RuleSetBase run(final ExampleSet dataset) {
		
		Logger.log("RegressionExpertSnC.run()\n", Level.FINE);
		double beginTime;
		beginTime = System.nanoTime();
		
		RuleSetBase ruleset = factory.create(dataset);
		Attribute label = dataset.getAttributes().getLabel();
		SortedExampleSet ses = new SortedExampleSet(dataset, label, SortedExampleSet.INCREASING);
		ses.recalculateAttributeStatistics(ses.getAttributes().getLabel());
		
		if (factory.getType() == RuleFactory.REGRESSION) {
			double median = ses.getExample(ses.size() / 2).getLabel();
			RegressionRuleSet tmp = (RegressionRuleSet)ruleset;
			tmp.setDefaultValue(median);
		}
		
		Set<Integer> uncovered = new HashSet<Integer>();
		double weighted_PN = 0;
		// at the beginning rule set does not cover any examples
		for (int id = 0; id < ses.size(); ++id) {
			uncovered.add(id);
			Example ex = ses.getExample(id);
			double w = ses.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
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

			erf.adjust(rule, dataset, uncovered);

			Logger.log("Expert rule: " + rule.toString() + "\n", Level.FINE);
			double t = System.nanoTime();
			finder.grow(rule, ses, uncovered);
			ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
						
			if (params.isPruningEnabled()) {
				Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
				t = System.nanoTime();
				finder.prune(rule, ses, uncovered);
				ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
			}
			Logger.log("Candidate rule: " + rule.toString() + "\n", Level.FINE);

			Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
			Logger.log("\t" + totalExpertRules + " expert rules, " + (++totalAutoRules) + " auto rules" , Level.INFO);

			finder.postprocess(rule, ses);
			ruleset.addRule(rule);

			// remove examples covered by the rule and update statistics
			uncovered.removeAll(rule.getCoveredPositives());
			uncovered.removeAll(rule.getCoveredNegatives());
			uncovered_pn = 0;
			for (int id : uncovered) {
				Example e = ses.getExample(id);
				uncovered_pn += ses.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
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
			carryOn = (finder.grow(rule, ses, uncovered) > 0);
			ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
			
			if (carryOn) {
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
					t = System.nanoTime();
					finder.prune(rule, ses, uncovered);
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
					Example e = ses.getExample(id);
					uncovered_pn += ses.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
				}
				
				// stop if number of examples remaining is less than threshold
				if (uncovered_pn <= params.getMaximumUncoveredFraction() * weighted_PN) {
					carryOn = false; 
				}
				
				// stop and ignore last rule if no new examples covered
				if (uncovered.size() == previouslyUncovered) {
					carryOn = false; 
				} else {
					finder.postprocess(rule, ses);
					ruleset.addRule(rule);
					Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
					Logger.log("\t" + totalExpertRules + " expert rules, " + (++totalAutoRules) + " auto rules" , Level.INFO);
				}
			}
		}
		
		ruleset.setTotalTime((System.nanoTime() - beginTime) / 1e9);
		return ruleset;
	}

}
