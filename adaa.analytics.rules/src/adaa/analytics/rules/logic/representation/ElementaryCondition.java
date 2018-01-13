package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Example;

public class ElementaryCondition extends ConditionBase {

	private static final long serialVersionUID = 8030800833578235852L;
	
	protected String attribute;
	protected IValueSet valueSet;
	
	public String getAttribute() { return attribute; }
	public IValueSet getValueSet() { return valueSet; }
	
	protected ElementaryCondition() {
		;
	}
	
	public ElementaryCondition(String attribute, IValueSet valueSet) {
		this.attribute = attribute;
		this.valueSet = valueSet;
	}

	@Override
	protected boolean internalEvaluate(Example ex) {
		double v = ex.getValue(ex.getAttributes().get(attribute));
		boolean result = valueSet.contains(v);
		return result;
	}
	
	
	public String toString() {
		String s = attribute + " = " + valueSet.toString();
		if (type == Type.FORCED) {
			s = "[[" + s + "]]";
		} else if (type == Type.PREFERRED) {
			s = "[" + s + "]";
		}
		return s;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else {
			ElementaryCondition ref = (obj instanceof ElementaryCondition) ? (ElementaryCondition)obj : null;
			if (ref != null) {
				return this.valueSet.equals(ref) && this.attribute.equals(ref.getAttribute()); 
			} else {
				return false;
			}
		}
	}
	
	public ElementaryCondition intersect(ElementaryCondition other) {
		return new ElementaryCondition(attribute, this.valueSet.getIntersection(other.getValueSet())); 
	}
	@Override
	public Set<String> getAttributes() {
		Set<String> attrs = new HashSet<String>();
		attrs.add(attribute);
		return attrs;
	}

}
