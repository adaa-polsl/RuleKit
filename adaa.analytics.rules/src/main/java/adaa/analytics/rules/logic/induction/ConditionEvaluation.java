package adaa.analytics.rules.logic.induction;
import adaa.analytics.rules.logic.representation.ConditionBase;

/**
 * Helper class for storing information about evaluated condition. 
 * @author Adam
 *
 */
public class ConditionEvaluation {
	public ConditionBase condition = null;
	public Covering covering = null;
	public double quality = -Double.MAX_VALUE;
	public double covered = 0;
}
