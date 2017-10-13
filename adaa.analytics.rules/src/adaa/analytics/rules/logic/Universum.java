package adaa.analytics.rules.logic;

import java.io.Serializable;

public class Universum implements IValueSet, Serializable {

	private static final long serialVersionUID = -1067103164849784013L;

	@Override
	public boolean contains(double value) {
		return true;
	}

	@Override
	public boolean intersects(IValueSet set) {
		return true;
	}

	@Override
	public IValueSet getIntersection(IValueSet set) {
		return set;
	}
	
	@Override 
	public String toString() {
		return "Any";
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Universum);
	}
}
