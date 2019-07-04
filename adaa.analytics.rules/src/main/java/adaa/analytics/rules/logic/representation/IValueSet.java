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
 * @author Adam
 *
 */
public interface IValueSet {
	
	/**
	 * Checks whether set contains given value.
	 * @param value value to be checked.
	 * @return flag indicating whether value is contained in a set.
	 */
	public boolean contains(double value);
	
	public boolean intersects(IValueSet set);
	
	public IValueSet getIntersection(IValueSet set);
	
	public List<IValueSet> getDifference(IValueSet set);
	
	
	public boolean equals(Object obj);
	public int hashCode();
	
	public String toString();
	
	
}
