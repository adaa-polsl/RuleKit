/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;

import java.util.HashSet;
import java.util.Set;

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
