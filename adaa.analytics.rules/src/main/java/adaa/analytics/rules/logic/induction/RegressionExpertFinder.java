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
import adaa.analytics.rules.logic.representation.ConditionBase.Type;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;

import org.apache.commons.lang3.SerializationUtils;

import java.util.*;
import java.util.logging.Level;

/**
 * Class for growing and pruning regression rules with user's knowledge.
 * 
 * @author Adam Gudys
 *
 */
public class RegressionExpertFinder extends RegressionFinder implements IExpertFinder {

	protected Knowledge knowledge;
	
	public void setKnowledge(Knowledge knowledge) {
		this.knowledge = knowledge;
	}
	
	public RegressionExpertFinder(InductionParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
	}
	
	public void adjust(
			Rule rule,
			IExampleSet dataset,
			Set<Integer> uncovered) {
			
			CompoundCondition expertPremise = rule.getPremise();
			rule.setPremise(new CompoundCondition());

			Covering covering = new Covering();

			for (ConditionBase cnd : expertPremise.getSubconditions()) {
				ElementaryCondition ec = (ElementaryCondition)cnd;
				if (ec.isAdjustable()) {
					
					// update covering information - needed for automatic induction
					covering.clear();
					rule.covers(dataset, covering, covering.positives, covering.negatives);
					Set<Integer> covered = new HashSet<Integer>();
					covered.addAll(covering.positives);
					covered.addAll(covering.negatives);
					rule.setCoveringInformation(covering);
					
					// determine attribute
					Set<IAttribute> attr = new TreeSet<IAttribute>(new AttributeComparator());
					attr.add(dataset.getAttributes().get(ec.getAttribute()));
					
					Set<Integer> mustBeCovered;
					
					if (ec.getValueSet() instanceof Universum) {
						// condition in a form "attribute = Any" - just find best condition using this attribute
						mustBeCovered = uncovered;
						
					} else {
						// condition in other form - find best condition using this attribute with non-empty intersection with specified condition
						mustBeCovered = new HashSet<Integer>();
						for (int i : covered) {
							if (ec.evaluate(dataset.getExample(i))) {
								mustBeCovered.add(i);
							}
						}	
					}
					
					ElementaryCondition newCondition = induceCondition(
							rule, dataset, mustBeCovered, covered, attr);
					
					if (newCondition != null) {
						newCondition.setType(Type.FORCED);
						rule.getPremise().addSubcondition(newCondition);
					}
					
				} else {
					rule.getPremise().addSubcondition((ElementaryCondition)SerializationUtils.clone(ec));
				}
			}
			
			covering.clear();
			IntegerBitSet positives = new IntegerBitSet(dataset.size());
			IntegerBitSet negatives = new IntegerBitSet(dataset.size());
			rule.covers(dataset, covering, positives, negatives);

			// ugly
			covering.positives = positives;
			covering.negatives = negatives;
			rule.setCoveringInformation(covering);

			rule.setCoveredNegatives(negatives);
			rule.setCoveredPositives(positives);

			rule.updateWeightAndPValue(dataset, covering, params.getVotingMeasure());
		}
	
	
	@Override 
	public int grow(
			Rule rule,
			IExampleSet dataset,
			Set<Integer> uncovered)
	{
		Logger.log("RegressionExpertFinder.grow()\n", Level.FINE);
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		boolean isRuleEmpty = rule.getPremise().getSubconditions().size() == 0;
		
		// get current covering
		IntegerBitSet positives = new IntegerBitSet(dataset.size());
		IntegerBitSet negatives = new IntegerBitSet(dataset.size());
		Covering covering =  new Covering();
		rule.covers(dataset, covering, positives, negatives);

		IntegerBitSet covered = new IntegerBitSet(dataset.size());
		covered.addAll(positives);
		covered.addAll(negatives);
		Set<IAttribute> allowedAttributes = new TreeSet<IAttribute>(new AttributeComparator());
		
		// add all attributes 
		for (IAttribute a: dataset.getAttributes()) {
			allowedAttributes.add(a);
		}
		
		// remove existing attributes from allowed list
		for (ConditionBase c : rule.getPremise().getSubconditions()) {
			for (String a: c.getAttributes()) {
				allowedAttributes.remove(dataset.getAttributes().get(a));
			}
		}
		
		// try to induce / extend using preferred conditions
		if ((isRuleEmpty && knowledge.isInduceUsingPreferred()) ||
			(!isRuleEmpty && knowledge.isExtendUsingPreferred())) {
			
			boolean carryOn = true;
			int preferredCounter = knowledge.getPreferredConditionsPerRule();
						
			while (carryOn) {
				ConditionEvaluation bestEvaluation = new ConditionEvaluation();
				carryOn = false;
				
				// select best condition (in terms of rule quality).
				for (CompoundCondition candidate : knowledge.getPreferredConditions()) {
					List<IAttribute> attrs = new ArrayList<IAttribute>();
					for (String name: candidate.getAttributes()) {
						attrs.add(dataset.getAttributes().get(name));
					}
					if (!allowedAttributes.containsAll(attrs)) {
						continue;
					}

					checkCandidate(dataset, rule, candidate, uncovered, covered, bestEvaluation);
				}
				
				if (bestEvaluation.condition != null) {
					List<IAttribute> attrs = new ArrayList<IAttribute>();
					for (String name: bestEvaluation.condition.getAttributes()) {
						attrs.add(dataset.getAttributes().get(name));
					}
					allowedAttributes.removeAll(attrs);
					knowledge.getPreferredConditions().remove(bestEvaluation.condition);
					
					rule.getPremise().addSubcondition(bestEvaluation.condition);
					rule.setCoveringInformation(bestEvaluation.covering);

			//		rule.updateWeightAndPValue(dataset, covering, params.getVotingMeasure());

					carryOn = true;
					Logger.log("Preferred condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
					
					if (--preferredCounter == 0) {
						carryOn = false;
					}
				}
			}
		}
		
	
		// try to extend using preferred attributes
		if ((isRuleEmpty && knowledge.isInduceUsingPreferred()) ||
			(!isRuleEmpty && knowledge.isExtendUsingPreferred())) {
			boolean carryOn = true;
			int preferredCounter = knowledge.getPreferredAttributesPerRule();
			
			// create temporary collection of preferred attributes
			Set<IAttribute> localAllowed = new TreeSet<IAttribute>(new AttributeComparator());
			Set<IAttribute> used = new TreeSet<IAttribute>(new AttributeComparator());
			for (IAttribute a: allowedAttributes) {
				if (knowledge.getPreferredAttributes().contains(a.getName())) {
					localAllowed.add(a);
				}
			}
			
			do {
				ElementaryCondition condition = induceCondition(rule, dataset, uncovered, covered, localAllowed);
					
				if (condition != null) {
					// do not allow to use this attribute later in this rule
					localAllowed.remove(dataset.getAttributes().get(condition.getAttribute()));
					used.add(dataset.getAttributes().get(condition.getAttribute()));
					// remove this attribute from preferred attributes
					knowledge.getPreferredAttributes().remove(condition.getAttribute());
					
					condition.setType(ConditionBase.Type.PREFERRED);
					rule.getPremise().addSubcondition(condition);

					// update covering
					covering.clear();
					positives.clear();
					negatives.clear();
					rule.covers(dataset, covering, positives, negatives);
					covered.clear();
					covered.addAll(positives);
					covered.addAll(negatives);

					rule.setCoveredPositives(positives);
					rule.setCoveredNegatives(negatives);
					rule.setCoveringInformation(covering);

					Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
					
					if (--preferredCounter == 0) {
						carryOn = false;
					}
					
				} else {
					carryOn = false;
				}
				
			} while (carryOn);
			
			// remove already utilised attributes from allowed collection
			for (IAttribute a: used) {
				allowedAttributes.remove(a);
			}
		}
		

		// try to extend using automatic conditions
		// eliminate forbidden attributes
		for (String a: knowledge.getForbiddenAttributes()) {
			allowedAttributes.remove(dataset.getAttributes().get(a));
		}
		
		if ((isRuleEmpty && knowledge.isInduceUsingAutomatic()) ||
			(!isRuleEmpty && knowledge.isExtendUsingAutomatic())) {
			boolean carryOn = true;
			
			do {
				ElementaryCondition condition = induceCondition(
						rule, dataset, uncovered, covered, allowedAttributes);
					
				if (condition != null) {
					rule.getPremise().addSubcondition(condition);
					covering.clear();
					rule.covers(dataset, covering, covering.positives, covering.negatives);
					
					covered.clear();
					covered.addAll(covering.positives);
					covered.addAll(covering.negatives);

					rule.getCoveredPositives().setAll(covering.positives);
					rule.getCoveredNegatives().setAll(covering.negatives);

					rule.setCoveringInformation(covering);
			//		rule.updateWeightAndPValue(dataset, covering, params.getVotingMeasure());
					
					Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
				} else {
					carryOn = false;
				}
				
			} while (carryOn); 
		}

		covering.clear();
		positives.clear();
		negatives.clear();
		rule.covers(dataset, covering, positives, negatives);

		// ugly
		covering.positives = positives;
		covering.negatives = negatives;
		rule.setCoveringInformation(covering);

		rule.setCoveredNegatives(negatives);
		rule.setCoveredPositives(positives);

		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
		rule.setInducedContitionsCount(addedConditionsCount);

		if (addedConditionsCount > 0) {
			rule.updateWeightAndPValue(dataset, covering, params.getVotingMeasure());
		}

		return addedConditionsCount;
	}
	
	@Override
	protected boolean checkCandidate(
			IExampleSet dataset,
			Rule rule,
			ConditionBase candidate,
			Set<Integer> uncovered,
			Set<Integer> covered,
			ConditionEvaluation currentBest) {

		boolean ok = super.checkCandidate(dataset, rule, candidate, uncovered, covered, currentBest);

		// verify knowledge only on elementary conditions
		if (ok && candidate instanceof  ElementaryCondition) {
			ok &= !knowledge.isForbidden(
					((ElementaryCondition)candidate).getAttribute(),
					((ElementaryCondition)candidate).getValueSet());
		}

		return ok;
	}
}
