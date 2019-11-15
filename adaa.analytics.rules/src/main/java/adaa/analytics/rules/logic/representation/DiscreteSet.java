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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a discrete set of values.
 * @author Adam Gudys
 *
 */
class DiscreteSet implements IValueSet, Serializable {

	/** Serialization identifier. */
	private static final long serialVersionUID = -5679534406109859941L;
	
	/** Collection of values contained in a set. */
	protected Set<Double> values = new HashSet<Double>();
	
	/** Mapping from integer representation to label. */
	protected List<String> mapping;
	
	/** Gets {@link #values} */
	public Set<Double> getValues() { return values; }
	
	/**
	 * Creates empty set.
	 */
	public DiscreteSet() {
		super();
	}
	
	/**
	 * Checks whether the set contains a given value. If the value is missing (NaN), the behaviour depends on the missing value policy
	 * (see {@link #adaa.analytics.rules.logic.representation.MissingValuesHandler}).
	 * @param value Value to be checked.
	 * @return Test result.
	 */
	@Override
	public boolean contains(double value) {
		return values.contains(value) || (Double.isNaN(value) && MissingValuesHandler.ignore);
	}
	
	/**
	 * Checks if the value set intersects with another one.
	 * @param set Other value set.
	 * @return Test result.
	 */
	@Override
	public boolean intersects(IValueSet set) {
		DiscreteSet ds = (set instanceof DiscreteSet) ? (DiscreteSet)set : null;
		if (set instanceof AnyValueSet)
			return true;
		if (ds != null) {
			return this.values.containsAll(ds.getValues());
		}
		return false;
	}
	
	/**
	 * Checks if the value set equals to other one.
	 * @param obj Object co cmopare with.
	 * @return Test result.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) {
			return true;
		} else {
			DiscreteSet ref = (obj instanceof DiscreteSet) ? (DiscreteSet)obj : null;
			if (ref != null) {
				return values.equals(ref.values);
			} else {
				return false;
			}
		}
	}
	
	/**
	 * Converts the value set to string.
	 * @return Text representation of the value set. 
	 */
	@Override
	public String toString() {
	
		String s = "{";
		for (double v : values) {
			s += mapping.get((int)v) + ", ";
		}
		s = s.substring(0, s.length() - 2);
		s += "}";
		return s;
	}

	/**
	 * Gets intersection of the value set with another one.
	 * @param set Other value set.
	 * @return Intersection of sets.
	 */
	@Override
	public IValueSet getIntersection(IValueSet set) {
		if (!(set instanceof DiscreteSet)) {
			return null;
		}
		DiscreteSet ds = (DiscreteSet)set;
		DiscreteSet ret = new DiscreteSet();
		ret.values = new HashSet<Double>(this.values);
		ret.values.retainAll(ds.getValues());
		return ret;
	}

	/**
	 * Get difference between current value set and another one.
	 * @param set Other value set.
	 * @return Difference of sets.
	 */
	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		DiscreteSet ds = (set instanceof DiscreteSet) ? (DiscreteSet)set : null;
		List<IValueSet> ret = new ArrayList<IValueSet>(1);
			
		if (!this.intersects(set) || set instanceof AnyValueSet) {
			ret.add(this);
		} else {
			DiscreteSet s = new DiscreteSet();
			s.values = new HashSet<Double>(this.values);
			s.values.removeAll(ds.values);
			ret.add(s);
		}
		return ret;
	}

	
}
