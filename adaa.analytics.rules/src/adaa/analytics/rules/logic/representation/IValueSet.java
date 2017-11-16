package adaa.analytics.rules.logic.representation;

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
	
	public boolean equals(Object obj);
	
	public String toString();
	
	
}
