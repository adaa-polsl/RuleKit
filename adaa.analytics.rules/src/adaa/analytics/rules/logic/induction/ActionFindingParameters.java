package adaa.analytics.rules.logic.induction;

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
	
	public void setUseNotIntersectingRangesOnly(RangeUsageStrategy value) {
		this.rangeStrategy = value;
	}

}
