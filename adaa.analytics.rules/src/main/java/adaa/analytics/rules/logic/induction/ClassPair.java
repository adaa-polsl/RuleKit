package adaa.analytics.rules.logic.induction;

import com.rapidminer.example.table.NominalMapping;

public class ClassPair {

	protected String sourceLabel;
	protected String targetLabel;
	protected int sourceId;
	protected int targetId;
	
	public ClassPair(String source, String target, NominalMapping mapping) {
		sourceLabel = source;
		targetLabel = target;
		sourceId = mapping.getIndex(source);
		targetId = mapping.getIndex(target);
	}
	
	public String getSourceLabel() {
		return sourceLabel;
	}
	
	public String getTargetLabel() {
		return targetLabel;
	}

	public int getSourceId() {
		return sourceId;
	}
	
	public int getTargetId() {
		return targetId;
	}
}
