package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.actions.ActionInductionRangeStrategy;
import adaa.analytics.rules.logic.actions.DefaultActionInductionRangeStrategy;
import adaa.analytics.rules.logic.actions.ExclusiveOnlyActionInductionRangeStrategy;
import adaa.analytics.rules.logic.actions.NotIntersectingActionInductionRangeStrategy;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import com.rapidminer.example.ExampleSet;

public class ActionFindingParameters {
	
	public enum RangeUsageStrategy 
	{
		ALL,
		NOT_INTERSECTING,
		EXCLUSIVE_ONLY
	}
	
	private RangeUsageStrategy rangeStrategy;
	
	public RangeUsageStrategy getUseNotIntersectingRangesOnly() {
		return this.rangeStrategy;
	}
	
	public ActionInductionRangeStrategy getRangeStrategy(ElementaryCondition pattern, ExampleSet dataset) {
		switch(rangeStrategy) {
		case ALL: return new DefaultActionInductionRangeStrategy(pattern, dataset);
		case EXCLUSIVE_ONLY: return new ExclusiveOnlyActionInductionRangeStrategy(pattern, dataset);
		case NOT_INTERSECTING: return new NotIntersectingActionInductionRangeStrategy(pattern, dataset);
		default: throw new RuntimeException("Unknown range strategy");
		}
	}
	
	public void setUseNotIntersectingRangesOnly(RangeUsageStrategy value) {
		this.rangeStrategy = value;
	}

}
