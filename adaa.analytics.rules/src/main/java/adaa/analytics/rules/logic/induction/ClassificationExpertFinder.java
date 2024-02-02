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

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.ConditionBase.Type;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.apache.commons.lang3.SerializationUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

/**
 * Class for growing and pruning classification rules with user's knowledge.
 * 
 * @author Adam Gudys
 *
 */
public class ClassificationExpertFinder extends ClassificationFinder implements IExpertFinder {
	
	/**
	 * User's knowledge.
	 */
	protected Knowledge knowledge;
	
	/**
	 * Sets users knowledge.
	 * @param knowledge User's knowledge to be set.
	 */
	public void setKnowledge(Knowledge knowledge) {
		this.knowledge = knowledge;
	}
	
	/**
	 * Invokes base class constructor and initializes knowledge object.
	 * 
	 * @param params Induction parameters.
	 * @param knowledge Expert knowledge.
	 */
	public ClassificationExpertFinder(final InductionParameters params, Knowledge knowledge) {
		super(params);
		this.knowledge = knowledge;
	}
	
	/**
	 * Adjusts expert rule. The procedure specifies value sets of adjustable conditions, i.e., those in the form:
	 * <ul> 
	 * <li> attribute ~= Any,</li>
	 * <li> attribute ~= SomeSet (non-empty intersection of determined value set with SomeSet is required).</li>
	 * </ul>  
	 * @param rule Rule to be adjusted.
	 * @param dataset Training dataset.
	 * @param uncoveredPositives Set of positive examples not covered by the model.
	 */
	public void adjust(
		Rule rule,
		IExampleSet dataset,
		Set<Integer> uncoveredPositives) {
		
		CompoundCondition expertPremise = rule.getPremise();
		rule.setPremise(new CompoundCondition());
		
		HashSet<Integer> covered = new HashSet<Integer>();
		
		// bit vectors for faster operations on coverings

		covered.addAll(rule.getCoveredPositives());
		covered.addAll(rule.getCoveredNegatives());
		
		for (ConditionBase cnd : expertPremise.getSubconditions()) {
			ElementaryCondition ec = (ElementaryCondition)cnd;
			ElementaryCondition newCondition;
			
			if (ec.isAdjustable()) {
				// determine attribute
				Set<IAttribute> attr = new TreeSet<IAttribute>(new AttributeComparator());
				attr.add(dataset.getAttributes().get(ec.getAttribute()));
				
				Set<Integer> mustBeCovered;
				
				if (ec.getValueSet() instanceof Universum) {
					// condition in a form "attribute = Any" - just find the best condition using this attribute
					mustBeCovered = uncoveredPositives;
					
				} else {
					// condition in other form - find the best condition using this attribute with non-empty intersection with specified condition
					mustBeCovered = new IntegerBitSet(dataset.size());
					ec.evaluate(dataset, mustBeCovered);
					mustBeCovered.retainAll(rule.getCoveredPositives());
				}
				
				newCondition = induceCondition(
						rule, dataset, mustBeCovered, covered, attr);
				newCondition.setType(Type.FORCED);
				tryAddCondition(rule, null, newCondition, dataset, covered, uncoveredPositives);
				
			} else {
				// add condition as it is without verification
				IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
				newCondition = SerializationUtils.clone(ec);
				newCondition.evaluate(dataset, conditionCovered);
				newCondition.setCovering(conditionCovered);

				rule.getPremise().addSubcondition(newCondition);

				covered.retainAll(conditionCovered);
				rule.getCoveredPositives().retainAll(conditionCovered);
				rule.getCoveredNegatives().retainAll(conditionCovered);

				rule.setWeighted_p(rule.getCoveredPositives().size());
				rule.setWeighted_n(rule.getCoveredNegatives().size());

				rule.updateWeightAndPValue(dataset, rule.getCoveringInformation(), params.getVotingMeasure());

				Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: "
						+ rule.toString() + " " + rule.printStats() + "\n", Level.FINER);
			}
		}

		
		ContingencyTable ct = new ContingencyTable();
		
		if (dataset.getAttributes().getWeight() != null) {
			rule.covers(dataset, ct, new HashSet<Integer>(), new HashSet<Integer>());
		} else {
			ct = new ContingencyTable(
				rule.getWeighted_p(),
				rule.getWeighted_n(),
				rule.getWeighted_P(),
				rule.getWeighted_N());
		}

		rule.updateWeightAndPValue(dataset, ct, params.getVotingMeasure());
	}
	
	/**
	 * Adds elementary conditions to the classification rule premise until termination conditions are fulfilled.
	 * The method uses expert knowledge.
	 * 
	 * @param rule Rule to be grown.
	 * @param dataset Training set.
	 * @param uncoveredPositives Set of positive examples yet uncovered by the model.
	 * @return Number of conditions added.
	 */
	@Override 
	public int grow(
			Rule rule,
			IExampleSet dataset,
			Set<Integer> uncoveredPositives)
	{
		Logger.log("ClassificationExpertFinder.grow()\n", Level.FINE);
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		
		boolean isRuleEmpty = rule.getPremise().getSubconditions().size() == 0;
		double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
		double apriori_prec = rule.getWeighted_P() / (rule.getWeighted_P() + rule.getWeighted_N());
		IAttribute weightAttr = dataset.getAttributes().getWeight();
		
		// get current covering
		Set<Integer> covered = new IntegerBitSet(dataset.size());
		
		// create bit vectors for fast covering operations
		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
		IntegerBitSet newlyCoveredPositives = new IntegerBitSet(dataset.size());

		covered.addAll(rule.getCoveredPositives());
		covered.addAll(rule.getCoveredNegatives());
		newlyCoveredPositives.addAll(uncoveredPositives);
		
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
				CompoundCondition bestCondition = null;
				double bestQuality = Double.NEGATIVE_INFINITY;
				carryOn = false;
				int mostCovered = 0;
				
				// select best condition (in terms of rule quality).
				for (CompoundCondition candidate : knowledge.getPreferredConditions((int)classId)) {
					
					// all attributes in a preferred condition must be allowed
					if (!allowedAttributes.containsAll(names2attributes(candidate.getAttributes(), dataset))) {	
						continue;
					}
					
					conditionCovered.clear();
					candidate.evaluate(dataset, conditionCovered);
					double p = 0, n = 0;
					int newlyCoveredPositivesCount = 0;
					
					if (weightAttr != null) {
						// collect newly covered examples
						for (int id: conditionCovered) {
							if (rule.getCoveredPositives().contains(id)) {
								p += dataset.getExample(id).getWeight();
							} else if (rule.getCoveredNegatives().contains(id)) {
								n += dataset.getExample(id).getWeight();
							}
						}
						
					} else {
						p = rule.getCoveredPositives().calculateIntersectionSize(conditionCovered);
						n = rule.getCoveredNegatives().calculateIntersectionSize(conditionCovered);
						
						newlyCoveredPositivesCount = rule.getCoveredPositives().calculateIntersectionSize(conditionCovered, newlyCoveredPositives);
					}
					double prec = p / (p + n);
					
					if (newlyCoveredPositivesCount >= params.getAbsoluteMinimumCovered(rule.getWeighted_P()) && prec > apriori_prec) {
					
						double q = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								p, n, rule.getWeighted_P(), rule.getWeighted_N());
						
						Logger.log("\tPreferred candidate: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double)mostCovered + ", quality="  + q, Level.FINEST);
						
						// analyse condition only if coverage decreased 
						// select better quality or same quality with higher coverage
						if (q > bestQuality || (q == bestQuality && p > mostCovered)) {
							bestCondition = candidate;
							bestQuality = q;
							mostCovered = (int)p;
							Logger.log("\tSELECTED\n", Level.FINEST);
						} else {
							Logger.log("\n", Level.FINEST);
						}
					}
				}
				
				if (bestCondition != null) {
					carryOn = tryAddCondition(rule, null, bestCondition, dataset, covered, uncoveredPositives);
					knowledge.getPreferredConditions((int)classId).remove(bestCondition);
					
					newlyCoveredPositives.retainAll(rule.getCoveredPositives());
					
					allowedAttributes.removeAll(names2attributes(bestCondition.getAttributes(), dataset));
					
					if (--preferredCounter == 0) {
						carryOn = false;
					}
				}
			}
		}
		
		// try to induce / extend using preferred attributes
		if ((isRuleEmpty && knowledge.isInduceUsingPreferred()) ||
			(!isRuleEmpty && knowledge.isExtendUsingPreferred())) {
			
			// create temporary collection of preferred attributes
			Set<IAttribute> used = new TreeSet<IAttribute>(new AttributeComparator());
			Set<IAttribute> localAllowed = new TreeSet<IAttribute>(new AttributeComparator());
			for (IAttribute a: allowedAttributes) {
				if (knowledge.getPreferredAttributes((int)classId).contains(a.getName())) {
					localAllowed.add(a);
				}
			}
			
			// condition loop
			boolean carryOn = true;
			int preferredCounter = knowledge.getPreferredAttributesPerRule();
						
			do {
				ElementaryCondition condition = induceCondition(rule, dataset, uncoveredPositives, covered, localAllowed, rule.getCoveredPositives());
				carryOn = tryAddCondition(rule, null, condition, dataset, covered,uncoveredPositives);
				// fixme: we are not sure if condition was added
				if (carryOn) {
					knowledge.getPreferredAttributes((int)classId).remove(condition.getAttribute());
					localAllowed.remove(dataset.getAttributes().get(condition.getAttribute()));
					used.add(dataset.getAttributes().get(condition.getAttribute()));
					condition.setType(ConditionBase.Type.PREFERRED);
					
					if (--preferredCounter == 0) {
						carryOn = false;
					}
				}
			} while (carryOn); 
			
			// remove already utilised attributes from allowed collection
			for (IAttribute a: used) {
				allowedAttributes.remove(a);
			}
		}
		
		// try to extend using automatic conditions
		// eliminate forbidden attributes
		for (String a: knowledge.getForbiddenAttributes((int)classId)) {
			allowedAttributes.remove(dataset.getAttributes().get(a));
		}
		
		if ((isRuleEmpty && knowledge.isInduceUsingAutomatic()) ||
			(!isRuleEmpty && knowledge.isExtendUsingAutomatic())) {
			boolean carryOn = true;
			Rule currentRule = new ClassificationRule();
			currentRule.copyFrom(rule);

			do {
				ElementaryCondition condition = induceCondition(
						rule, dataset, uncoveredPositives, covered, allowedAttributes, rule.getCoveredPositives());

				if (params.getSelectBestCandidate()) {
					carryOn = tryAddCondition(currentRule, rule, condition, dataset, covered, uncoveredPositives);
				} else {
					carryOn = tryAddCondition(rule, null, condition, dataset, covered, uncoveredPositives);
				}

			} while (carryOn); 
		}
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;

		rule.setInducedContitionsCount(addedConditionsCount);
		return addedConditionsCount;
	}
	
	/***
	 * Checks if candidate condition fulfills coverage requirement and is not in conflict with forbidden knowledge.
	 * 
	 * @param cnd Candidate condition.
	 * @param classId Class identifier.
	 * @return
	 */
	@Override
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double p, double n, double new_p, double P,double uncoveredSize,  int ruleOrderNum) {
		double adjustedMinCov =
				Math.min(
						params.getAbsoluteMinimumCovered(P),
						Math.max(1.0, 0.2 * P));
		if (new_p >= adjustedMinCov && p >= params.getAbsoluteMinimumCoveredAll(P)) {
			return true &&
					!knowledge.isForbidden(cnd.getAttribute(), cnd.getValueSet(), (int)classId);
		} else {
			return false;
		}
	}
	
	
}
