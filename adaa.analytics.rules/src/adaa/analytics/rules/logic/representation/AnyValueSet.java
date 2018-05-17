package adaa.analytics.rules.logic.representation;

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

}
