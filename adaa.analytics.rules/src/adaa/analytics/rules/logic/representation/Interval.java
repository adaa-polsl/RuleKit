package adaa.analytics.rules.logic.representation;

import java.io.Serializable;

/**
 * Represents continuous interval.
 * @author Adam
 *
 */
public class Interval implements IValueSet, Serializable {

	private static final long serialVersionUID = -5118225436243640157L;
	
	public static double MINUS_INF = -Double.MAX_VALUE;
	public static double INF = Double.MAX_VALUE;
	
	protected double left;
	protected double right;
	
	protected boolean leftClosed;
	protected boolean rightClosed;
	
	public static Interval create_le(double v) {
		return new Interval(MINUS_INF, v, false, false);
	}
	
	public static Interval create_geq(double v) {
		return new Interval(v, INF, true, false);
	}
	
	public Interval() {
		this.left = MINUS_INF;
		this.right = INF;
		this.leftClosed = false;
		this.rightClosed = false;
	}
	
	public Interval(double left, double right, boolean leftClosed, boolean rightClosed) {
		this.left = left;
		this.right = right;
		this.leftClosed = leftClosed;
		this.rightClosed = rightClosed;
	}
	
	public Interval(double value, String relation) {
		this();
		if (relation.equals("<")) {
			right = value;
		} else if (relation.equals(">")) {
			left = value;
		} else if (relation.equals("<=")) {
			right = value;
			rightClosed = true;
		} else if (relation.equals(">=")) {
			left = value;
			leftClosed = true;
		}
	}
	
	@Override
	public boolean contains(double value) {		
		return ((value >= left && leftClosed) || value > left) && ((value <= right && rightClosed) || value < right) ||
				(Double.isNaN(value) && MissingValuesHandler.ignore);
	}
	
	@Override
	public boolean intersects(IValueSet set) {
		Interval ds = (set instanceof Interval) ? (Interval)set : null;
		if (ds != null) {
			if (this.right < ds.left || (this.right == ds.left && !ds.leftClosed) || 
				this.left > ds.right || (this.left == ds.right && !ds.rightClosed)) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public IValueSet getIntersection(IValueSet set) {
		if (set instanceof Interval) {
			Interval other = (Interval)set;
			return new Interval(
				Math.max(this.left, other.left),
				Math.min(this.right, other.right),
				this.left < other.left ? other.leftClosed : this.rightClosed,	
				this.right > other.right ? other.rightClosed : this.rightClosed);
		} else {
			return null;
		}
	}
	
	@Override
	public String toString() {
		String s =
				(leftClosed ? "<" : "(")+
				((left == MINUS_INF) ? "-inf" : Double.toString(left)) + ", " + 
				((right == INF) ? "inf" : Double.toString(right)) +
				(rightClosed ? ">" : ")");
		
		return s;	
	}

}
