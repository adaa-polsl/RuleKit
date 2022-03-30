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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.quality.IQualityModifier;
import adaa.analytics.rules.logic.quality.NoneQualityModifier;
import adaa.analytics.rules.logic.representation.*;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

/**
 * Abstract base class for growing and pruning procedures for all types of rules (classification, regression, survival).
 * 
 * @author Adam Gudys
 * 
 */
public abstract class AbstractFinder implements AutoCloseable {
	
	/**
	 * Rule induction parameters.
	 */
	protected final InductionParameters params;
	
	/**
	 * Number of threads to be used by the induction algorithm.
	 */
	protected int threadCount;
    
	/**
	 * Thread pool to be used by the algorithm.
	 */
	protected ExecutorService pool;

	protected IQualityModifier modifier;

	protected List<IFinderObserver> observers = new ArrayList<IFinderObserver>();
	
	/**
	 * Initializes induction parameters and thread pool.
	 *
	 * @param params Induction parameters.
	 */
	public AbstractFinder(final InductionParameters params) {
		this.params = params;
		
		threadCount = Runtime.getRuntime().availableProcessors();
		pool = Executors.newFixedThreadPool(threadCount);
		modifier = new NoneQualityModifier();
	}

	@Override
	public void close() {
		pool.shutdown();
	}

	/**
	 * Can be implemented by subclasses to perform some initial processing prior growing.
	 * @param trainSet Training set.
	 */
	public void preprocess(ExampleSet trainSet) {}

	/**
	 * Adds elementary conditions to the rule premise until termination conditions are fulfilled.
	 * 
	 * @param rule Rule to be grown.
	 * @param dataset Training set.
	 * @param uncovered Collection of examples yet uncovered by the model (positive examples in the classification problems).
	 * @return Number of conditions added.
	 */
	public int grow(
		final Rule rule,
		final ExampleSet dataset,
		final Set<Integer> uncovered) {

		Logger.log("AbstractFinder.grow()\n", Level.FINE);

		for (IFinderObserver o: observers) {
			o.growingStarted(rule);
		}

		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		
		// get current covering
		Covering covering = new Covering();
		rule.covers(dataset, covering, covering.positives, covering.negatives);
		Set<Integer> covered = new HashSet<Integer>();
		covered.addAll(covering.positives);
		covered.addAll(covering.negatives);
		Set<Attribute> allowedAttributes = new TreeSet<Attribute>(new AttributeComparator());
		for (Attribute a: dataset.getAttributes()) {
			allowedAttributes.add(a);
		}
		
		// add conditions to rule
		boolean carryOn = true;
		
		do {
			ElementaryCondition condition = induceCondition(
					rule, dataset, uncovered, covered, allowedAttributes);
			
			if (condition != null) {
				rule.getPremise().addSubcondition(condition);

				for (IFinderObserver o: observers) {
					o.conditionAdded(condition);
				}

				covering = new Covering();
				rule.covers(dataset, covering, covering.positives, covering.negatives);
				covered.clear();
				covered.addAll(covering.positives);
				covered.addAll(covering.negatives);

				rule.setCoveringInformation(covering);
				rule.getCoveredPositives().setAll(covering.positives);
				rule.getCoveredNegatives().setAll(covering.negatives);

				rule.updateWeightAndPValue(dataset, covering, params.getVotingMeasure());
				
				Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
						+ rule.toString() + "\n", Level.FINER);
				
				if (params.getMaxGrowingConditions() > 0) {
					if (rule.getPremise().getSubconditions().size() - initialConditionsCount >= 
						params.getMaxGrowingConditions() * dataset.getAttributes().size()) {
						carryOn = false;
					}
				}
				
			} else {
				carryOn = false;
			}
			
		} while (carryOn); 
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
		rule.setInducedContitionsCount(addedConditionsCount);

		for (IFinderObserver o: observers) {
			o.growingFinished(rule);
		}

		return addedConditionsCount;
	}
	
	/**
	 * Removes irrelevant conditions from rule using hill-climbing strategy. 
	 * 
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @param uncovered Collection of examples yet uncovered by the model (positive examples in the classification problems).
	 * @return Covering of the rule after pruning.
	 */
	public void prune(
			final Rule rule,
			final ExampleSet trainSet,
			final Set<Integer> uncovered) {
		
		Logger.log("AbstractFinder.prune()\n", Level.FINE);
		boolean weighting = (trainSet.getAttributes().getWeight() != null);
		
		// check preconditions
		if (rule.getWeighted_p() == Double.NaN || rule.getWeighted_p() == Double.NaN ||
			rule.getWeighted_P() == Double.NaN || rule.getWeighted_N() == Double.NaN) {
			throw new IllegalArgumentException();
		}

		IntegerBitSet positives = new IntegerBitSet(trainSet.size());
		IntegerBitSet negatives = new IntegerBitSet(trainSet.size());
		IntegerBitSet localUncovered;

		// fixme: ugly workaround for having IntegerBitSet in Covering instance
		Covering covering = new Covering();
		covering.positives = positives;
		covering.negatives = negatives;

		rule.covers(trainSet, covering, covering.positives, covering.negatives);

		if (uncovered instanceof IntegerBitSet) {
			localUncovered = (IntegerBitSet) uncovered;
		} else {
			localUncovered = new IntegerBitSet(trainSet.size());
			localUncovered.addAll(uncovered);
		}

		double new_p = 0, new_n = 0;

		if (weighting) {

		} else {
			new_p = positives.calculateIntersectionSize(localUncovered);
			new_n = negatives.calculateIntersectionSize(localUncovered);
		}

		double initialQuality = params.getPruningMeasure().calculate(trainSet, covering);
		initialQuality = modifier.modifyQuality(
				initialQuality, null, covering.weighted_p + covering.weighted_n, new_p + new_n);

		boolean continueClimbing = true;

		while (continueClimbing) {
			ConditionBase toRemove = null;
			double bestQuality = Double.NEGATIVE_INFINITY;
			
			for (ConditionBase cnd : rule.getPremise().getSubconditions()) {
				// consider only prunable conditions
				if (!cnd.isPrunable()) {
					continue;
				}
				
				// disable subcondition to calculate measure
				cnd.setDisabled(true);
				covering.clear();
				rule.covers(trainSet, covering, covering.positives, covering.negatives);
				cnd.setDisabled(false);

				if (weighting) {
					new_p = 0;
					new_n = 0;
				} else {
					new_p = positives.calculateIntersectionSize(localUncovered);
					new_n = negatives.calculateIntersectionSize(localUncovered);
				}
				
				double q = params.getPruningMeasure().calculate(trainSet, covering);
				
				if (cnd instanceof  ElementaryCondition) {
					ElementaryCondition ec = (ElementaryCondition)cnd;
					q = modifier.modifyQuality(q, ec.getAttribute(), covering.weighted_p + covering.weighted_n, new_p + new_n);
				}

				if (q > bestQuality) {
					bestQuality = q;
					toRemove = cnd;
				}
			}
			
			// if there is something to remove
			if (bestQuality >= initialQuality) {
				initialQuality = bestQuality;
				rule.getPremise().removeSubcondition(toRemove);

				for (IFinderObserver o: observers) {
					o.conditionRemoved(toRemove);
				}

				// stop climbing when only single condition remains
				continueClimbing = rule.getPremise().getSubconditions().size() > 1;
				Logger.log("Condition removed: " + rule + "\n", Level.FINER);
			} else {
				continueClimbing = false;
			}
		}

		covering.clear();
		rule.covers(trainSet, covering, covering.positives, covering.negatives);
		rule.setCoveringInformation(covering);

		rule.getCoveredPositives().addAll(covering.positives);
		rule.getCoveredNegatives().addAll(covering.negatives);

		rule.updateWeightAndPValue(trainSet, covering, params.getVotingMeasure());
	}

	/**
	 * Postprocesses a rule.
	 *
	 * @param rule Rule to be postprocessed.
	 * @param dataset Training set.
	 *
	 */
	public void postprocess(
		final Rule rule,
		final ExampleSet dataset) {

		for (IFinderObserver o: observers) {
			o.ruleReady(rule);
		}
	}

	/**
	 * Abstract method representing all procedures which induce an elementary condition.
	 * 
	 * @param rule Current rule.
	 * @param trainSet Training set.
	 * @param uncoveredByRuleset Set of examples uncovered by the model.
	 * @param coveredByRule Set of examples covered by the rule being grown.
	 * @param allowedAttributes Set of attributes that may be used during induction.
	 * @param extraParams Additional parameters.
	 * @return Induced elementary condition.
	 */
	protected abstract ElementaryCondition induceCondition(
		final Rule rule,
		final ExampleSet trainSet,
		final Set<Integer> uncoveredByRuleset,
		final Set<Integer> coveredByRule, 
		final Set<Attribute> allowedAttributes,
		Object... extraParams);
	
	/**
	 * Maps a set of attribute names to a set of attributes.
	 * 
	 * @param names Set of attribute names.
	 * @param dataset Training set.
	 * @return Set of attributes.
	 */
	protected Set<Attribute> names2attributes(Set<String> names, ExampleSet dataset) {
		Set<Attribute> out = new HashSet<Attribute>();
		for (String s : names) {
			out.add(dataset.getAttributes().get(s));
		}
		return out;
	}
}
