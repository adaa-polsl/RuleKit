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
package adaa.analytics.rules.logic.representation.valueset;

import adaa.analytics.rules.utils.DoubleFormatter;
import adaa.analytics.rules.logic.representation.MissingValuesHandler;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * Value set containing one element. 
 * @author Adam Gudys
 *
 */
public class SingletonSet implements IValueSet, Serializable {

	/** Serialization identifier */
	private static final long serialVersionUID = -1567922506451323157L;
	
	/** Value stored in a set */
	protected double value;
	
	/** Mapping from value to label (null if no label exist). */
	protected List<String> mapping;
	
	
	/** Gets {@link #value} */
	public double getValue() { return value; }
	/** Sets {@link #value} */
	public void setValue(double v) { value = v; }

	/** Gets {@link #value} as string */
	public String getValueAsString() { return mapping.get((int)value); }
	
	/** Gets {@link #mapping} */
	public List<String> getMapping() { return mapping; }
	/** Sets {@link #mapping} */
	public void setMapping(List<String> v) { mapping = v; }
	
	/**
	 * Initializes members with arguments.
	 * @param v Singleton value.
	 * @param mapping Mapping from value to label (can be null).
	 */
	public SingletonSet(double v, List<String> mapping) {
		this.value = v;
		this.mapping = mapping;
	}
	
	/**
	 * Checks whether the set contains a given value. If the value is missing (NaN), the behaviour depends on the missing value policy
	 * (see {@link adaa.analytics.rules.logic.representation.MissingValuesHandler}).
	 * @param value Value to be checked.
	 * @return Test result.
	 */
	@Override
	public boolean contains(double value) {
		return (value == this.value) || (Double.isNaN(value) && MissingValuesHandler.ignore);
	}

	/**
	 * Checks if the value set intersects with another one.
	 * @param set Other value set.
	 * @return Test result.
	 */
	@Override
	public boolean intersects(IValueSet set) {
		SingletonSet ss = (set instanceof SingletonSet) ? (SingletonSet)set : null;
		if (ss != null) {
			return this.value == ss.value;
		}
		return false;
	}
	
	/**
	 * Checks if the value set equals to other one.
	 * @param obj Object co cmopare with.
	 * @return Test result.
	 */
	@Override
	public boolean equals(Object obj) {
		SingletonSet ref = (obj instanceof SingletonSet) ? (SingletonSet)obj : null;
		
		if (ref != null) {
			EqualsBuilder builder = new EqualsBuilder();
			builder.append(value,  ref.value);
			builder.append(mapping, ref.mapping);
			return builder.isEquals();
		} else {
			return false;
		}
	}

	/**
	 * Converts the value set to string.
	 * @return Text representation of the value set. 
	 */
	@Override
	public String toString() {
		String s = "{" + ((mapping == null) ? DoubleFormatter.format(value) : mapping.get((int)value)) + "}";
		return s;
	}
	
	/**
	 * Gets intersection of the value set with another one.
	 * @param set Other value set.
	 * @return Intersection of sets.
	 */
	@Override
	public IValueSet getIntersection(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * Calculates hashcode of the value set.
	 * @return Hashcode.
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(19,27);
		builder.append(value).append(mapping);
		return builder.toHashCode();
	}
	
	/**
	 * Get difference between current value set and another one.
	 * @param set Other value set.
	 * @return Difference of sets.
	 */
	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}
}
