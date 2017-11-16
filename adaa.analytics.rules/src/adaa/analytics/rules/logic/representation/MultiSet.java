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
