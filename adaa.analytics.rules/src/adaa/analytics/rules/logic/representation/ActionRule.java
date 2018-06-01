package adaa.analytics.rules.logic.representation;

import java.util.Set;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.induction.ActionCovering;
import adaa.analytics.rules.logic.induction.Covering;

public class ActionRule extends Rule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7552850511136098386L;

	protected Action actionConsequence;
	protected ActionCovering coveringInformation;
	
	public String toString() {
		String premiseText = premise == null ? "" :premise.toString(); 
		boolean degenerated = premise.subconditions.stream()
			.map(Action.class::cast)
			.allMatch(x -> x.getActionNil() || x.isLeftEqualRight() || x.isDisabled());
			
		String consequenceText = null;
		if (degenerated) {
			consequenceText = actionConsequence.getLeftCondition().toString();
		} else {
			consequenceText = actionConsequence.toString();
		}
		
		String s = "IF " + premiseText + " THEN " + consequenceText;	
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
		
		this.coveringInformation = rref.coveringInformation;
		
		this.premise = ref.premise;
		actionConsequence = rref.actionConsequence;
	}

	
	private void updateCoveringForSubrule(
			Covering cov,
			Example ex,
			Rule rule,
			int id,
			double w) {
		
	
		boolean consequenceAgree = rule.getConsequence().evaluate(ex);
		if (consequenceAgree) {
			cov.weighted_P += w;
		} else {
			cov.weighted_N += w;
		}
		
		if (rule.getPremise().evaluate(ex)) {
			if (consequenceAgree) {
				cov.positives.add(id);
				cov.weighted_p += w;
			} else {
				cov.negatives.add(id);
				cov.weighted_n += w;
			}
		}
	}
	
	@Override
	public Covering covers(ExampleSet set, Set<Integer> ids) {
		ActionCovering covered = new ActionCovering();
		
		Rule rightRule = this.getRightRule();
		Rule leftRule = this.getLeftRule();
		
		Covering leftCov = new Covering();
		Covering rightCov = new Covering();
		
		for (int id : ids) {
			Example ex = set.getExample(id);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			updateCoveringForSubrule(leftCov, ex, leftRule, id, w);
			updateCoveringForSubrule(rightCov, ex, rightRule, id, w);
		}
		
		covered.weighted_p = leftCov.weighted_p;
		covered.weighted_pRight =  rightCov.weighted_p;
		covered.weighted_n = leftCov.weighted_n;
		covered.weighted_nRight =  rightCov.weighted_n;
		covered.weighted_P = leftCov.weighted_P;
		covered.weighted_N = leftCov.weighted_N;
		
		covered.positives = leftCov.positives;
		covered.negatives = leftCov.negatives;
		
		return covered;
	}
	
	@Override
	public Covering covers(ExampleSet set) {
		
		ActionCovering covered = new ActionCovering();
		
		Rule rightRule = this.getRightRule();
		Rule leftRule = this.getLeftRule();
		
		Covering leftCov = new Covering();
		Covering rightCov = new Covering();
		
		for (int i = 0; i < set.size(); i++) {
			
			Example ex = set.getExample(i);
			double w = set.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
			
			updateCoveringForSubrule(leftCov, ex, leftRule, i, w);
			updateCoveringForSubrule(rightCov, ex, rightRule, i, w);
			
		}
		
		covered.weighted_p = leftCov.weighted_p;
		covered.weighted_pRight =  rightCov.weighted_p;
		covered.weighted_n = leftCov.weighted_n;
		covered.weighted_nRight =  rightCov.weighted_n;
		covered.weighted_P = leftCov.weighted_P;
		covered.weighted_N = leftCov.weighted_N;
		
		covered.positives = leftCov.positives;
		covered.negatives = leftCov.negatives;
		
		return covered;
	}

	public ElementaryCondition getConsequence() {
		return actionConsequence;
	}
	
	public Rule getLeftRule() {
		
		CompoundCondition premise = new CompoundCondition();
		for (ConditionBase a : this.getPremise().getSubconditions()) {
			if (a.isDisabled()) {
				continue;
			}
			Action ac = (Action)a;
			premise.addSubcondition(new ElementaryCondition(ac.getAttribute(), ac.getLeftValue()));
		}
		
		Rule r = new ClassificationRule(premise, new ElementaryCondition(actionConsequence.getAttribute(), actionConsequence.getLeftValue()));
		
		return r;
	}
	
	public Rule getRightRule() {
		
		CompoundCondition premise = new CompoundCondition();
		for (ConditionBase a : this.getPremise().getSubconditions()) {
			if (a.isDisabled()){
				continue;
			}
			Action ac = (Action)a;
			if (ac.getRightValue() != null && !ac.getActionNil()) {
				premise.addSubcondition(new ElementaryCondition(ac.getAttribute(), ac.getRightValue()));
			}
		}
		
		Rule r = new ClassificationRule(premise, new ElementaryCondition(actionConsequence.getAttribute(), actionConsequence.getRightValue()));
		
		return r;
	}
	
	@Override
	public void setCoveringInformation(Covering cov) {
		super.setCoveringInformation(cov);
		if (cov instanceof ActionCovering)
			this.coveringInformation = (ActionCovering)cov;
	}
	
	@Override
	public Covering getCoveringInformation() {
		
		return this.coveringInformation;
	}
	
	public String printStats() {
		return "(pl=" + weighted_p + ", nl=" + weighted_n + ", pr=" + this.coveringInformation.weighted_pRight + 
				", nr=" + this.coveringInformation.weighted_nRight + ", P=" + weighted_P + ", N=" + weighted_N + 
				", weight=" + getWeight() + ")";
	}
}
