package adaa.analytics.rules.logic.representation;

import java.util.HashSet;
import java.util.Set;

import com.rapidminer.example.Example;

public class Action extends ElementaryCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7307576747677085713L;
	
	protected IValueSet leftValue, rightValue;
	
	public Action(String attribute, IValueSet sourceValue, IValueSet targetValue)  {
		
		this.attribute = attribute;
	//	if (!sourceValue.getClass().equals(targetValue.getClass())) {
	//		leftValue = rightValue = null;
	//	}
		leftValue = sourceValue;
		rightValue = targetValue;
	}
	
	public String toString() {
		
		return "(" + attribute + ", " + leftValue.toString() + "->" + (rightValue == null ? " " : rightValue.toString() ) + ")";
	}
	
	public boolean isPrunable() {
		return false;
	}
	
	public IValueSet getLeftValue() { return leftValue; }
	public IValueSet getRightValue() { return rightValue;};
	
	@Override
	public boolean equals(Object ref) {
		
		if (ref == this) {
			return true;
		} else {
			Action act = (ref instanceof Action) ? (Action)ref : null;
			if (act == null)
				return false;
		}
		return false;
	}

	@Override
	public Set<String> getAttributes() {
		HashSet<String> attrs = new HashSet<String>();
		attrs.add(attribute);
		return attrs;
	}

	@Override
	//Assume that action evaluates, if left part of condition is met
	protected boolean internalEvaluate(Example ex) {

		double val = ex.getValue(ex.getAttributes().get(attribute));

		return leftValue.contains(val);
	}

}
