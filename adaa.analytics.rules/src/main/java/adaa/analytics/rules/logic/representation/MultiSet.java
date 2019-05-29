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
import java.util.Map;


public class MultiSet<T> implements Iterable<T>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4349403675429533526L;
	protected Map<T, Integer> map = new HashMap<T, Integer>();
	
	public boolean contains(Object v) { return map.containsKey(v); }
	public boolean add(T v) {
		return add(v, 1);
	}
	
	public boolean add(T v, int count) {
		int current = map.containsKey(v) ? map.get(v) : 0;
		map.put(v, current + count);
		return true;
	}
	
	public int getCount(T v) {
		return map.containsKey(v) ? map.get(v) : 0;
	}
	
	public void remove(Object v) {
		int count = map.containsKey(v) ? map.get(v) : 0;
		if (count > 1)  {
			map.put((T)v, count - 1);
		} else {
			map.remove(v);
		}
	}
	
	public int size() { return map.keySet().size(); }
	
	public Iterator<T> iterator() { return map.keySet().iterator(); }
}
