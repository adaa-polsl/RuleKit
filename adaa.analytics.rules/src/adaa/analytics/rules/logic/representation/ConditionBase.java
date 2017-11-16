package adaa.analytics.rules.logic.representation;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.rapidminer.example.Example;

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
	
	public abstract boolean equals(Object ref);
	
	public abstract Set<String> getAttributes();
	
	protected abstract boolean internalEvaluate(Example ex);
	
	protected boolean disabled = false;
	protected Type type = Type.NORMAL;
}
