package adaa.analytics.rules.logic.representation;

public class BackwardActionRuleAdapter {

	protected ActionRule aRule;

	public BackwardActionRuleAdapter(ActionRule rule) {
		aRule = rule;
		
	}
	
	public ActionRule get() {
		Action oldConsequence = ((Action)(aRule.getConsequence()));
		Action consequence = new Action(oldConsequence.attribute, oldConsequence.rightValue, oldConsequence.leftValue);
		
		
		CompoundCondition premise = new CompoundCondition();
		
		aRule
			.getPremise()
			.getSubconditions()
			.stream()
			.map(Action.class::cast)
			.map(x -> new Action(x.attribute, x.rightValue == null || x.getActionNil() ? x.leftValue : x.rightValue, x.leftValue))
			.forEach(x -> premise.addSubcondition(x));
		
		
		return new ActionRule(premise, consequence);
	}

}
