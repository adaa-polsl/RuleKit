package adaa.analytics.rules.logic.induction;

import com.rapidminer.example.table.NominalMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ActionInductionParameters extends InductionParameters {

	protected Map<String,String> transitions = new HashMap<String,String>();
	protected ActionFindingParameters findingParams;
	protected List<String> stableAttributes = new ArrayList<String>();
	public static String ALL_CLASSES_LABEL = "*";
	
	public ActionInductionParameters(ActionFindingParameters params) {
		findingParams = params;
	}

	public ActionInductionParameters(ActionInductionParameters rhs) {
		transitions = new HashMap<String, String>();
		transitions.putAll(rhs.transitions);
		findingParams = rhs.findingParams;
		stableAttributes = new ArrayList<String>(rhs.stableAttributes.size());
		stableAttributes.addAll(rhs.stableAttributes);
		setMinimumCovered(rhs.getMinimumCovered());
		setVotingMeasure(rhs.getVotingMeasure());
		setMinimumCovered(rhs.getMinimumCovered());
		setEnablePruning(rhs.isPruningEnabled());
		setIgnoreMissing(rhs.isIgnoreMissing());
		setPruningMeasure(rhs.getPruningMeasure());
		setInductionMeasure(rhs.getInductionMeasure());
		setMaxGrowingConditions(rhs.getMaxGrowingConditions());
		setMaximumUncoveredFraction(rhs.getMaximumUncoveredFraction());
	}
	
	public ActionFindingParameters getActionFindingParameters() {
		return findingParams;
	}
	
	public void setGenerateAllTransitions() {		
		transitions.clear();
		transitions.put(ALL_CLASSES_LABEL, ALL_CLASSES_LABEL);
	}

	public void setStableAttributes(List<String> stableAttributes_) {
		this.stableAttributes = stableAttributes_;
	}
	public List<String> getStableAttributes(){ return this.stableAttributes;}

	public void reverseTransitions() {
		Map<String, String> newTransitions = new HashMap<String, String>();
		transitions.entrySet().stream().forEach(x -> newTransitions.put(x.getValue(), x.getKey()));
		transitions = newTransitions;
	}
	
	public void addClasswiseTransition(String source, String target) {
		transitions.put(source, target);
	}
	
	public List<ClassPair> generateClassPairs(NominalMapping mapping) {
		List<ClassPair> pairs = new ArrayList<ClassPair>();

		Supplier<Stream<String>> s = () -> mapping.getValues().stream();

		for (String key : transitions.keySet()) {
			String value = transitions.get(key);

			
			if (key.equals(ALL_CLASSES_LABEL)) {
				if (value.equals(ALL_CLASSES_LABEL)) {
					
					mapping.getValues()
						.stream()
						.forEach(x ->
								s.get().filter(y -> y != x)
								.forEach(y -> pairs.add(new ClassPair(x,y, mapping)))
								);
					return pairs;					
				} else {
					
					s.get().filter(y -> y != value).forEach(y -> pairs.add(new ClassPair(y, value, mapping)));
				}
			} else if (value.equals(ALL_CLASSES_LABEL)) {
				s.get().filter(y -> y != key).forEach(y -> pairs.add(new ClassPair(key, y, mapping)));
			} else {
				pairs.add(new ClassPair(key, value, mapping));
			}
		}
		
		return pairs;
	}
}
