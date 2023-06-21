package adaa.analytics.rules.logic.actions;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import adaa.analytics.rules.logic.representation.ActionRuleSet;

public abstract class Recommendation {

	public Recommendation() {
		// TODO Auto-generated constructor stub
	}
	
	protected String printExampleNicely(Example ex) {
		StringBuilder sb = new StringBuilder();
		
		Iterator<Attribute> it = ex.getAttributes().allAttributes();
			
		while(it.hasNext()) {
			Attribute at = it.next();
			sb.append(at.getName());
			sb.append("=");
			sb.append(ex.getValueAsString(at));
			sb.append("  ");
			
		}

		return sb.toString();
	}
	
	public abstract void train(ExampleSet set);
	public abstract ActionRuleSet test(ExampleSet set);
	
	

}
