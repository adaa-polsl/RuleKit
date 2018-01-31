package adaa.analytics.rules.logic.representation;

import java.util.Set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.induction.Covering;

public class ActionRule extends Rule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7552850511136098386L;

	protected Action actionConsequence;
	
	public String toString() {
		String s = "IF " + premise.toString() + " THEN " + actionConsequence.toString();	
		return s;
	}
	
	public ActionRule(){
		super();
	}
	
	public ActionRule(CompoundCondition premise, Action consequence) {
		
		this.premise = premise;
		actionConsequence = consequence;
	}
	//can't use super here
	public ActionRule(Rule ref) {
		ActionRule rref = (ref instanceof ActionRule) ? (ActionRule)ref : null;
		if (rref == null)
			throw new RuntimeException("Cannot copy ordinary rule to action rule!");
		
		this.weight = ref.weight;
		this.weighted_p = ref.weighted_p;
		this.weighted_n = ref.weighted_n;
		this.weighted_P = ref.weighted_P;
		this.weighted_N = ref.weighted_N;
		
		this.premise = ref.premise;
		actionConsequence = rref.actionConsequence;
	}
	
	@Override
	public Covering covers(ExampleSet set, Set<Integer> ids) {
		Covering covering = new Covering();
		
		for (int id : ids) {
			Example ex = set.getExample(id);
			double weight = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			boolean consAgree = this.actionConsequence.evaluate(ex);
			if (consAgree) {
				covering.weighted_P += weight;
			} else {
				covering.weighted_N += weight;
			}
			
			if (this.getPremise().evaluate(ex)) {
				if (consAgree) {
					covering.positives.add(id);
					covering.weighted_p += weight;
				} else {
					covering.negatives.add(id);
					covering.weighted_n += weight;
				}
			}
		}
		return covering;
	}

	@Override
	public Covering covers(ExampleSet set) {
		
		Covering covered = new Covering();
		
		for (int id = 0; id < set.size(); ++id) {
			
			Example ex = set.getExample(id);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			boolean consequenceAgree = this.actionConsequence.evaluate(ex);
			if (consequenceAgree) {
				covered.weighted_P += w;
			} else {
				covered.weighted_N += w;
			}
			
			if (this.getPremise().evaluate(ex)) {
				if (consequenceAgree) {
					covered.positives.add(id);
					covered.weighted_p += w;
				} else {
					covered.negatives.add(id);
					covered.weighted_n += w;
				}
			}
		}
		return covered;
	}

	public ElementaryCondition getConsequence() {
		return actionConsequence;
	}
	
	public Rule getLeftRule() {
		
		CompoundCondition premise = new CompoundCondition();
		for (ConditionBase a : this.getPremise().getSubconditions()) {
			Action ac = (Action)a;
			premise.addSubcondition(new ElementaryCondition(ac.getAttribute(), ac.getLeftValue()));
		}
		
		Rule r = new ClassificationRule(premise, new ElementaryCondition(actionConsequence.getAttribute(), actionConsequence.getLeftValue()));
		
		return r;
	}
	
	public Rule getRightRule() {
		
		CompoundCondition premise = new CompoundCondition();
		for (ConditionBase a : this.getPremise().getSubconditions()) {
			Action ac = (Action)a;
			if (ac.getRightValue() != null) {
				premise.addSubcondition(new ElementaryCondition(ac.getAttribute(), ac.getRightValue()));
			}
		}
		
		Rule r = new ClassificationRule(premise, new ElementaryCondition(actionConsequence.getAttribute(), actionConsequence.getRightValue()));
		
		return r;
	}
}
