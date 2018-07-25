package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.lang.SerializationUtils;

import adaa.analytics.rules.logic.induction.AbstractFinder.QualityAndPValue;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ConditionBase.Type;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;
import adaa.analytics.rules.logic.representation.Universum;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;

public class ClassificationExpertFinder extends ClassificationFinder implements IExpertFinder {
	
	protected Knowledge knowledge;
	
	public void setKnowledge(Knowledge knowledge) {
		this.knowledge = knowledge;
	}
	
	public ClassificationExpertFinder(final InductionParameters params, Knowledge knowledge) {
		super(params);
		this.knowledge = knowledge;
	}
	
	public void adjust(
		Rule rule,
		ExampleSet dataset, 
		Set<Integer> uncoveredPositives) {
		
		CompoundCondition expertPremise = rule.getPremise();
		rule.setPremise(new CompoundCondition());

		for (ConditionBase cnd : expertPremise.getSubconditions()) {
			ElementaryCondition ec = (ElementaryCondition)cnd;
			if (ec.isAdjustable()) {
				
				// update covering information - needed for automatic induction
				Covering covering = rule.covers(dataset);
				Set<Integer> covered = new HashSet<Integer>();
				covered.addAll(covering.positives);
				covered.addAll(covering.negatives);
				rule.setCoveringInformation(covering);
				
				// determine attribute
				Set<Attribute> attr = new TreeSet<Attribute>(new AttributeComparator());
				attr.add(dataset.getAttributes().get(ec.getAttribute()));
				
				Set<Integer> mustBeCovered;
				
				if (ec.getValueSet() instanceof Universum) {
					// condition in a form "attribute = Any" - just find best condition using this attribute
					mustBeCovered = uncoveredPositives;
					
				} else {
					// condition in other form - find best condition using this attribute with non-empty intersection with specified condition
					mustBeCovered = new HashSet<Integer>();
					for (int i : covering.positives) {
						if (ec.evaluate(dataset.getExample(i))) {
							mustBeCovered.add(i);
						}
					}	
				}
				
				ElementaryCondition newCondition = induceCondition(
						rule, dataset, mustBeCovered, covered, attr);
				
				boolean carryOn = tryAddCondition(rule, newCondition, dataset, covered);
				
				if (carryOn) {
					newCondition.setType(Type.FORCED);
					rule.getPremise().addSubcondition(newCondition);
				}
				
			} else {
				rule.getPremise().addSubcondition((ElementaryCondition)SerializationUtils.clone(ec));
			}
		}
		
		Covering covering = rule.covers(dataset);
		rule.setCoveringInformation(covering);
		
		QualityAndPValue qp = calculateQualityAndPValue(dataset, covering, params.getVotingMeasure());
		rule.setWeight(qp.quality);
		rule.setPValue(qp.pvalue);		
	}
	
	
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
		
		// get current covering
		Covering covering = rule.covers(dataset);
		Set<Integer> covered = new HashSet<Integer>();
		covered.addAll(covering.positives);
		covered.addAll(covering.negatives);
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
						
					rule.getPremise().addSubcondition(candidate);
					Covering cov = rule.covers(dataset, covered);
					rule.getPremise().removeSubcondition(candidate);
					
					if (checkCandidateCoverage(cov.weighted_p + cov.weighted_n)) {
					
						double q = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
								cov.weighted_p, cov.weighted_n, rule.getWeighted_P(), rule.getWeighted_N());
						
						// analyse condition only if coverage decreased 
						// select better quality or same quality with higher coverage
						if ((cov.getSize() < covered.size()) && (q > bestQuality || (q == bestQuality && cov.positives.size() > mostCovered))) {
							bestCondition = candidate;
							bestQuality = q;
							mostCovered = cov.positives.size();
						}
					}
				}
				
				if (bestCondition != null) {
					carryOn = tryAddCondition(rule, bestCondition, dataset, covered);
					knowledge.getPreferredConditions((int)classId).remove(bestCondition);
					
					allowedAttributes.removeAll(names2attributes(bestCondition.getAttributes(), dataset));
					
					Logger.log("Preferred condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
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
				ElementaryCondition condition = induceCondition(rule, dataset, uncoveredPositives, covered, localAllowed);	
				carryOn = tryAddCondition(rule, condition, dataset, covered);
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
						rule, dataset, uncoveredPositives, covered, allowedAttributes);
				
				carryOn = tryAddCondition(rule, condition, dataset, covered);
			} while (carryOn); 
		}
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
	
		if (addedConditionsCount > 0) {
			covering = rule.covers(dataset);
			rule.setCoveringInformation(covering);
			
			QualityAndPValue qp = calculateQualityAndPValue(dataset, covering, params.getVotingMeasure());
			rule.setWeight(qp.quality);
			rule.setPValue(qp.pvalue);
		}
		
		rule.setInducedContitionsCount(addedConditionsCount);
		return addedConditionsCount;
	}
	
	@Override
	protected boolean checkCandidate(ElementaryCondition cnd, double classId, double covered) {
		return super.checkCandidate(cnd, classId, covered) &&
			!knowledge.isForbidden(cnd.getAttribute(), cnd.getValueSet(), (int)classId);
	}
	
	/**
	 * Tries to add condition to the rule. 
	 * @param trainSet
	 * @param rule
	 * @param condition
	 * @param covered
	 * @return value indicating whether condition adding loop should be continued
	 */
	public boolean tryAddCondition(
		final Rule rule, 
		final ConditionBase condition, 
		final ExampleSet trainSet,
		final Set<Integer> covered) {
		
		boolean carryOn = true;
		boolean added = false;
		Covering covering = null;
		
		if (condition != null) {
			rule.getPremise().addSubcondition(condition);
			covering = rule.covers(trainSet, covered); // after adding condition rule always covers less examples

			// analyse stopping criteria
			if (covering.weighted_p + covering.weighted_n < params.getMinimumCovered()) {
				// if drops below minimum - remove condition and do not update statistics
				if (rule.getPremise().getSubconditions().size() > 1) {
					rule.getPremise().removeSubcondition(condition);
				} else {
					added = true;
				}
				carryOn = false;
			} else {
				// exact rule
				if (covering.negatives.size() == 0) { 
					carryOn = false; 
				}
				added = true;
			}
			
			// update coverage if condition was added
			if (added) {
				covered.clear();
				covered.addAll(covering.positives);
				covered.addAll(covering.negatives);

				rule.setCoveringInformation(covering);
				QualityAndPValue qp = calculateQualityAndPValue(trainSet, covering, params.getVotingMeasure());
				rule.setWeight(qp.quality);
				rule.setPValue(qp.pvalue);
				
				Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
						+ rule.toString() + "\n", Level.FINER);
			}
		}
		else {
			 carryOn = false;
		}
	
		return carryOn;
	}	
}
