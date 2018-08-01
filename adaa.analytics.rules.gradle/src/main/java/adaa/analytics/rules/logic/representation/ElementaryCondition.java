package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;

public class ElementaryCondition extends ConditionBase {

	private static final long serialVersionUID = 8030800833578235852L;
	
	protected String attribute;
	protected IValueSet valueSet;
	protected boolean adjustable = false;
	
	public String getAttribute() { return attribute; }
	public IValueSet getValueSet() { return valueSet; }
	
	public boolean isAdjustable() { return adjustable; }
	public void setAdjustable(boolean b) { adjustable = b; }	
	
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
	
	
	protected void internalEvaluate(ExampleSet set,  Set<Integer> out) {
		ExampleTable tab = set.getExampleTable();
		DataRowReader drr = tab.getDataRowReader();
		
		Attribute a = set.getAttributes().get(attribute);
		
		int id = 0; 
		while (drr.hasNext()) {
			DataRow dr = drr.next();
			
			double v = dr.get(a);
			if (valueSet.contains(v)) {
				out.add(id);
			}
			++id;
		}
	}
	
	
	public String toString() {
		
		String s = attribute + (adjustable ? " @= " : " = ") + valueSet.toString();	
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
				return this.valueSet.equals(ref.getValueSet()) && this.attribute.equals(ref.getAttribute()); 
			} else {
				return false;
			}
		}
	}
	
	public ElementaryCondition intersect(ElementaryCondition other) {
		return new ElementaryCondition(attribute, this.valueSet.getIntersection(other.getValueSet())); 
	}

	
	@Override
	public int hashCode() {
		int result = attribute.hashCode();
		result = 31 * result + valueSet.hashCode();
		return result;
	}
	
	@Override
	public Set<String> getAttributes() {
		Set<String> attrs = new HashSet<String>();
		attrs.add(attribute);
		return attrs;
	}

}
