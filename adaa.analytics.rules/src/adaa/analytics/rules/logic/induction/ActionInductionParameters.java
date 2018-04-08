package adaa.analytics.rules.logic.induction;

import java.util.Optional;

public class ActionInductionParameters extends InductionParameters {

	protected Optional<Integer> sourceClassId;
	protected Optional<Integer> targetClassId;
	
	public ActionInductionParameters() {
		// TODO Auto-generated constructor stub
	}
	
	public Optional<Integer> getSourceClassId() {
		return sourceClassId;
	}
	
	protected void setSourceClassId(int id) {
		sourceClassId = Optional.of(id);
	}
	
	public Optional<Integer> getTargetClassId() {
		return targetClassId;
	}
	
	protected void setTargetClassId(int id) {
		targetClassId = Optional.of(id);
	}
	
	public void setGenerateAllTransitions() {
		targetClassId = Optional.empty();
		sourceClassId = Optional.empty();
	}
	
	public boolean getGenerateAllTransitions() {
		return !targetClassId.isPresent() && !sourceClassId.isPresent();
	}
	
	public void setClasswiseTransition(int source, int target) {
		this.setSourceClassId(source);
		this.setTargetClassId(target);
	}
	
	public void setFromSourceClassToAny(int source) {
		this.setSourceClassId(source);
	}
	
	public void setFromAnyToTargetClass(int target) {
		this.setTargetClassId(target);
	}
}
