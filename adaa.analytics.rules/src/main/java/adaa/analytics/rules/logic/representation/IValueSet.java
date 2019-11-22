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

import java.util.List;

/**
 * Inteface to be implemented by all classes representing set of values (discrete set, interval, sum of sets, etc.).
 * @author Adam Gudys
 *
 */
public interface IValueSet {
	
	/**
	 * Checks whether the set contains a given value. If the value is missing (NaN), the behaviour depends on the missing value policy
	 * (see {@link #adaa.analytics.rules.logic.representation.MissingValuesHandler}).
	 * @param value Value to be checked.
	 * @return Test result.
	 */
	public boolean contains(double value);
	
	/**
	 * Checks if the value set intersects with another one.
	 * @param set Other value set.
	 * @return Test result.
	 */
	public boolean intersects(IValueSet set);
	
	/**
	 * Gets intersection of the value set with another one.
	 * @param set Other value set.
	 * @return Intersection of sets.
	 */
	public IValueSet getIntersection(IValueSet set);
	
	/**
	 * Get difference between the value set and another one.
	 * @param set Other value set.
	 * @return Difference of sets.
	 */
	public List<IValueSet> getDifference(IValueSet set);
	
	/**
	 * Checks if the value set equals to another one.
	 * @param obj Reference object.
	 * @return Test result.
	 */
	public boolean equals(Object obj);
	
	/**
	 * Calculates hashcode of the value set.
	 * @return Hashcode.
	 */
	public int hashCode();
	
	/**
	 * Converts the value set to a string.
	 * @return Text representation of the value set. 
	 */
	public String toString();
	
	
}
