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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.container.Pair;

import org.apache.commons.lang.SerializationUtils;

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
		ExampleSet dataset, 
		Set<Integer> uncoveredPositives) {
		
		CompoundCondition expertPremise = rule.getPremise();
		rule.setPremise(new CompoundCondition());
		
		HashSet<Integer> covered = new HashSet<Integer>();
		
		// bit vectors for faster operations on coverings
		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
		IntegerBitSet coveredPositives = new IntegerBitSet(dataset.size());
		IntegerBitSet coveredNegatives = new IntegerBitSet(dataset.size());
		
		// assume empty premise at the beginning (rule covers all examples)
		int id = 0;
		for (Example e : dataset) {
			if (rule.getConsequence().evaluate(e)) {
				coveredPositives.add(id);
			} else {
				coveredNegatives.add(id);
			}
			++id;
		}
		covered.addAll(coveredPositives);
		covered.addAll(coveredNegatives);
		
		for (ConditionBase cnd : expertPremise.getSubconditions()) {
			ElementaryCondition ec = (ElementaryCondition)cnd;
			ElementaryCondition newCondition;
			
			if (ec.isAdjustable()) {
				IntegerBitSet tempPositives = new IntegerBitSet(dataset.size());
				ec.evaluate(dataset, tempPositives);
				tempPositives.retainAll(coveredPositives);
				
				// determine attribute
				Set<Attribute> attr = new TreeSet<Attribute>(new AttributeComparator());
				attr.add(dataset.getAttributes().get(ec.getAttribute()));
				
				Set<Integer> mustBeCovered;
				
				if (ec.getValueSet() instanceof Universum) {
					// condition in a form "attribute = Any" - just find the best condition using this attribute
					mustBeCovered = uncoveredPositives;
					
				} else {
					// condition in other form - find the best condition using this attribute with non-empty intersection with specified condition
					mustBeCovered = new HashSet<Integer>();
					for (int i : tempPositives) {
						if (ec.evaluate(dataset.getExample(i))) {
							mustBeCovered.add(i);
						}
					}	
				}
				
				newCondition = induceCondition(
						rule, dataset, mustBeCovered, covered, attr, coveredPositives);
				newCondition.setType(Type.FORCED);	
				
			} else {
				newCondition = (ElementaryCondition)SerializationUtils.clone(ec);
			}
			
			tryAddCondition(rule, newCondition, dataset, covered, coveredPositives, coveredNegatives, conditionCovered);
		}
		
		ContingencyTable ct; 
		
		if (dataset.getAttributes().getWeight() != null) {
			ct = rule.covers(dataset);
			
		} else {
			ct = new ContingencyTable(
				coveredPositives.size(),
				coveredNegatives.size(),
				rule.getWeighted_P(),
				rule.getWeighted_N());
		}
		
		rule.setWeighted_p(ct.weighted_p);
		rule.setWeighted_n(ct.weighted_n);
		Pair<Double,Double> qp = calculateQualityAndPValue(dataset, ct, params.getVotingMeasure());
		rule.setWeight(qp.getFirst());
		rule.setPValue(qp.getSecond());		
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
			ExampleSet dataset, 
			Set<Integer> uncoveredPositives)
	{
		Logger.log("ClassificationExpertFinder.grow()\n", Level.FINE);
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		
		boolean isRuleEmpty = rule.getPremise().getSubconditions().size() == 0;
		double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
		double apriori_prec = rule.getWeighted_P() / (rule.getWeighted_P() + rule.getWeighted_N());
		Attribute weightAttr = dataset.getAttributes().getWeight();
		
		// get current covering
		Set<Integer> covered = new IntegerBitSet(dataset.size());
		
		// create bit vectors for fast covering operations
		IntegerBitSet conditionCovered = new IntegerBitSet(dataset.size());
		IntegerBitSet coveredPositives = new IntegerBitSet(dataset.size());
		IntegerBitSet coveredNegatives = new IntegerBitSet(dataset.size());
		IntegerBitSet newlyCoveredPositives = new IntegerBitSet(dataset.size());
		
		ContingencyTable ct = new ContingencyTable();
		rule.covers(dataset, ct, coveredPositives, coveredNegatives);
		
		covered.addAll(coveredPositives);
		covered.addAll(coveredNegatives);
		newlyCoveredPositives.addAll(uncoveredPositives);
		
		Set<Attribute> allowedAttributes = new TreeSet<Attribute>(new AttributeComparator());
		
		// add all attributes
		for (Attribute a: dataset.getAttributes()) {
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
						
						for (int id: conditionCovered) {
							if (coveredPositives.contains(id)) {
								p += dataset.getExample(id).getWeight();
							} else if (coveredNegatives.contains(id)) {
								n += dataset.getExample(id).getWeight();
							}
						}
						
					} else {
						p = coveredPositives.calculateIntersectionSize(conditionCovered);
						n = coveredNegatives.calculateIntersectionSize(conditionCovered);
						
						newlyCoveredPositivesCount = coveredPositives.calculateIntersectionSize(conditionCovered, newlyCoveredPositives);
					}
					double prec = p / (p + n);
					
					if (newlyCoveredPositivesCount >= params.getMinimumCovered() && prec > apriori_prec) {
					
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
					carryOn = tryAddCondition(rule, bestCondition, dataset, covered, coveredPositives, coveredNegatives, conditionCovered);
					knowledge.getPreferredConditions((int)classId).remove(bestCondition);
					
					newlyCoveredPositives.retainAll(coveredPositives);
					
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
			Set<Attribute> used = new TreeSet<Attribute>(new AttributeComparator());
			Set<Attribute> localAllowed = new TreeSet<Attribute>(new AttributeComparator());
			for (Attribute a: allowedAttributes) {
				if (knowledge.getPreferredAttributes((int)classId).contains(a.getName())) {
					localAllowed.add(a);
				}
			}
			
			// condition loop
			boolean carryOn = true;
			int preferredCounter = knowledge.getPreferredAttributesPerRule();
						
			do {
				ElementaryCondition condition = induceCondition(rule, dataset, uncoveredPositives, covered, localAllowed, coveredPositives);	
				carryOn = tryAddCondition(rule, condition, dataset, covered, coveredPositives, coveredNegatives, conditionCovered);
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
			for (Attribute a: used) {
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
			
			do {
				ElementaryCondition condition = induceCondition(
						rule, dataset, uncoveredPositives, covered, allowedAttributes, coveredPositives);
				
				carryOn = tryAddCondition(rule, condition, dataset, covered, coveredPositives, coveredNegatives, conditionCovered);
			} while (carryOn); 
		}
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
	
		if (addedConditionsCount > 0) {
			Covering covering = rule.covers(dataset);
			rule.setCoveringInformation(covering);
			
			Pair<Double,Double> qp = calculateQualityAndPValue(dataset, covering, params.getVotingMeasure());
			rule.setWeight(qp.getFirst());
			rule.setPValue(qp.getSecond());
		}
		
		rule.setInducedContitionsCount(addedConditionsCount);
		return addedConditionsCount;
	}
	
	/***
	 * Checks if candidate condition fulfills coverage requirement and is not in conflict with forbidden knowledge.
	 * 
	 * @param cnd Candidate condition.
	 * @param classId Class identifier.
	 * @param newlyCoveredPositives Number of newly covered positive examples after addition of the condition.
	 * @return
	 */
	@Override
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double totalPositives, double newlyCoveredPositives) {
		return super.checkCandidate(cnd, classId, totalPositives, newlyCoveredPositives) &&
			!knowledge.isForbidden(cnd.getAttribute(), cnd.getValueSet(), (int)classId);
	}
	
	
}
