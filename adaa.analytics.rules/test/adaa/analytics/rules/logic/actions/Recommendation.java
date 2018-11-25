package adaa.analytics.rules.logic.actions;

import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.representation.ActionRuleSet;

public abstract class Recommendation {

	public Recommendation() {
		// TODO Auto-generated constructor stub
	}
	
	public abstract void train(ExampleSet set);
	public abstract ActionRuleSet test(ExampleSet set);
	
	

}
