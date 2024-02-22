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
import adaa.analytics.rules.logic.representation.model.ClassificationRuleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.INominalMapping;
import adaa.analytics.rules.rm.tools.container.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Separate'n'conquer algorithm for generating classification rule sets.
 * @author Adam Gudys
 *
 */
public class ClassificationSnC extends AbstractSeparateAndConquer {
	
	/**
	 * Module for finding single classification rules.
	 */
	protected AbstractFinder finder;

	protected int numClassThreads = 1;

	public ClassificationSnC(AbstractFinder finder, InductionParameters params) {
		super(params);
		this.finder = finder;
		this.factory = new RuleFactory(RuleFactory.CLASSIFICATION, true, params, null);
	}
	
	/**
	 * Generates a classification rule set on the basis of a training set.
	 * @param dataset Training data set.
	 * @return Rule set.
	 */
	public RuleSetBase run(IExampleSet dataset) {
		Logger.log("ClassificationSnC.run()\n", Level.FINE);

		ClassificationRuleSet finalRuleset = (ClassificationRuleSet) factory.create(dataset);

		// use contrast attribute if specified
		final IAttribute outputAttr = (dataset.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
				? dataset.getAttributes().getLabel()
				: dataset.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

		INominalMapping mapping = outputAttr.getMapping();

		boolean weighted = (dataset.getAttributes().getWeight() != null);

		ExecutorService pool = Executors.newFixedThreadPool(numClassThreads);
		Semaphore mutex = new Semaphore(1);
		AtomicInteger totalRules = new AtomicInteger(0);

		// perform prepreprocessing
		finder.preprocess(dataset);

		// array of futures, each consisting of ruleset and P value
		Map<Integer, Future<Pair<ClassificationRuleSet, Double>>> futures = new HashMap<Integer, Future<Pair<ClassificationRuleSet, Double>>>();
		
		// iterate over all classes
		for (int cid = 0; cid < mapping.size(); ++cid) {
			final int classId = cid;

			Future<Pair<ClassificationRuleSet, Double>> future = pool.submit( () -> {
				Logger.log("Class " + classId + " started\n" , Level.FINE);

				preprocessClass(dataset, classId);

				ClassificationRuleSet ruleset = (ClassificationRuleSet) factory.create(dataset);

				IntegerBitSet positives = new IntegerBitSet(dataset.size());
				IntegerBitSet negatives = new IntegerBitSet(dataset.size());
				IntegerBitSet uncoveredPositives = new IntegerBitSet(dataset.size());
				Set<Integer> uncovered = new HashSet<Integer>();
				
				double weighted_P = 0;
				double weighted_N = 0;
				
				// at the beginning rule set does not cover any examples
				for (int id = 0; id < dataset.size(); ++id) {
					Example e = dataset.getExample(id);
					double w = dataset.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
					
					if ((double)e.getValue(outputAttr) == classId) {
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

				boolean carryOn = uncoveredPositives.size() > 0; 
				double uncovered_p = weighted_P;
				
				while (carryOn) {
				
					Logger.log("Class " + classId + " uncovered positive weight:" + 
							uncovered_p +  "/" + weighted_P + "\n", Level.FINE);
					Rule rule = factory.create(
							new CompoundCondition(),
							new ElementaryCondition(outputAttr.getName(), new SingletonSet((double)classId, mapping.getValues())));

					// rule covers everything at the beginning
					rule.setWeighted_P(weighted_P);
					rule.setWeighted_N(weighted_N);
					rule.setWeighted_p(weighted_P);
					rule.setWeighted_n(weighted_N);

					rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
					rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
					rule.getCoveredPositives().addAll(positives);
					rule.getCoveredNegatives().addAll(negatives);
					rule.setRuleOrderNum(ruleset.getRules().size());

					rule.getConsequence().setCovering(positives);

					double t = System.nanoTime();
					carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
					ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
					
					if (carryOn) {
						if (params.isPruningEnabled()) {
							Logger.log("Before prunning:" + rule.toString() + "\n" , Level.FINE);
							t = System.nanoTime();
							finder.prune(rule, dataset, uncoveredPositives);
							ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
						}
						
						
						Logger.log("Class " + classId + ", candidate rule " + ruleset.getRules().size() +  ":" + rule.toString() + "\n", Level.FINE);

						// remove covered examples
						int previouslyUncovered = uncoveredPositives.size();
						uncoveredPositives.removeAll(rule.getCoveredPositives());
						uncovered.removeAll(rule.getCoveredPositives());
						uncovered.removeAll(rule.getCoveredNegatives());

						uncovered_p = 0;
						for (int id : uncoveredPositives) {
							Example e = dataset.getExample(id);
							uncovered_p += dataset.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
						}

						Logger.log("Uncovered positives" + uncovered_p + "\n", Level.FINER);


						// stop if number of positive examples remaining is less than threshold
						if (uncovered_p <= params.getMaximumUncoveredFraction() * weighted_P) {
							carryOn = false;
						}
						
						// stop and ignore last rule if no new positive examples covered
						if (uncoveredPositives.size() == previouslyUncovered) {
							carryOn = false; 
						} else {
							finder.postprocess(rule, dataset);
							ruleset.addRule(rule);
							mutex.acquire(1);
							Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
							Logger.log("\t" + totalRules.incrementAndGet() + " rules" , Level.INFO);
							mutex.release(1);
						}
						//report to operator command proxy
						this.operatorCommandProxy.onNewRule(rule);
						this.operatorCommandProxy.onProgressChange(dataset.size(), uncovered.size());

					}
					if (this.operatorCommandProxy.isRequestStop()) {
						carryOn = false;
					}
				}
				
				return new Pair<ClassificationRuleSet, Double>(ruleset, weighted_P);
			});
			
			futures.put(classId, future);
		}

		// add rulesets from all classes
		double defaultClassP = 0;
		
		for (int classId = 0; classId < mapping.size(); ++classId) {
			Pair<ClassificationRuleSet, Double> result;

			try {
				result = futures.get(classId).get();
				ClassificationRuleSet partialSet = result.getFirst();
				finalRuleset.getRules().addAll(partialSet.getRules());
				finalRuleset.setGrowingTime( finalRuleset.getGrowingTime() + partialSet.getGrowingTime());
				finalRuleset.setPruningTime( finalRuleset.getPruningTime() + partialSet.getPruningTime());

				// set default class
				if (result.getSecond() > defaultClassP) {
					defaultClassP = result.getSecond();
					finalRuleset.setDefaultClass(classId);
				}
				
			} catch (InterruptedException | ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

		pool.shutdown();

		return finalRuleset;
	}

	public void preprocessClass(IExampleSet dataset, int classId) {

	}
}
