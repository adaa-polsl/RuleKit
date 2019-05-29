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
 * Represents discrete set of values.
 * @author Adam
 *
 */
class DiscreteSet implements IValueSet, Serializable {

	private static final long serialVersionUID = -5679534406109859941L;
	
	protected Set<Double> values = new HashSet<Double>();
	protected List<String> mapping;
	
	public Set<Double> getValues() { return values; }
	
	public DiscreteSet() {
		super();
	}
		
	@Override
	public boolean contains(double value) {
		return values.contains(value) || (Double.isNaN(value) && MissingValuesHandler.ignore);
	}
	
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
