package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.InductionParameters;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

public class ActionRuleSet extends RuleSetBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActionRuleSet(ExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		super(exampleSet, isVoting, params, knowledge);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double predict(Example example) throws OperatorException {
		// TODO Auto-generated method stub
		return 0;
	}

	private enum SIDE {
		LEFT,
		RIGHT
	}

	private ClassificationRuleSet getOneSidedRuleSet(SIDE side){
		ClassificationRuleSet ruleSet = new ClassificationRuleSet(this.trainingSet, this.isVoting, this.params, this.knowledge);

		for(Rule r : this.getRules()) {
			ActionRule ar = (ActionRule)r;

			switch(side) {
				case LEFT: ruleSet.addRule(ar.getLeftRule()); break;
				case RIGHT: ruleSet.addRule(ar.getRightRule()); break;
				default: throw new RuntimeException("Unknown side of rule");
			}
		}
		return ruleSet;
	}

	public ClassificationRuleSet getSourceRuleSet() {
		return getOneSidedRuleSet(SIDE.LEFT);
	}

	public ClassificationRuleSet getTargetRuleSet() {
		return getOneSidedRuleSet(SIDE.RIGHT);
	}

}
