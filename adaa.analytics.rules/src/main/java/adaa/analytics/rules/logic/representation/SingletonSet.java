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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.List;

public class SingletonSet implements IValueSet, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1567922506451323157L;
	protected double value;
	protected List<String> mapping;
	
	public double getValue() { return value; }
	public void setValue(double v) { value = v; }
	
	public List<String> getMapping() { return mapping; }
	public void setMapping(List<String> v) { mapping = v; }
	
	public SingletonSet(double v, List<String> mapping) {
		this.value = v;
		this.mapping = mapping;
	}
	
	@Override
	public boolean contains(double value) {
		return (value == this.value) || (Double.isNaN(value) && MissingValuesHandler.ignore);
	}

	@Override
	public boolean intersects(IValueSet set) {
		SingletonSet ss = (set instanceof SingletonSet) ? (SingletonSet)set : null;
		if (ss != null) {
			return this.value == ss.value;
		}
		return false;
	}
	
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

	@Override
	public String toString() {
		String s = "{" + ((mapping == null) ? value : mapping.get((int)value)) + "}";
		return s;
	}
	
	@Override
	public IValueSet getIntersection(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(19,27);
		builder.append(value).append(mapping);
		return builder.toHashCode();
	}
	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		// TODO Auto-generated method stub
		return null;
	}
}
