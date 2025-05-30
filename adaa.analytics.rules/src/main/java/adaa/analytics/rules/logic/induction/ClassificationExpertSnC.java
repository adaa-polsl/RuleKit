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

import java.util.*;
import java.util.logging.Level;

import adaa.analytics.rules.data.*;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.condition.CompoundCondition;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;
import adaa.analytics.rules.logic.representation.exampleset.ExampleSetFactory;
import adaa.analytics.rules.logic.representation.rule.RuleType;
import adaa.analytics.rules.logic.representation.ruleset.ClassificationRuleSet;
import adaa.analytics.rules.logic.representation.rule.ClassificationRule;
import adaa.analytics.rules.logic.representation.rule.Rule;
import adaa.analytics.rules.logic.representation.valueset.SingletonSet;
import adaa.analytics.rules.utils.Logger;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import tech.tablesaw.api.DoubleColumn;

/**
 * User-guided separate'n'conquer algorithm for generating classification rule sets.
 * 
 * @author Adam Gudys
 *
 */
public class ClassificationExpertSnC extends ClassificationSnC {

	/**
	 * User's knowledge.
	 */
	protected Knowledge knowledge;
	
	/**
	 * Invokes base class constructor, creates factory for generating 
	 * 
	 * @param finder
	 * @param params
	 * @param knowledge
	 */
	public ClassificationExpertSnC(ClassificationFinder finder, InductionParameters params, Knowledge knowledge) {
		super(finder, params);
		factory = new RuleFactory(RuleType.CLASSIFICATION,  params, knowledge);
		setFactory = new ExampleSetFactory(RuleType.CLASSIFICATION);
		this.knowledge = (Knowledge)SerializationUtils.clone(knowledge);
	}
	
	@Override
	public ClassificationRuleSet run(IExampleSet dataset)
	{
		Logger.log("ClassificationExpertSnC.run()\n", Level.FINE);
		double beginTime;
		beginTime = System.nanoTime();
		DoubleColumn weightDataColumnDoubleAdapter = dataset.getDoubleColumn(dataset.getAttributes().getWeight());
		DoubleColumn labelDataColumnDoubleAdapter = dataset.getDoubleColumn(dataset.getAttributes().getLabel());

		ClassificationRuleSet ruleset = (ClassificationRuleSet)factory.create(dataset);
		IAttribute outputAttr = dataset.getAttributes().getLabel();
		INominalMapping mapping = outputAttr.getMapping();
		List<String> labels = new ArrayList<>();
		labels.addAll(mapping.getValues());
		Collections.sort(labels);

		int totalExpertRules = 0;
		int totalAutoRules = 0;
		
		double defaultClassWeight = 0;

		// perform prepreprocessing
		finder.preprocess(dataset);
		
		// iterate over all classes
		for (String label : labels) {
			int classId = mapping.getIndex(label);
			Logger.log("Class " + label + " (" +classId + ") started\n" , Level.FINE);

			IntegerBitSet positives = new IntegerBitSet(dataset.size());
			IntegerBitSet negatives = new IntegerBitSet(dataset.size());
			IntegerBitSet uncoveredPositives = new IntegerBitSet(dataset.size());
			Set<Integer> uncovered = new HashSet<Integer>();
			double weighted_P = 0;
			double weighted_N = 0;

			// at the beginning rule set does not cover any examples
			for (int id = 0; id < dataset.size(); ++id) {
				double w = dataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDouble(id);

				if ((double)labelDataColumnDoubleAdapter.getDouble(id) == classId) {
					weighted_P += w;
					positives.add(id);
				} else {
					weighted_N += w;
					negatives.add(id);
				}
			}
			uncoveredPositives.addAll(positives);
			uncovered.addAll(positives);
			uncovered.addAll(negatives);

			// change default class if necessary
			if (weighted_P > defaultClassWeight) {
				defaultClassWeight = weighted_P;
				ruleset.setDefaultClass(label);
			}
			
			Knowledge classKnowledge = null;
			
			// if no expert rules and conditions specified
			if (knowledge.getRules(classId).size() == 0 
					&& knowledge.getPreferredConditions(classId).size() == 0
					&& knowledge.getPreferredAttributes(classId).size() == 0
					&& knowledge.getForbiddenAttributes(classId).size() == 0
					&& knowledge.getForbiddenAttributes(classId).size() == 0) {
				
				if (knowledge.isConsiderOtherClasses()) {
					// if flag specified allow induction for non-expert rules
					classKnowledge = (Knowledge) SerializationUtils.clone(knowledge);
					classKnowledge.setInduceUsingAutomatic(true);
				} else {
					continue;
				}
				
			} else {
				classKnowledge = knowledge;
				
			}
			
			// add expert rules to the ruleset and try to refine them
			Logger.log("Processing expert rules...\n", Level.FINE);
			for (Rule r : knowledge.getRules(classId)) {
				Rule rule = (Rule) SerializationUtils.clone(r);

				// rule covers everything at the beginning (adjustment phase corrects covering)
				rule.setWeighted_P(weighted_P);
				rule.setWeighted_N(weighted_N);
				rule.setWeighted_p(weighted_P);
				rule.setWeighted_n(weighted_N);

				rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
				rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
				rule.getCoveredPositives().addAll(positives);
				rule.getCoveredNegatives().addAll(negatives);

				rule.getConsequence().setCovering(positives);
				
				ClassificationExpertFinder erf = (ClassificationExpertFinder)finder;
				
				erf.adjust(rule, dataset, uncoveredPositives);

				Logger.log("Expert rule: " + rule.toString() + "\n", Level.FINE);
				
				erf.setKnowledge(classKnowledge);
				double t = System.nanoTime();
				finder.grow(rule, dataset, uncoveredPositives);
				ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
				
				if (params.isPruningEnabled()) {
					Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
					t = System.nanoTime();
					finder.prune(rule, dataset, uncoveredPositives);
					ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
				}
				Logger.log(".", Level.INFO);
				Logger.log("Candidate rule:" + rule.toString() + "\n", Level.FINE);

				finder.postprocess(rule, dataset);
				ruleset.addRule(rule);
				Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
				Logger.log("\t" + (++totalExpertRules) + " expert rules, " + totalAutoRules + " auto rules" , Level.INFO);

				// remove covered examples
				uncoveredPositives.removeAll(rule.getCoveredPositives());
				uncovered.removeAll(rule.getCoveredPositives());
				uncovered.removeAll(rule.getCoveredNegatives());
			}
			
			// try to generate new rules
			Logger.log("Processing other rules...\n", Level.FINE);
			boolean carryOn = uncoveredPositives.size() > 0; 
			while (carryOn) {
				Rule rule = new ClassificationRule(
						new CompoundCondition(),
						new ElementaryCondition(outputAttr.getName(), new SingletonSet((double)classId, mapping.getValues())));
				
				rule.setWeighted_P(weighted_P);
				rule.setWeighted_N(weighted_N);
				rule.setWeighted_p(weighted_P);
				rule.setWeighted_n(weighted_N);

				rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
				rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
				rule.getCoveredPositives().addAll(positives);
				rule.getCoveredNegatives().addAll(negatives);

				rule.getConsequence().setCovering(positives);
				
				ClassificationExpertFinder erf = (ClassificationExpertFinder)finder;
				erf.setKnowledge(classKnowledge);
				double t = System.nanoTime();
				carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
				ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
				
				if (carryOn) {
					if (params.isPruningEnabled()) {
						Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
						t = System.nanoTime();
						finder.prune(rule, dataset, uncoveredPositives);
						ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
					}
					Logger.log("Candidate rule:" + rule.toString() + "\n", Level.FINE);
					Logger.log(".", Level.INFO);
					
					// remove covered examples
					int previouslyUncovered = uncoveredPositives.size();
					uncoveredPositives.removeAll(rule.getCoveredPositives());
					uncovered.removeAll(rule.getCoveredPositives());
					uncovered.removeAll(rule.getCoveredNegatives());

					double uncovered_p = 0;
					if (dataset.getAttributes().getWeight() == null) {
						uncovered_p = uncoveredPositives.size();
					} else {
						for (int id : uncoveredPositives) {
							uncovered_p += weightDataColumnDoubleAdapter.getDouble(id);
						}
					}

					// stop if no positive examples remaining
					if (uncovered_p <= params.getMaximumUncoveredFraction() * weighted_P) {
						carryOn = false;
					}
					
					// stop and ignore last rule if no new positive examples covered
					if (uncoveredPositives.size() == previouslyUncovered) {
						carryOn = false; 
					} else {
						finder.postprocess(rule, dataset);
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
		}
			
		ruleset.setTotalTime((System.nanoTime() - beginTime) / 1e9);
		return ruleset;
	}
	

}
