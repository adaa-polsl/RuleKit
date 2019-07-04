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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.io.Serializable;
import java.util.Set;

/**
 * Interface to be implemented by all conditions.
 * @author Adam
 *
 */
public abstract class ConditionBase implements Cloneable, Serializable {
	
	public enum Type {FORCED, PREFERRED, NORMAL};
	
	private static final long serialVersionUID = 6510660043726970892L;

	public boolean isDisabled() { return disabled; }
	public void setDisabled(boolean b) { disabled = b; }
	
	public Type getType() { return type; }
	public void setType(Type t) { type = t; }
	
	public boolean isPrunable() { return (type != Type.FORCED) && (type != Type.PREFERRED); }

	/**
	 * Checks if condition is fulfilled for given example.
	 * @param ex An example to be checked.
	 * @return Test result.
	 */
	public boolean evaluate(Example ex) {
		return disabled ? true : internalEvaluate(ex);
	}
	
	/**
	 * Evaluates condition on the specified dataset.
	 * @param set Input dataset.
	 * @param outIndices Output set of indices covered by the condition.
	 */
	public void evaluate(ExampleSet set, Set<Integer> outIndices) {
		if (!disabled) 
			internalEvaluate(set, outIndices);
	}
	
	
	public abstract boolean equals(Object ref);
	
	public abstract Set<String> getAttributes();
	
	protected abstract boolean internalEvaluate(Example ex);
	
	protected abstract void internalEvaluate(ExampleSet set,  Set<Integer> outIndices);
	
	protected boolean disabled = false;
	protected Type type = Type.NORMAL;
}
