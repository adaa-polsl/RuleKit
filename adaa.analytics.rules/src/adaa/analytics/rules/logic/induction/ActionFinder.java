package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.quality.*;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class ActionFinder extends AbstractFinder {
	
	protected Set<Integer> uncoveredNegatives;
	
	public void setUncoveredNegatives(Set<Integer> toSet) {
		uncoveredNegatives = toSet;
	}
	
	public Set<Integer> getUncoveredNegatives() {
		return uncoveredNegatives;
	}

	public ActionFinder(InductionParameters params) {
		super(params);
	}
	
	protected ConditionBase getBestElementaryCondition(
			Set<ElementaryCondition> conditions, 
			ExampleSet trainSet, 
			Set<Integer> positives,
			Rule rule) {
		
		double bestQ = Double.NEGATIVE_INFINITY;
		ConditionBase best = null;
		
		
		for (ConditionBase cond : conditions) {
			//extend rule with condition
			rule.getPremise().addSubcondition(cond);
			
			Covering cov = rule.covers(trainSet, positives);
			Covering covAll = rule.covers(trainSet);
			
			double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(covAll);
			
			if (cov.weighted_p >= params.getMinimumCovered() && quality >= bestQ) {
				bestQ = quality;
				best = cond;
				rule.setCoveringInformation(cov);
			}
			
			//clean it up
			rule.getPremise().removeSubcondition(cond);
		}
		
		return best;
	}
	
	
	
	protected Set<ElementaryCondition> generateElementaryConditions(
			ExampleSet trainSet,
			Set<Attribute> allowedAttributes,
			Set<Integer> coveredByRule) {
		
		HashSet<ElementaryCondition> conditions = new HashSet<ElementaryCondition>();
		
		for (Attribute atr : allowedAttributes) {
			getElementaryConditionForAttribute(trainSet, coveredByRule, conditions, atr.getName());
		}
		
		return conditions;
	}

	private void getElementaryConditionForAttribute(
			ExampleSet trainSet,
			Set<Integer> coveredByRule,
			Set<ElementaryCondition> conditions,
			String attributeName) {
		
		Attribute attribute = trainSet.getAttributes().get(attributeName);
		Set<Double> attributeValues = new HashSet<Double>();
		
		if (attribute.isNominal()) {
			
			
			//We take only attribute values present in Dr - already covered examples
			for (int id : coveredByRule) {
				
				Example ex = trainSet.getExample(id);
				double value = ex.getValue(attribute);
				
				attributeValues.add(value);
			}
			
			for (double val : attributeValues) {
				conditions.add(
						new ElementaryCondition(
								attributeName, 
								new SingletonSet(val, attribute.getMapping().getValues())));
			}
			
		} else {
			//numerical attribute - have to find midpoints
			attributeValues = new TreeSet<Double>();
			for (int id : coveredByRule) {
				
				Example ex = trainSet.getExample(id);
				double val = ex.getValue(attribute);
				
				attributeValues.add(val);
			}
			
			HashSet<Double> midPoints = new HashSet<Double>();
			attributeValues.stream().reduce(0.0, (a,b) -> {midPoints.add(a + b /2.0); return 0.0;});
			
			for (double midPoint : midPoints) {
				
				conditions.add(new ElementaryCondition(attributeName, Interval.create_le(midPoint)));
				conditions.add(new ElementaryCondition(attributeName, Interval.create_geq(midPoint)));
			}
							
		}
	}
	
	protected Action buildAction(ElementaryCondition left, ElementaryCondition right) throws Exception {
		
		return new Action(left.getAttribute(), left.getValueSet(), right == null ? null : right.getValueSet());
	}

	@Override
	protected ElementaryCondition induceCondition(
			Rule rule,
			ExampleSet trainSet, 
			Set<Integer> uncoveredByRuleset,
			Set<Integer> coveredByRule,
			Set<Attribute> allowedAttributes) {
		
		ActionRule aRule = rule instanceof ActionRule ? (ActionRule)rule : null;
		
		if (aRule == null)
			return null;
		
		Rule posRule = aRule.getLeftRule();
		Rule negRule = aRule.getRightRule();
		
		Set<ElementaryCondition> conds = this.generateElementaryConditions(trainSet, allowedAttributes, coveredByRule);
		ConditionBase best = this.getBestElementaryCondition(conds, trainSet, uncoveredByRuleset, posRule);
		
		if (best == null)
			return null;
		
		Attribute usedAttribute = trainSet.getAttributes().get(((ElementaryCondition)best).getAttribute());
		if (usedAttribute.isNominal()){
			allowedAttributes.remove(usedAttribute);
		}
		
		Set<Integer> coveredByNegRule = negRule.covers(trainSet).positives;		
		Set<ElementaryCondition> conditionsForNegativeRule = new HashSet<ElementaryCondition>();
		
		this.getElementaryConditionForAttribute(trainSet, coveredByNegRule, conditionsForNegativeRule, ((ElementaryCondition)best).getAttribute());
		ConditionBase otherBest = this.getBestElementaryCondition(conditionsForNegativeRule, trainSet, uncoveredNegatives, negRule);

		/*if (best.equals(otherBest)) {
			return null;
		}*/
		
		Action proposedAction = null;
		try {
			proposedAction = this.buildAction((ElementaryCondition)best, (ElementaryCondition)otherBest);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		if (rule.getPremise().getSubconditions().size() == 0) {
			//first time rule growing
			return proposedAction;
		} else {
			
			Action nilAction = null;
			try {
				nilAction = this.buildAction((ElementaryCondition)best, null);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			

			aRule.getPremise().addSubcondition((ElementaryCondition)proposedAction);
			Covering normalCovering = aRule.covers(trainSet, uncoveredByRuleset);
			double normalQ = this.calculateQuality(trainSet, normalCovering, params.getInductionMeasure());
			aRule.getPremise().removeSubcondition((ElementaryCondition)proposedAction);
			
			aRule.getPremise().addSubcondition((ElementaryCondition)nilAction);
			Covering nilCovering = aRule.covers(trainSet, uncoveredByRuleset);
			double nilQ = this.calculateQuality(trainSet, nilCovering, params.getInductionMeasure());
			aRule.getPremise().removeSubcondition((ElementaryCondition)nilAction);
			
			if (normalQ < nilQ) {
				proposedAction = nilAction;
			}
		}
		return proposedAction;
	}
	
	public double calculateActionQuality(final ExampleSet trainSet, Covering covering, IQualityMeasure measure) {
		double q = ((ClassificationMeasure)measure).calculate(covering);
		return q;
	}
	
	/**
	 * Removes irrelevant conditions from rule using hill-climbing strategy and action-specific quality measurements. 
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @return Updated covering object.
	 */
	@Override
	public Covering prune(final Rule rule_, final ExampleSet trainSet) {
		
		Logger.log("ActionFinder.prune()\n", Level.FINE);
		ActionRule rule = rule_ instanceof ActionRule ? (ActionRule)rule_ : null;
		
		if (rule == null) {
			throw new RuntimeException("Not an actionrule in actionrule pruning!");
		}
		
		// check preconditions
		if (rule.getWeighted_p() == Double.NaN || rule.getWeighted_p() == Double.NaN ||
			rule.getWeighted_P() == Double.NaN || rule.getWeighted_N() == Double.NaN) {
			throw new IllegalArgumentException();
		}
		
		Covering covering = rule.actionCovers(trainSet);
		double initialQuality = calculateActionQuality(trainSet, covering, params.getPruningMeasure());
		boolean continueClimbing = true;
		boolean shouldActionBeNil = false;
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
				covering = rule.actionCovers(trainSet);
				cnd.setDisabled(false);
				
				double q = calculateActionQuality(trainSet, covering, params.getPruningMeasure());
				
				if (q > bestQuality) {
					bestQuality = q;
					toRemove = cnd;
					shouldActionBeNil = false;
				}
				
				Action actionCondition = cnd instanceof Action ? (Action)cnd : null;
				if (actionCondition == null) {
					Logger.log("Non-action condition in ActionFinder.prune", Level.ALL);
					continue;
				}
				
				actionCondition.setActionNil(true);
				covering = rule.actionCovers(trainSet);
				actionCondition.setActionNil(false);
				
				q = calculateActionQuality(trainSet, covering, params.getPruningMeasure());
				
				if (q > bestQuality) {
					bestQuality = q;
					//with disabled right part of action
					toRemove = actionCondition;
					shouldActionBeNil = true;
				}
			}
			
			// if there is something to remove
			if (bestQuality >= initialQuality) {
				initialQuality = bestQuality;				
				rule.getPremise().removeSubcondition(toRemove);
				
				if (shouldActionBeNil) {
					Action ac = toRemove instanceof Action ? (Action)toRemove : null;
					if (ac == null) {
						throw new RuntimeException("Impossible at that phase!");
					}
					ac.setActionNil(true);
					rule.getPremise().addSubcondition(ac);
				}
				// stop climbing when only single condition remains
				continueClimbing = rule.getPremise().getSubconditions().size() > 1;
				Logger.log("Condition removed: " + rule + "\n", Level.FINER);
			} else {
				continueClimbing = false;
			}
		}
		
		covering = rule.actionCovers(trainSet);
		rule.setCoveringInformation(covering);
		double weight = calculateActionQuality(trainSet, covering, params.getPruningMeasure());
		rule.setWeight(weight);
		
		return covering;
	}

}
