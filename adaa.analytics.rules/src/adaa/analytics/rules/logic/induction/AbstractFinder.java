package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.IQualityMeasure;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;

/**
 * Abstract base class for algorithms performing growing and pruning of classification and regression rules.
 * @author Adam
 *
 */
public abstract class AbstractFinder {
	
	/**
	 * Rule induction parameters.
	 */
	protected final InductionParameters params;
	
	/**
	 * Initialises induction parameters.
	 * @param params
	 */
	public AbstractFinder(final InductionParameters params) {
		this.params = params;
	}
	
	/**
	 * Grows a rule.
	 * @param rule Rule to be grown.
	 * @param trainSet Training set.
	 * @param uncovered Collection of examples yet to cover (either all or positives).
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
				double v = calculateQuality(dataset, covering, params.getInductionMeasure());
				rule.setWeight(v);
				
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
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @return Updated covering object.
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
		double weight = calculateQuality(trainSet, covering, params.getPruningMeasure());
		rule.setWeight(weight);
		
		return covering;
	}
	
	
	/**
	 * 
	 * @param cov
	 * @return
	 */
	protected double calculateQuality(ExampleSet trainSet, Covering cov, IQualityMeasure measure) {
		return ((ClassificationMeasure)measure).calculate(
				cov.weighted_p, cov.weighted_n, 
				cov.weighted_P, cov.weighted_N);
	}
	
	/**
	 * 
	 * @param rule
	 * @param trainSet
	 * @param uncoveredByRuleset
	 * @param coveredByRule
	 * @param ignoredAttributes
	 * @return
	 */
	protected abstract ElementaryCondition induceCondition(
		final Rule rule,
		final ExampleSet trainSet,
		final Set<Integer> uncoveredByRuleset,
		final Set<Integer> coveredByRule, 
		final Set<Attribute> allowedAttributes);
	
	protected Set<Attribute> names2attributes(Set<String> names, ExampleSet dataset) {
		Set<Attribute> out = new HashSet<Attribute>();
		for (String s : names) {
			out.add(dataset.getAttributes().get(s));
		}
		return out;
	}
}
