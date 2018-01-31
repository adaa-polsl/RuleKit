package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

public class ActionRuleSet extends RuleSetBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ActionRuleSet(ExampleSet exampleSet, boolean isVoting, Knowledge knowledge) {
		super(exampleSet, isVoting, knowledge);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double predict(Example example) throws OperatorException {
		// TODO Auto-generated method stub
		return 0;
	}

}
