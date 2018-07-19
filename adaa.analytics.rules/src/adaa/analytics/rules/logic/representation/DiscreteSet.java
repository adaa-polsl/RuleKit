package adaa.analytics.rules.logic.representation;

import java.io.Serializable;
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

	
}
