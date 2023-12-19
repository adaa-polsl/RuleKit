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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Abstract base class representing all conditions.
 * @author Adam Gudys
 *
 */
public abstract class ConditionBase implements Cloneable, Serializable {
	
	/**
	 * Represents condition type:
	 * <p><ul>
	 * <li>FORCED - must appear in the rule (part of user's rule)
	 * <li>PREFERRED - preferred,
	 * <li>NORMAL - automatically induced. 
	 * </ul>
	 * @author Adam Gudys
	 *
	 */
	public enum Type {FORCED, PREFERRED, NORMAL};
	
	/** Serialization identifier. */
	private static final long serialVersionUID = 6510660043726970892L;
	
	/** Flag indicating if condition is disabled. */
	protected boolean disabled = false;
	
	/** Condition type. */
	protected Type type = Type.NORMAL;

	/** Optional integer bit set for storing condition coverage. */
	protected IntegerBitSet covering = null;

	/** Gets {@link #disabled} */
	public boolean isDisabled() { return disabled; }
	/** Sets {@link #disabled} */
	public void setDisabled(boolean b) { disabled = b; }
	
	/** Gets {@link #type} */
	public Type getType() { return type; }
	/** Sets {@link #type} */
	public void setType(Type t) { type = t; }

	/** Gets {@link #covering}. */
	public IntegerBitSet getCovering() { return covering; }
	/** Sets {@link #covering}. */
	public void setCovering(IntegerBitSet c) { covering = c; }

	/**
	 * Check whether the condition is prunable (non-FORCED and non-PREFERRED). 
	 * @return Value indicating whether condition is prunable.
	 */
	public boolean isPrunable() { return (type != Type.FORCED) && (type != Type.PREFERRED); }

	/**
	 * Evaluates the condition on a given example. 
	 * It verifies {@link #disabled} flag and calls {@link #internalEvaluate} abstract method.
	 * @param ex Example to be examined.
	 * @return Logical value indicating whether the example fulfills the condition.
	 */
	public boolean evaluate(Example ex) {
		return disabled ? true : internalEvaluate(ex);
	}
	
	/**
	 * Evaluates the condition on a specified dataset. 
	 * It verifies {@link #disabled} flag and calls {@link #internalEvaluate} abstract method.
	 * @param set Input dataset.
	 * @param outIndices Output set of indices covered by the condition.
	 */
	public void evaluate(ExampleSet set, Set<Integer> outIndices) {
		if (!disabled) 
			internalEvaluate(set, outIndices);
	}
	
	/**
	 * Verifies whether the condition is equal to another one.
	 * @param ref Reference object.
	 * @return Logical value indicating whether conditions are equal. 
	 */
	public abstract boolean equals(Object ref);
	
	/**
	 * Gets a collection of attributes the condition is built upon.
	 * @return Set of attributes.
	 */
	public abstract Set<String> getAttributes();
	
	/**
	 * Evaluates the condition on a given example. 
	 * @param ex Example to be examined.
	 * @return Logical value indicating whether the example fulfills the condition.
	 */
	protected abstract boolean internalEvaluate(Example ex);
	
	/**
	 * Evaluates the condition on a specified dataset.
	 * @param set Input dataset.
	 * @param outIndices Output set of indices covered by the condition.
	 */
	protected abstract void internalEvaluate(ExampleSet set,  Set<Integer> outIndices);

	public abstract ConditionBase clone();
}
