package adaa.analytics.rules.logic.representation;

import java.util.logging.Level;

public class BackwardActionRuleAdapter {

	protected ActionRule aRule;

	public BackwardActionRuleAdapter(ActionRule rule) {
		aRule = rule;
		
	}
	
	public ActionRule get() {
		Action oldConsequence = ((Action)(aRule.getConsequence()));
		Action consequence = new Action(oldConsequence.attribute, oldConsequence.rightValue, oldConsequence.leftValue);
		
		Logger.log(aRule.toString(), Level.ALL);
		
		CompoundCondition premise = new CompoundCondition();
		
		aRule
			.getPremise()
			.getSubconditions()
			.stream()
			.map(Action.class::cast)
			.map(x -> Action.ReversedAction(x))
			.forEach(x -> premise.addSubcondition(x));


		return new ActionRule(premise, consequence);
	}

}
