package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;


/// Represents elementary action, which can be of four kind
/// 1. x -> y
/// 2. x -> nil - then isNilAction evaluates to true
/// 3. x -> x - then isLeftEqualRight evaluates to true
/// 4. nil -> x
public class Action extends ElementaryCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7307576747677085713L;
	
	IValueSet leftValue, rightValue;
	
	private void construct(String attribute, IValueSet sourceValue, IValueSet targetValue) {
		this.attribute = attribute;
		leftValue = sourceValue;
		rightValue = targetValue;
	}

	public Action(String attribute, IValueSet sourceValue, IValueSet targetValue)  {
		
		construct(attribute, sourceValue, targetValue);
	}

	public Action(ElementaryCondition left, ElementaryCondition right) {
		if (right != null && left != null && !left.getAttribute().equals(right.getAttribute())) {
			throw new RuntimeException("Non matching attributes");
		}
		if (left == null && right == null) {
			throw new RuntimeException("Can't create empty action");
		}
		String attribute;
		if (left == null) {
			attribute = right.attribute;
		} else {
			attribute = left.attribute;
		}
		construct(attribute, left == null ? null : left.getValueSet(), right == null ? null : right.getValueSet());
	}
	
	static Action ReversedAction(Action act) {
		
		if (act.getActionNil()) {
			return new Action(act.attribute, new AnyValueSet(), act.leftValue);
		}
		
		return new Action(act.attribute, act.rightValue, act.leftValue );
	}
	
	public boolean isLeftEqualRight() {
		if (rightValue == null) {
			return leftValue == null;
		}
		if (leftValue == null) {
			return rightValue == null;
		}
		return leftValue.equals(rightValue);
	}
		
	public String toString() {
		String rightPart = "";
		String leftPart;
		if (this.getActionNil()) {
			rightPart = " -> ";
		} else {
			rightPart = " -> " + rightValue.toString();
		}
		/*
		if (rightValue != null && !this.getActionNil()) {
			if (rightValue.equals(leftValue)) {
				rightPart = "";
			} else {
				rightPart = " -> " + rightValue.toString();
			}
		}

		 */
		if (leftValue == null) {
			leftPart = "";
		} else {
			leftPart = leftValue.toString();
		}
		return "(" + attribute + ", " + leftPart + rightPart + ")";
	}

	/*
		This method is used to compress conditions
		for example if we have in a rule (atr1, x -> y) AND (atr1, z -> v)
		we want to compress it into one - so the effect is we're taking intersection on both side
		in special case of nil actions we have
		1. (atr1, x -> nil) AND (atr1, z -> v) ===> (atr1, x AND z -> v) (because nil means "impute no restrictions")
		2. (atr1, x -> y) AND (atr1, z -> nil) ===> (atr1, x AND z -> y)
		3. (atr1, x -> y) AND (atr1, z -> v) ===> (atr1, x AND z -> y AND v)
		4. (atr1, x -> nil) AND (atr1, z -> nil) ===> (atr1, z AND x -> nil)

	*/
	public ElementaryCondition intersect(ElementaryCondition other) {
		if (other instanceof Action)
		{
			Action ac = (Action)other;
			
			IValueSet left, right;
			if (this.leftValue == null) {
				left = ac.getLeftValue();
			} else {
				left = this.leftValue.getIntersection(ac.getLeftValue());
			}
			if (this.getActionNil()) {
				right = ac.rightValue;
			} else {
				if (ac.getActionNil()) {
					right = this.rightValue;
				} else {
					IValueSet intersection = this.rightValue.getIntersection(ac.rightValue);
					right = intersection;
				}
			}

			
			return new Action(attribute,
					left, 
					right); 
		}
		return new ElementaryCondition(attribute, this.valueSet.getIntersection(other.getValueSet()));
	}
	
	public boolean isPrunable() {
		return super.isPrunable();
	}
	
	public IValueSet getLeftValue() { return leftValue; }
	public IValueSet getRightValue() { return rightValue;}

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
			return new EqualsBuilder()
					.append(this.leftValue, act.leftValue)
					.append(this.rightValue, act.rightValue)
					.append(this.attribute, act.attribute )
					.isEquals();
		}
	}

	@Override
	public Set<String> getAttributes() {
		HashSet<String> attrs = new HashSet<>();
		attrs.add(attribute);
		return attrs;
	}

	public boolean getActionNil() {
		return rightValue == null ;
	}
	
	@Override
	public int hashCode() {
		int result = new HashCodeBuilder().append(this.attribute).append(leftValue).append(rightValue).toHashCode();
		return result;
	}
	
	@Override
	//Assume that action evaluates, if left part of condition is met
	protected boolean internalEvaluate(Example ex) {
		//action of type nil -> value evaluates always
		if (leftValue == null)
			return true;
		double val = ex.getValue(ex.getAttributes().get(attribute));

		return leftValue.contains(val);
	}

	/**
	 * Evaluates the condition on a specified dataset.
	 * @param set Input dataset.
	 * @param outIndices Output set of indices covered by the condition.
	 * For aciton always assume that condition evaluates, if left part of action is met for given example
	 */
	@Override
	protected void internalEvaluate(ExampleSet set, Set<Integer> outIndices) {
		Attribute atr = set.getAttributes().get(getAttribute());

		for (int id = 0; id < set.size(); ++id) {
			Example e = set.getExample(id);
			double v = e.getValue(atr);

			if (leftValue.contains(v)) {
				outIndices.add(id);
			}
		}
	}

}
