package adaa.analytics.rules.logic.induction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import adaa.analytics.rules.logic.induction.ClassPair;

import com.rapidminer.example.table.NominalMapping;

public class ActionInductionParameters extends InductionParameters {

	protected Map<String,String> transitions = new HashMap<String,String>();
	public static String ALL_CLASSES_LABEL = "*";
	
	public ActionInductionParameters() {
		// TODO Auto-generated constructor stub
	}
	
	
	public void setGenerateAllTransitions() {		
		transitions.clear();
		transitions.put(ALL_CLASSES_LABEL, ALL_CLASSES_LABEL);
	}
	
	public void addClasswiseTransition(String source, String target) {
		transitions.put(source, target);
	}
	
	public List<ClassPair> generateClassPairs(NominalMapping mapping) {
		List<ClassPair> pairs = new ArrayList<ClassPair>();
		Stream<String> s = mapping.getValues().stream();
		
		for (String key : transitions.keySet()) {
			String value = transitions.get(key);

			
			if (key.equals(ALL_CLASSES_LABEL)) {
				if (value.equals(ALL_CLASSES_LABEL)) {
					
					mapping.getValues()
						.stream()
						.forEach(x ->
								s.filter(y -> y != x)
								.forEach(y -> pairs.add(new ClassPair(x,y, mapping)))
								);
					return pairs;					
				} else {
					
					s.filter(y -> y != value).forEach(y -> pairs.add(new ClassPair(y, value, mapping)));
				}
			} else if (value.equals(ALL_CLASSES_LABEL)) {
				s.filter(y -> y != key).forEach(y -> pairs.add(new ClassPair(key, y, mapping)));
			} else {
				pairs.add(new ClassPair(key, value, mapping));
			}
		}
		
		return pairs;
	}
}