package adaa.analytics.rules.logic.induction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
			if (otherBest == null) {
				proposedAction.setActionNil(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return proposedAction;
	}
	
	public double calculateActionQuality(Covering covering, IQualityMeasure measure) {
		double q = ((ClassificationMeasure)measure).calculate(covering);
		return q;
	}
	
	//@Override
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
		
		int maskLength = (trainSet.size() + Long.SIZE - 1) / Long.SIZE;
		//during growing, nil action never will be constructed (?)
		int maskCount = rule.getPremise().getSubconditions().size();
		
		Rule leftRule = rule.getLeftRule();
		Rule rightRule = rule.getRightRule();
		
		Covering lCov = leftRule.covers(trainSet);
		leftRule.setCoveringInformation(lCov);
		
		int maskCountLeft = maskCount;//leftRule.getPremise().getSubconditions().size();
		
		long[] masksLeft = new long[maskCountLeft * maskLength]; 
		long[] labelMaskLeft = new long[maskLength];
		
		int maskCountRight =/* maskCount;//*/rightRule.getPremise().getSubconditions().size();
		long[] masksRight = new long[maskCountRight * maskLength];
		long[] labelMaskRight = new long[maskLength];
		
		for (int i = 0; i < trainSet.size(); i++) {
			
			Example ex = trainSet.getExample(i);
			
			int wordId = i / Long.SIZE;
			int wordOffset = i % Long.SIZE;
			
			if (leftRule.getConsequence().evaluate(ex)) {
				labelMaskLeft[wordId] |= 1L << wordOffset;
			}
			
			if (rightRule.getConsequence().evaluate(ex)) {
				labelMaskRight[wordId] |= 1L << wordOffset;
			}
			
			for (int m = 0; m < maskCountLeft; ++m) {
				ConditionBase cnd = leftRule.getPremise().getSubconditions().get(m);
				if (cnd.evaluate(ex)) {
					masksLeft[m * maskLength + wordId] |= 1L << wordOffset;
				}
			}
			
			for (int m = 0; m < maskCountRight; m++) {
				ConditionBase cnd = rightRule.getPremise().getSubconditions().get(m);
				if (cnd.evaluate(ex)) {
					masksRight[m * maskLength + wordId] |= 1L << wordOffset;
				}
			}
		}
		
		Map<ConditionBase, Integer> condToMaskLeft = new HashMap<ConditionBase, Integer>();
		Map<ConditionBase, Integer> condToMaskRight = new HashMap<ConditionBase, Integer>();
		Map<ConditionBase, Integer> conditionToMask = new HashMap<ConditionBase, Integer>();
		Set<ConditionBase> presentCondLeft = new HashSet<ConditionBase>();
		Set<ConditionBase> presentCondRight = new HashSet<ConditionBase>();
		Set<ConditionBase> presentConditions = new HashSet<ConditionBase>();
		
		for (int i = 0; i < maskCount; i++) {
			ConditionBase cndLeft = leftRule.getPremise().getSubconditions().get(i);
			condToMaskLeft.put(cndLeft, i);
			presentCondLeft.add(cndLeft);
			
			if (i < maskCountRight) {
				ConditionBase cndRight = rightRule.getPremise().getSubconditions().get(i);
				condToMaskRight.put(cndRight, i);
				presentCondRight.add(cndRight);
			}
			ConditionBase cnd = rule.getPremise().getSubconditions().get(i);
			conditionToMask.put(cnd, i);
			presentConditions.add(cnd);
		}
		
		Covering covering = rule.covers(trainSet);
		
		double initialQuality = this.calculateActionQuality(covering, params.getPruningMeasure());
		double initialQualityR = this.calculateQuality(trainSet, rule.getRightRule().covers(trainSet), params.getPruningMeasure());
		double initialQualityL = this.calculateQuality(trainSet, rule.getLeftRule().covers(trainSet), params.getPruningMeasure());
		boolean climbing = true;
		
		while(climbing) {
			
			ConditionBase toRemove = null;
			ConditionBase toCompleteRemoval = null;
			ConditionBase toPrune = null;
			ConditionBase toRemoveAlreadyNil = null;
			double bestQualityAlreadyNil = Double.NEGATIVE_INFINITY;
			double bestQualityLeft = Double.NEGATIVE_INFINITY;
			double bestQualityRight = Double.NEGATIVE_INFINITY;
			boolean looseCondition = false;
			boolean alreadyNilPruning = false;
			for (ConditionBase cnd_ : rule.getPremise().getSubconditions()) {
				
				Action cnd = cnd_ instanceof Action ? (Action)cnd_ : null;
				if (cnd == null) {
					throw new RuntimeException("Impossible at that phase");
				}
				
				if (!cnd.isPrunable()) continue;
				
				//if (cnd.getActionNil()) continue;
				
				presentConditions.remove(cnd);
				presentCondLeft.remove(cnd.getLeftCondition());
				if (!cnd.getActionNil()) {
					presentCondRight.remove(cnd.getRightCondition());
				}
				
				double pLeft = 0.0, nLeft = 0.0;
				double pRight = 0.0, nRight = 0.0;
				double loosePLeft = 0.0, looseNLeft = 0.0;
				
				
				
				for (int wordId = 0; wordId < maskLength; ++wordId) {
					long word = ~(0L);
					long labelWord = labelMaskLeft[wordId];
					// iterate over all present conditions
					for (ConditionBase other : presentCondLeft) {
						int m = condToMaskLeft.get(other);
						word &= masksLeft[m * maskLength + wordId];
					}
					long loosedWord = word & masksLeft[conditionToMask.get(cnd) * maskLength + wordId];
					// no weighting - use popcount
					if (trainSet.getAttributes().getWeight() == null) {
						pLeft += Long.bitCount(word & labelWord);
						nLeft += Long.bitCount(word & ~labelWord);
						loosePLeft += Long.bitCount(loosedWord & labelWord);
						looseNLeft += Long.bitCount(loosedWord & ~labelWord);
					} else {
						long posWord = word & labelWord;
						long negWord = word & ~labelWord;
						long loosePosWord = loosedWord & labelWord;
						long looseNegWord = loosedWord & ~labelWord;
						for (int wordOffset = 0; wordOffset < Long.SIZE; ++wordOffset) {
							if ((posWord & (1L << wordOffset)) != 0) {
								pLeft += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							} else if ((negWord & (1L << wordOffset)) != 0) {
								nLeft += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							}
							if ((loosePosWord & (1L << wordOffset)) != 0) {
								loosePLeft += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							} else if ((looseNegWord & (1L << wordOffset)) != 0) {
								looseNLeft += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							}
						}
					}
					
					word = ~(0L);
					labelWord = labelMaskRight[wordId];
					for (ConditionBase other : presentCondRight) {
						int m = condToMaskRight.get(other);
						word &= masksRight[m * maskLength + wordId];
					}
					
					if (trainSet.getAttributes().getWeight() == null) {
						pRight += Long.bitCount(word & labelWord);
						nRight += Long.bitCount(word & ~labelWord);
					} else {
						long posWord = word & labelWord;
						long negWord = word & ~labelWord;
						for (int wordOffset = 0; wordOffset < Long.SIZE; ++wordOffset) {
							if ((posWord & (1L << wordOffset)) != 0) {
								pRight += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							} else if ((negWord & (1L << wordOffset)) != 0) {
								nRight += trainSet.getExample(wordId * Long.SIZE + wordOffset).getWeight();
							}
						}
					}
				}
				
				presentConditions.add(cnd);
				presentCondLeft.add(cnd.getLeftCondition());
				if (!cnd.getActionNil()){
					presentCondRight.add(cnd.getRightCondition());
				}
				
				double qualityRight = ((ClassificationMeasure)params.getPruningMeasure()).calculate(
						pRight, nRight, leftRule.getWeighted_N(), leftRule.getWeighted_P());
				
				double qualityLeft = ((ClassificationMeasure)params.getPruningMeasure()).calculate(
						pLeft, nLeft, leftRule.getWeighted_P(), leftRule.getWeighted_N());
				
				if (cnd.getActionNil()) {
					if (qualityLeft > bestQualityLeft) {
						bestQualityAlreadyNil = qualityLeft;
						toRemoveAlreadyNil = cnd;
						alreadyNilPruning = true;
					}
				} else {
					if (qualityRight > bestQualityRight) {
						looseCondition = false;
						alreadyNilPruning = false;
						bestQualityRight = qualityRight;
						toRemove = cnd;
						if (qualityLeft > bestQualityLeft) {
							bestQualityLeft = qualityLeft;
							looseCondition = true;
						}
					}
				}
			}
			
			Action act = (Action)toRemove;
			
			if (act == null) {
				climbing = false;
				continue;
			}
			
			boolean leftBetter = bestQualityLeft >= initialQualityL && looseCondition;
			boolean rightBetter = bestQualityRight >= initialQualityR;
			
			if (alreadyNilPruning && bestQualityAlreadyNil > initialQualityL) {
				
				initialQualityL = bestQualityLeft;
				presentConditions.remove(toRemoveAlreadyNil);
				presentCondLeft.remove(((Action)toRemoveAlreadyNil).getLeftCondition());
				rule.getPremise().removeSubcondition(toRemoveAlreadyNil);
			} else if (rightBetter) {
				initialQualityR = bestQualityRight;
				presentConditions.remove(toRemove);
				presentCondRight.remove(((Action)toRemove).getRightCondition());
				
				if (leftBetter) {
					
					initialQualityL = bestQualityLeft;
					presentCondLeft.remove(((Action)toRemove).getLeftCondition());
					rule.getPremise().removeSubcondition(toRemove);
				} else {
				
					rule.getPremise().removeSubcondition(toRemove);
					act.setActionNil(true);
					rule.getPremise().addSubcondition(toRemove);
				}
			} else  {
				climbing = false;
			}
			
			if (rule.getPremise().getSubconditions().size() == 1) {
				climbing = false;
			}
		}
		
		covering = rule.covers(trainSet);
		rule.setCoveringInformation((ActionCovering)covering);
		
		double weight = calculateActionQuality(covering, params.getPruningMeasure());
		rule.setWeight(weight);
		
		return covering;
	}
	
	/**
	 * Removes irrelevant conditions from rule using hill-climbing strategy and action-specific quality measurements. 
	 * @param rule Rule to be pruned.
	 * @param trainSet Training set. 
	 * @return Updated covering object.
	 */
	//@Override
	public Covering prune2(final Rule rule_, final ExampleSet trainSet) {
		
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
		
		Covering covering = rule.covers(trainSet);
		double initialQuality = calculateActionQuality(covering, params.getPruningMeasure());
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
				covering = rule.covers(trainSet);
				cnd.setDisabled(false);
				
				double q = calculateActionQuality(covering, params.getPruningMeasure());
				
				if (q > bestQuality) {
					bestQuality = q;
					toRemove = cnd;
					shouldActionBeNil = false;
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
		
		covering = rule.covers(trainSet);
		rule.setCoveringInformation(covering);
		double weight = calculateActionQuality(covering, params.getPruningMeasure());
		rule.setWeight(weight);
		
		return covering;
	}

}
