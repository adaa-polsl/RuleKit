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
import adaa.analytics.rules.logic.representation.model.ClassificationRuleSet;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.INominalMapping;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
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

	public ClassificationSnC(AbstractFinder finder, InductionParameters params) {
		super(params);
		this.finder = finder;
		this.factory = new RuleFactory(RuleFactory.CLASSIFICATION,  params, null);
	}
	
	/**
	 * Generates a classification rule set on the basis of a training set.
	 * @param dataset Training data set.
	 * @return Rule set.
	 */
	public RuleSetBase run(IExampleSet dataset) {
		Logger.log("ClassificationSnC.run()\n", Level.FINE);

		// use contrast attribute if specified
		final IAttribute outputAttr = (dataset.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
				? dataset.getAttributes().getLabel()
				: dataset.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
		DataColumnDoubleAdapter weightDataColumnDoubleAdapter = dataset.getDataColumnDoubleAdapter(dataset.getAttributes().getWeight(), Double.NaN);
		DataColumnDoubleAdapter outputAttrrDataColumnDoubleAdapter = dataset.getDataColumnDoubleAdapter(outputAttr, Double.NaN);

		INominalMapping mapping = outputAttr.getMapping();
		List<String> labels = new ArrayList<>();
		labels.addAll(mapping.getValues());
		Collections.sort(labels);

		boolean weighted = (dataset.getAttributes().getWeight() != null);

		// perform prepreprocessing
		finder.preprocess(dataset);
		ClassificationRuleSet ruleset = (ClassificationRuleSet) factory.create(dataset);

		// add rulesets from all classes
		double defaultClassP = 0;

		// iterate over all classes
		for (String label : labels) {
			int classId = mapping.getIndex(label);
			Logger.log("Class " + label + " (" +classId + ") started\n" , Level.FINE);
			
			preprocessClass(dataset, classId);

			IntegerBitSet positives = new IntegerBitSet(dataset.size());
			IntegerBitSet negatives = new IntegerBitSet(dataset.size());
			IntegerBitSet uncoveredPositives = new IntegerBitSet(dataset.size());
			Set<Integer> uncovered = new HashSet<Integer>();

			double weighted_P = 0;
			double weighted_N = 0;

			// at the beginning rule set does not cover any examples
			for (int id = 0; id < dataset.size(); ++id) {

				double w = !weighted ? 1.0 : weightDataColumnDoubleAdapter.getDoubleValue(id);

				if ((double)outputAttrrDataColumnDoubleAdapter.getDoubleValue(id) == classId) {
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

			if (weighted_P > defaultClassP) {
				defaultClassP = weighted_P;
				ruleset.setDefaultClass(classId);
			}

			boolean carryOn = uncoveredPositives.size() > 0;
			double uncovered_p = weighted_P;

			while (carryOn) {

				Logger.log("Class " + classId + " uncovered positive weight:" +
						uncovered_p + "/" + weighted_P + "\n", Level.FINE);
				Rule rule = factory.create(
						new CompoundCondition(),
						new ElementaryCondition(outputAttr.getName(), new SingletonSet((double) classId, mapping.getValues())));

				// rule covers everything at the beginning
				rule.setWeighted_P(weighted_P);
				rule.setWeighted_N(weighted_N);
				rule.setWeighted_p(weighted_P);
				rule.setWeighted_n(weighted_N);

				rule.setCoveredPositives(new IntegerBitSet(dataset.size()));
				rule.setCoveredNegatives(new IntegerBitSet(dataset.size()));
				rule.getCoveredPositives().addAll(positives);
				rule.getCoveredNegatives().addAll(negatives);
				rule.setRuleOrderNum(countRuleOrderNumber(ruleset,label));

				rule.getConsequence().setCovering(positives);

				double t = System.nanoTime();
				carryOn = (finder.grow(rule, dataset, uncoveredPositives) > 0);
				ruleset.setGrowingTime(ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);

				if (carryOn) {
					if (params.isPruningEnabled()) {
						Logger.log("Before prunning:" + rule.toString() + "\n", Level.FINE);
						t = System.nanoTime();
						finder.prune(rule, dataset, uncoveredPositives);
						ruleset.setPruningTime(ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
					}


					Logger.log("Class " + classId + ", candidate rule " + ruleset.getRules().size() + ":" + rule.toString() + "\n", Level.FINE);

					// remove covered examples
					int previouslyUncovered = uncoveredPositives.size();
					uncoveredPositives.removeAll(rule.getCoveredPositives());
					uncovered.removeAll(rule.getCoveredPositives());
					uncovered.removeAll(rule.getCoveredNegatives());

					uncovered_p = 0;

					for (int id : uncoveredPositives) {
						uncovered_p += dataset.getAttributes().getWeight() == null ? 1.0 : weightDataColumnDoubleAdapter.getDoubleValue(id);
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
						Logger.log("\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
						Logger.log("\t" + ruleset.getRules().size() + " rules", Level.INFO);
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

		return ruleset;
	}

	public void preprocessClass(IExampleSet dataset, int classId) {

	}

	int countRuleOrderNumber(ClassificationRuleSet ruleSet, String currentClassLabel)
	{
		int counter = 0;

		for(Rule r: ruleSet.getRules())
		{
			ClassificationRule cr = (ClassificationRule)r;
			if (cr.getClassLabel().equals(currentClassLabel))
			{
				counter++;
			}
		}

		return counter;
	}
}
