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

import adaa.analytics.rules.data.DataColumnDoubleAdapter;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an elementary condition (built upon single attribute and value set).
 * @author Adam Gudys
 *
 */
public class ElementaryCondition extends ConditionBase {

	/** Serialization id. */
	private static final long serialVersionUID = 8030800833578235852L;
	
	/** Attribute the condition is built upon. */
	protected String attribute;
	
	/** Value set. */
	protected IValueSet valueSet;
	
	/** Flag indicating if condition (in particular, the value set) is adjustable. */
	protected boolean adjustable = false;
	
	/** Gets {@link #attribute}. */
	public String getAttribute() { return attribute; }
	
	/** Gets {@link #valueSet}. */
	public IValueSet getValueSet() { return valueSet; }
	/** Sets {@link #valueSet}. */
	public void setValueSet(IValueSet vs) { this.valueSet = vs; }
	
	/** Gets {@link #adjustable}. */
	public boolean isAdjustable() { return adjustable; }
	/** Sets {@link #adjustable}. */
	public void setAdjustable(boolean b) { adjustable = b; }	
	
	/** Creates empty condition. */
	protected ElementaryCondition() {
		super();
	}
	
	/**
	 * Initializes members.
	 * @param attribute Attribute.
	 * @param valueSet Value set.
	 */
	public ElementaryCondition(String attribute, IValueSet valueSet) {
		this.attribute = attribute;
		this.valueSet = valueSet;
	}

	/**
	 * Evaluates the condition on a given example. 
	 * @param ex Example to be examined.
	 * @return Logical value indicating whether the example fulfills the condition.
	 */
	@Override
	protected boolean internalEvaluate(Example ex) {
		double v = ex.getValue(ex.getAttributes().get(attribute));
		boolean result = valueSet.contains(v);
		return result;
	}
	
	/**
	 * Evaluates the condition on a specified dataset.
	 * @param set Input dataset.
	 * @param outIndices Output set of indices covered by the condition.
	 */
	@Override
	protected void internalEvaluate(IExampleSet set, Set<Integer> outIndices) {

		IAttribute a = set.getAttributes().get(attribute);

		/* The following code does not work for SplittedExampleSet
		ExampleTable tab = set.getExampleTable();
		DataRowReader drr = tab.getDataRowReader();

		int id = 0; 
		while (drr.hasNext()) {
			DataRow dr = drr.next();
			
			double v = dr.get(a);
			if (valueSet.contains(v)) {
				outIndices.add(id);
			}
			++id;
		}*/
		DataColumnDoubleAdapter dataColumnDoubleAdapter = set.getDataColumnDoubleAdapter(a,Double.NaN);
		for (int id = 0; id < set.size(); ++id) {
//			Example e = set.getExample(id);
//			double v = e.getValue(a);
			double v = dataColumnDoubleAdapter.getDoubleValue(id);
			if (valueSet.contains(v)) {
				outIndices.add(id);
			}
		}
	}
	
	/**
	 * Generates a text representation of the condition.
	 * @return Text representation.
	 */
	public String toString() {
		
		String s = attribute + (adjustable ? " @= " : " = ") + valueSet.toString();	
		if (type == Type.FORCED) {
			s = "[[" + s + "]]";
		} else if (type == Type.PREFERRED) {
			s = "[" + s + "]";
		}
		return s;
	}
	
	/**
	 * Verifies whether the condition is equal to another one.
	 * @param obj Reference object.
	 * @return Logical value indicating whether conditions are equal. 
	 */
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
	
	/**
	 * Gets intersection with another elementary condition. 
	 * @param other Another condition.
	 * @return New elementary condition.
	 */
	public ElementaryCondition intersect(ElementaryCondition other) {
		return new ElementaryCondition(attribute, this.valueSet.getIntersection(other.getValueSet())); 
	}

	/**
	 * Calculates object hash code.
	 * @return Hash code.
	 */
	@Override
	public int hashCode() {
		int result = attribute.hashCode();
		result = 31 * result + valueSet.hashCode();
		return result;
	}
	
	/**
	 * Gets a collection of attributes the condition is built upon.
	 * @return Set of attributes.
	 */
	@Override
	public Set<String> getAttributes() {
		Set<String> attrs = new HashSet<String>();
		attrs.add(attribute);
		return attrs;
	}

	@Override
	public ConditionBase clone() {
		ElementaryCondition out = new ElementaryCondition(this.attribute, this.valueSet);
		return out;
	}
}
