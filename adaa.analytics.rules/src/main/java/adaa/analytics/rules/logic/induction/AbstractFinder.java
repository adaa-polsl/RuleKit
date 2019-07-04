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
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

/**
 * Abstract base class for growing and pruning procedures for all types of rules (classification, regression, survival).
 * 
 * @author Adam Gudys
 * 
 */
public abstract class AbstractFinder {
	
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
	
	/**
	 * Initializes induction parameters and thread pool.
	 *
	 * @param params Induction parameters.
	 */
	public AbstractFinder(final InductionParameters params) {
		this.params = params;
		
		threadCount = Runtime.getRuntime().availableProcessors();
		pool = Executors.newFixedThreadPool(threadCount);
	}
	
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
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		
		// get current covering
		Covering covering = rule.covers(dataset);
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
				covering = rule.covers(dataset);
				
				covered.clear();
				covered.addAll(covering.positives);
				covered.addAll(covering.negatives);

				rule.setCoveringInformation(covering);
				Pair<Double, Double> qp = calculateQualityAndPValue(dataset, covering, params.getVotingMeasure());
				rule.setWeight(qp.getFirst());
				rule.setPValue(qp.getSecond());
				
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
		return addedConditionsCount;
	}
	
	/**
	 * Removes irrelevant conditions from rule using hill-climbing strategy. 
	 * 
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @return Covering of the rule after pruning.
	 */
	public Covering prune(final Rule rule, final ExampleSet trainSet) {
		
		Logger.log("AbstractFinder.prune()\n", Level.FINE);
		
		// check preconditions
		if (rule.getWeighted_p() == Double.NaN || rule.getWeighted_p() == Double.NaN ||
			rule.getWeighted_P() == Double.NaN || rule.getWeighted_N() == Double.NaN) {
			throw new IllegalArgumentException();
		}
		
		Covering covering = rule.covers(trainSet);
		double initialQuality = calculateQuality(trainSet, covering, params.getPruningMeasure());
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
				covering = rule.covers(trainSet);
				cnd.setDisabled(false);
				
				double q = calculateQuality(trainSet, covering, params.getPruningMeasure());
				
				if (q > bestQuality) {
					bestQuality = q;
					toRemove = cnd;
				}
			}
			
			// if there is something to remove
			if (bestQuality >= initialQuality) {
				initialQuality = bestQuality;
				rule.getPremise().removeSubcondition(toRemove);
				// stop climbing when only single condition remains
				continueClimbing = rule.getPremise().getSubconditions().size() > 1;
				Logger.log("Condition removed: " + rule + "\n", Level.FINER);
			} else {
				continueClimbing = false;
			}
		}
		
		covering = rule.covers(trainSet);
		rule.setCoveringInformation(covering);
		Pair<Double,Double> qp = calculateQualityAndPValue(trainSet, covering, params.getVotingMeasure());
		rule.setWeight(qp.getFirst());
		rule.setPValue(qp.getSecond());
		
		return covering;
	}
	
	
	/**
	 * Calculates quality on a training set.
	 * 
	 * @param trainSet Training set.
	 * @param ct Contingency table.
	 * @param measure Quality measure to be calculated.
	 * @return Calculated measure.
	 */
	protected double calculateQuality(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure measure) {
		return ((ClassificationMeasure)measure).calculate(ct);
	}
	
	/***
	 * Calculates rule quality and p-value on a training set. The method returns
	 * 1.0 as a p-value - this behavior should be overridden by derived classes.
	 * 
	 * @param trainSet Training set.
	 * @param ct Contingency table.
	 * @param measure Quality measure to be calculated.
	 * @return Pair containing value of rule quality measure and p-value.
	 */
	protected Pair<Double,Double> calculateQualityAndPValue(ExampleSet trainSet, ContingencyTable ct, IQualityMeasure measure) {
	
		return new Pair<Double, Double>(
				calculateQuality(trainSet, ct, measure), 
				1.0);
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
