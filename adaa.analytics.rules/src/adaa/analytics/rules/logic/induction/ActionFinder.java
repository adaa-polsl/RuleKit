package adaa.analytics.rules.logic.induction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.quality.*;
import adaa.analytics.rules.logic.representation.Action;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ClassificationRule;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ConditionBase;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.Interval;
import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.SingletonSet;

public class ActionFinder extends AbstractFinder {

	public ActionFinder(InductionParameters params) {
		super(params);
		// TODO Auto-generated constructor stub
	}
	
	protected ConditionBase getBestElementaryCondition(Set<ElementaryCondition> conditions, ExampleSet trainSet, 
			Rule rule/*, IQualityMeasure quality params.getInductionMeasure...*/){
		
		double bestQ = Double.NEGATIVE_INFINITY;
		ConditionBase best = null;
		
		
		for (ConditionBase cond : conditions) {
			//extend rule with condition
			rule.getPremise().addSubcondition(cond);
			
			Covering cov = rule.covers(trainSet);
			
			double quality = ((ClassificationMeasure)params.getInductionMeasure())
								.calculate(cov.weighted_p, cov.weighted_n, cov.weighted_P, cov.weighted_N);
			if (cov.weighted_p >= params.getMinimumCovered() && quality >= bestQ) {
				bestQ = quality;
				best = cond;
			}
			
			//clean it up
			rule.getPremise().removeSubcondition(cond);
		}
		
		return best;
	}
	
	
	
	protected Set<ElementaryCondition> generateElementaryConditions(ExampleSet trainSet, Set<Attribute> allowedAttributes, Set<Integer> coveredByRule) {
		
		HashSet<ElementaryCondition> conditions = new HashSet<ElementaryCondition>();
		
		for (Attribute atr : allowedAttributes) {
			getElementaryConditionForAttribute(trainSet, coveredByRule, conditions, atr.getName());
		}
		
		return conditions;
	}

	private void getElementaryConditionForAttribute(ExampleSet trainSet, Set<Integer> coveredByRule,
			Set<ElementaryCondition> conditions, String attributeName) {
		
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
		return new Action(left.getAttribute(), left.getValueSet(), right.getValueSet());
	}

	@Override
	protected ElementaryCondition induceCondition(Rule rule, ExampleSet trainSet, Set<Integer> uncoveredByRuleset,
			Set<Integer> coveredByRule, Set<Attribute> allowedAttributes) {
		
		ActionRule aRule = rule instanceof ActionRule ? (ActionRule)rule : null;
		
//		if (aRule == null) {
//			throw new InvalidAttributeValueException("rule");
//		}
		if (aRule == null)
			return null;
		
		double posClass = ((SingletonSet)((Action)aRule.getConsequence()).getLeftValue()).getValue();
		double negClass = ((SingletonSet)((Action)aRule.getConsequence()).getRightValue()).getValue();
		List<String> mapping = trainSet.getAttributes().get(rule.getConsequence().getAttribute()).getMapping().getValues();
		
		Rule posRule = new ClassificationRule(new CompoundCondition()
				, new ElementaryCondition(rule.getConsequence().getAttribute(), new SingletonSet(posClass, mapping)));
		Rule negRule = new ClassificationRule(new CompoundCondition()
				, new ElementaryCondition(rule.getConsequence().getAttribute(), new SingletonSet(negClass, mapping)));
		
		Set<ElementaryCondition> conds = this.generateElementaryConditions(trainSet, allowedAttributes, coveredByRule);
		ConditionBase best = this.getBestElementaryCondition(conds, trainSet, posRule);
		
		Set<Integer> uncoveredByRule = new HashSet<Integer>();
		
		
		
		for (int i = 0; i < trainSet.size(); i++) {
			uncoveredByRule.add(i);
		}
		
		uncoveredByRule.removeAll(uncoveredByRuleset);
		Set<ElementaryCondition> newConds = new HashSet<ElementaryCondition>();
		this.getElementaryConditionForAttribute(trainSet, uncoveredByRule, newConds, ((ElementaryCondition)best).getAttribute());
		ConditionBase otherBest = this.getBestElementaryCondition(newConds, trainSet, negRule);

		Action proposedAction = null;
		try {
			proposedAction = this.buildAction((ElementaryCondition)best, (ElementaryCondition)otherBest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			Covering normalCovering = aRule.covers(trainSet);
			double normalQ = this.calculateQuality(trainSet, normalCovering, params.getInductionMeasure());
			aRule.getPremise().removeSubcondition((ElementaryCondition)proposedAction);
			
			aRule.getPremise().addSubcondition((ElementaryCondition)nilAction);
			Covering nilCovering = aRule.covers(trainSet);
			double nilQ = this.calculateQuality(trainSet, nilCovering, params.getInductionMeasure());
			aRule.getPremise().removeSubcondition((ElementaryCondition)nilAction);
			
			if (normalQ > nilQ) {
				return proposedAction;
			} else {
				return nilAction;
			}
		}
	}

}
