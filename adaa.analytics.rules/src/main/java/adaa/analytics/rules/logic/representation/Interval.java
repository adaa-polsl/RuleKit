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

import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents continuous interval.
 * @author Adam Gudys
 *
 */
public class Interval implements IValueSet, Serializable {

	/** Serialization id */
	private static final long serialVersionUID = -5118225436243640157L;
	
	/** Constant representing negative infinity. */
	public static double MINUS_INF = -Double.MAX_VALUE;
	
	/** Constant representing positive infinity. */
	public static double INF = Double.MAX_VALUE;
	
	/** Left bound of the interval. */
	protected double left;
	
	/** Right bound of the interval. */
	protected double right;
	
	/** Flag indicating if the interval is closed from the left side. */
	protected boolean leftClosed;
	
	/** Flag indicating if the interval is closed from the right side. */
	protected boolean rightClosed;
	
	
	/** Gets {@link #left} */
	public double getLeft() { return left; }
	
	/** Gets {@link #right} */
	public double getRight() { return right; }
	
	
	/**
	 * Factory method which creates a right-bounded interval in the form (-inf, v).
	 * @param v Right interval side.
	 * @return Created interval.
	 */
	public static Interval create_le(double v) {
		return new Interval(MINUS_INF, v, false, false);
	}
	
	/**
	 * Factory method which creates a left-bounded interval in the form [v, +inf).
	 * @param v Left interval side.
	 * @return Created interval.
	 */
	public static Interval create_geq(double v) {
		return new Interval(v, INF, true, false);
	}
	
	/**
	 * Creates an unbounded interval (-inf,+inf).
	 */
	public Interval() {
		this.left = MINUS_INF;
		this.right = INF;
		this.leftClosed = false;
		this.rightClosed = false;
	}
	
	/**
	 * Initializes all members.
	 * @param left Left side.
	 * @param right Right side.
	 * @param leftClosed Flag indicating if interval is left-closed.  
	 * @param rightClosed Flag indicating if interval is right-closed.
	 */
	public Interval(double left, double right, boolean leftClosed, boolean rightClosed) {
		this.left = left;
		this.right = right;
		this.leftClosed = leftClosed;
		this.rightClosed = rightClosed;
	}
	
	/**
	 * Creates a half-bounded interval using value and relation in a text form.
	 * @param value Interval bound.
	 * @param relation One of the following: "&lt;", "&gt;", "&lt;=", "&gt;=".
	 */
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
	
	/**
	 * Checks if the value set equals to another one.
	 * @param obj Reference object.
	 * @return Test result.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		Interval ival = obj instanceof Interval ? (Interval)obj : null;
		
		if (ival == null)
			return false;
		
		return left == ival.left && right == ival.right && leftClosed == ival.leftClosed && rightClosed == ival.rightClosed;
	}
	

	/**
	 * Checks whether the interval contains a given value. If the value is missing (NaN), the behaviour depends on the missing value policy
	 * (see {@link adaa.analytics.rules.logic.representation.MissingValuesHandler}).
	 * @param value Value to be checked.
	 * @return Test result.
	 */
	@Override
	public boolean contains(double value) {		
		return ((value >= left && leftClosed) || value > left) && ((value <= right && rightClosed) || value < right) ||
				(Double.isNaN(value) && MissingValuesHandler.ignore);
	}
	
	/**
	 * Checks if the interval intersects with another one.
	 * @param set Other value set.
	 * @return Test result.
	 */
	@Override
	public boolean intersects(IValueSet set) {
		if (set instanceof AnyValueSet) {
			return true;
		}
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
	
	/**
	 * Gets intersection of the interval with another one.
	 * @param set Other value set.
	 * @return Intersection of sets.
	 */
	@Override
	public IValueSet getIntersection(IValueSet set) {
		if (set instanceof AnyValueSet) {
			return this;
		}
		if (set instanceof Interval) {
			Interval other = (Interval)set;
			if (!this.intersects(other)) {
				return null;
			}
			return new Interval(
				Math.max(this.left, other.left),
				Math.min(this.right, other.right),
				this.left < other.left ? other.leftClosed : this.leftClosed,
				this.right > other.right ? other.rightClosed : this.rightClosed);
		} else {
			return null;
		}
	}
	
	/**
	 * Converts the value set to a string.
	 * @return Text representation of the value set. 
	 */
	@Override
	public String toString() {
		String s =
				(leftClosed ? "<" : "(")+
				((left == MINUS_INF) ? "-inf" : DoubleFormatter.format(left)) + ", " +
				((right == INF) ? "inf" : DoubleFormatter.format(right)) +
				(rightClosed ? ">" : ")");
		
		return s;	
	}

	/**
	 * Get difference between the value set and another one.
	 * @param set Other value set.
	 * @return Difference of sets.
	 */
	@Override
	public List<IValueSet> getDifference(IValueSet set) {
		if (set instanceof AnyValueSet) {
			return null;
		}
		Interval ival = set instanceof Interval ? (Interval)set : null;
		List<IValueSet> ret = new ArrayList<IValueSet>();
		
		if ( ival != null && this.intersects(ival) ) {
			Interval intersection = (Interval) this.getIntersection(set);
			
			if (intersection.left > this.left && intersection.right < this.right) {
				ret.add(new Interval(this.left, intersection.left, this.leftClosed, true));
				ret.add(new Interval(intersection.right, this.right, true, this.rightClosed));
			} else if (this.left == intersection.left) {
				ret.add(new Interval(intersection.right, this.right, true, this.rightClosed));
			} else if (this.right == intersection.right) {
				ret.add(new Interval(this.left, intersection.left, this.leftClosed, true));
			}
			
		} else {
			ret.add(this);
		}
		return ret;
	}

	/**
	 * Calculates hashcode of the value set.
	 * @return Hashcode.
	 */
	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder(17, 37);
		builder.append(leftClosed).append(rightClosed).append(left).append(right);
		return builder.toHashCode();
	}

	
}
