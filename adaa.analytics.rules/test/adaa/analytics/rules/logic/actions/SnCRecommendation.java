package adaa.analytics.rules.logic.actions;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

import adaa.analytics.rules.logic.actions.ActionMetaTable.AnalysisResult;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.ActionRuleSet;

public class SnCRecommendation extends Recommendation {

	protected ActionSnC engine;
	protected ActionRuleSet rules;
	
	public SnCRecommendation(ActionSnC snc) {
		engine = snc;
	}

	public void train(ExampleSet set) {
		rules = (ActionRuleSet) engine.run(set);
		
	}

	public ActionRuleSet test(ExampleSet set) {
		
		
		return rules;
		
	}
}
