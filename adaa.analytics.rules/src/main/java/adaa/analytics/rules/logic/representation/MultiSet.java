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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic class representing a multiset.
 * @author Adam Gudys
 *
 * @param <T> Type of elements stored in a set.
 */
public class MultiSet<T> implements Iterable<T>, Serializable {
	/** Serialization identifier */
	private static final long serialVersionUID = 4349403675429533526L;
	
	/** Collection storing elements with their multiplicity */
	protected Map<T, Integer> map = new LinkedHashMap<>();
	
	/**
	 * Verifies the multiset contains a given element.
	 * @param v Elements to be verified.
	 * @return Verification result.
	 */
	public boolean contains(Object v) { return map.containsKey(v); }
	
	/**
	 * Adds an element to the multiset (increments multiplicity by 1).
	 * @param v Element to be added.
	 * @return Should be ignored - always true.
	 */
	public boolean add(T v) {
		return add(v, 1);
	}
	
	/**
	 * Adds an element to the multiset given number of times (increments multiplicity by this number).
	 * @param v Element to be added. 
	 * @param count Multiplicity of the element being added.
	 * @return Should be ignored - always true.
	 */
	public boolean add(T v, int count) {
		int current = map.containsKey(v) ? map.get(v) : 0;
		map.put(v, current + count);
		return true;
	}
	
	/**
	 * Gets multiplicity of a specified element.
	 * @param v Element to be found in the set.
	 * @return Multiplicity of the element (0 if it doesn't exist).
	 */
	public int getCount(T v) {
		return map.containsKey(v) ? map.get(v) : 0;
	}
	
	/**
	 * Removes element from the multiset.
	 * @param v Elements to be removed.
	 */
	public void remove(Object v) {
		int count = map.containsKey(v) ? map.get(v) : 0;
		if (count > 1)  {
			map.put((T)v, count - 1);
		} else {
			map.remove(v);
		}
	}
	
	/**
	 * Gets size of the multiset.
	 * @return Number of unique elements in the multiset.
	 */
	public int size() { return map.keySet().size(); }
	
	/**
	 * Multiset iterator.
	 * @return Iterator to unique multiset elements.
	 */
	public Iterator<T> iterator() { return map.keySet().iterator(); }
}
