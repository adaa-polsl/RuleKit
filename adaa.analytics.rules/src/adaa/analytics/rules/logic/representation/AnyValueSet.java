package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.List;

public class AnyValueSet implements IValueSet {

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
		return "ANY";
	}

	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		List<IValueSet> ret = new ArrayList<IValueSet>();
		return ret; //or throw ?
	}

}
