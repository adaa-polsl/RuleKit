package adaa.analytics.rules.logic.induction;

public class ClassIdPair {

	protected int sourceId;
	protected int targetId;
	
	public ClassIdPair(int source, int target) {
		sourceId = source;
		targetId = target;
	}
	
	public int getSourceId() {
		return sourceId;
	}
	
	public int getTargetId() {
		return targetId;
	}

}
