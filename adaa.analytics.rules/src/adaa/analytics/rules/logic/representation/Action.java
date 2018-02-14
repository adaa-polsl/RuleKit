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
		
		return "(" + attribute + ", " + leftValue.toString() + "->" + ((rightValue == null || this.isNilAction) ? " " : rightValue.toString() ) + ")";
	}
	
	public ElementaryCondition intersect(ElementaryCondition other) {
		if (other instanceof Action)
		{
			Action ac = (Action)other;
			return (ElementaryCondition) new Action(attribute, 
					this.leftValue.getIntersection(ac.getLeftValue()), 
					rightValue.getIntersection(ac.getRightValue())); 
		}
		return new ElementaryCondition(attribute, this.valueSet.getIntersection(other.getValueSet()));
	}
	
	public boolean isPrunable() {
		return super.isPrunable();
	}
	
	public IValueSet getLeftValue() { return leftValue; }
	public IValueSet getRightValue() { return rightValue;};
	
	public ConditionBase getLeftCondition() {
		return new ElementaryCondition(this.attribute, this.leftValue);
	}
	
	public ConditionBase getRightCondition() {
		return new ElementaryCondition(this.attribute, this.rightValue);
	}
	
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

	private boolean isNilAction = false;
	
	public void setActionNil(boolean isNil) {
		isNilAction = isNil;
	}
	
	public boolean getActionNil() {
		return isNilAction;
	}
	
	@Override
	public int hashCode() {
		int result = this.attribute.hashCode();
		int a = leftValue == null ? 0 : leftValue.hashCode();
		int b = rightValue == null ? 0 : rightValue.hashCode();
		result = 31 * result + 7 * a + 13 * b;
		return result;
	}
	
	@Override
	//Assume that action evaluates, if left part of condition is met
	protected boolean internalEvaluate(Example ex) {

		double val = ex.getValue(ex.getAttributes().get(attribute));

		return leftValue.contains(val);
	}

}
