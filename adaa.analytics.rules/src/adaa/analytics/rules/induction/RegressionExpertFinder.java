package adaa.analytics.rules.induction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import adaa.analytics.rules.logic.CompoundCondition;
import adaa.analytics.rules.logic.ConditionBase;
import adaa.analytics.rules.logic.ElementaryCondition;
import adaa.analytics.rules.logic.Knowledge;
import adaa.analytics.rules.logic.Logger;
import adaa.analytics.rules.logic.Rule;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;

public class RegressionExpertFinder extends RegressionFinder {

	protected Knowledge knowledge;
	
	public void setKnowledge(Knowledge knowledge) {
		this.knowledge = knowledge;
	}
	
	public RegressionExpertFinder(InductionParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
	}
	
	@Override 
	public int grow(
			Rule rule,
			ExampleSet dataset, 
			Set<Integer> uncovered)
	{
		Logger.log("RegressionExpertFinder.grow()\n", Level.FINE);
		
		int initialConditionsCount = rule.getPremise().getSubconditions().size();
		boolean isRuleEmpty = rule.getPremise().getSubconditions().size() == 0;
		
		// get current covering
		Covering covering = rule.covers(dataset);
		Set<Integer> covered = new HashSet<Integer>();
		covered.addAll(covering.positives);
		covered.addAll(covering.negatives);
		Set<Attribute> allowedAttributes = new TreeSet<Attribute>(new AttributeComparator());
		
		// add all attributes omitting forbidden ones
		for (Attribute a: dataset.getAttributes()) {
			if (!knowledge.getForbiddenAttributes().contains(a.getName())) {
				allowedAttributes.add(a);
			}
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
			int preferredConditionCounter = knowledge.getPreferredCountPerRule();
						
			while (carryOn) {
				ConditionEvaluation bestEvaluation = new ConditionEvaluation();
				carryOn = false;
				
				// select best condition (in terms of rule quality).
				for (CompoundCondition candidate : knowledge.getPreferredConditions()) {
					List<Attribute> attrs = new ArrayList<Attribute>();
					for (String name: candidate.getAttributes()) {
						attrs.add(dataset.getAttributes().get(name));
					}
					if (!allowedAttributes.containsAll(attrs)) {
						continue;
					}
						
					checkCandidate(dataset, rule, candidate, uncovered, bestEvaluation);
				}
				
				if (bestEvaluation.condition != null) {
					List<Attribute> attrs = new ArrayList<Attribute>();
					for (String name: bestEvaluation.condition.getAttributes()) {
						attrs.add(dataset.getAttributes().get(name));
					}
					allowedAttributes.removeAll(attrs);
					knowledge.getPreferredConditions().remove(bestEvaluation.condition);
					
					rule.getPremise().addSubcondition(bestEvaluation.condition);
					rule.setCoveringInformation(bestEvaluation.covering);
					rule.setWeight(bestEvaluation.quality);
					carryOn = true;
					Logger.log("Preferred condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
					
					if (--preferredConditionCounter == 0) {
						carryOn = false;
					}
				}
			}
		}
		
	
		// try to extend using preferred attributes
		if ((isRuleEmpty && knowledge.isInduceUsingPreferred()) ||
			(!isRuleEmpty && knowledge.isExtendUsingPreferred())) {
			boolean carryOn = true;
			
			// create temporary collection of preferred attributes
			Set<Attribute> localAllowed = new TreeSet<Attribute>(new AttributeComparator());
			Set<Attribute> used = new TreeSet<Attribute>(new AttributeComparator());
			for (Attribute a: allowedAttributes) {
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
					covering = rule.covers(dataset);
					covered.clear();
					covered.addAll(covering.positives);
					covered.addAll(covering.negatives);
					double v = calculateQuality(dataset, covering, params.getInductionMeasure());
					rule.setCoveringInformation(covering);
					rule.setWeight(v);	
					Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
				} else {
					carryOn = false;
				}
				
			} while (carryOn);
			
			// remove already utilised attributes from allowed collection
			for (Attribute a: used) {
				allowedAttributes.remove(a);
			}
		}
		

		// try to extend using automatic conditions
		if ((isRuleEmpty && knowledge.isInduceUsingAutomatic()) ||
			(!isRuleEmpty && knowledge.isExtendUsingAutomatic())) {
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

					double v = calculateQuality(dataset, covering, params.getInductionMeasure());
					rule.setCoveringInformation(covering);
					rule.setWeight(v);
					
					Logger.log("Condition " + rule.getPremise().getSubconditions().size() + " added: " 
							+ rule.toString() + "\n", Level.FINER);
				} else {
					carryOn = false;
				}
				
			} while (carryOn); 
		}
		
		// if rule has been successfully grown
		int addedConditionsCount = rule.getPremise().getSubconditions().size() - initialConditionsCount;
		rule.setInducedContitionsCount(addedConditionsCount);
		return addedConditionsCount;
	}
}
