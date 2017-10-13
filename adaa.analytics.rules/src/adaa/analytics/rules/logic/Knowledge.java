package adaa.analytics.rules.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.ExampleSet;

public class Knowledge implements Serializable {
	
	private static final long serialVersionUID = 7649251655125558583L;
	
	protected boolean extendUsingPreferred;
	protected boolean extendUsingAutomatic;
	protected boolean induceUsingPreferred;
	protected boolean induceUsingAutomatic;
	protected boolean considerOtherClasses;
	protected int preferredCountPerRule;
	
	protected boolean isRegression;
	protected int numClasses;
	
	/**
	 * Maps a class identifier to a list of preferred rules.
	 */
	protected Map<Integer, List<Rule>> rules = new HashMap<Integer, List<Rule>>();
	protected Map<Integer, MultiSet<CompoundCondition>> preferredConditions = new HashMap<Integer, MultiSet<CompoundCondition>>();
	protected Map<Integer, MultiSet<String>> preferredAttributes = new HashMap<Integer, MultiSet<String>>();
	protected Map<Integer, List<CompoundCondition>> forbiddenConditions = new HashMap<Integer, List<CompoundCondition>>();
	protected Map<Integer, List<String>> forbiddenAttributes = new HashMap<Integer, List<String>>();

	public List<Rule> getRules(int classId) { return rules.get(classId); }
	public MultiSet<CompoundCondition> getPreferredConditions(int classId) { return preferredConditions.get(classId); }
	public MultiSet<String> getPreferredAttributes(int classId) { return preferredAttributes.get(classId); }
	public List<CompoundCondition> getForbiddenConditions(int classId) { return forbiddenConditions.get(classId); }
	public List<String> getForbiddenAttributes(int classId) { return forbiddenAttributes.get(classId); }
	
	public List<Rule> getRules() { return rules.get(0); }
	public MultiSet<CompoundCondition> getPreferredConditions() { return preferredConditions.get(0); }
	public MultiSet<String> getPreferredAttributes() { return preferredAttributes.get(0); }
	public List<CompoundCondition> getForbiddenConditions() { return forbiddenConditions.get(0); }
	public List<String> getForbiddenAttributes() { return forbiddenAttributes.get(0); }

	public boolean isExtendUsingPreferred() { return extendUsingPreferred; }
	public void setExtendUsingPreferred(boolean extendUsingPreferred) { this.extendUsingPreferred = extendUsingPreferred; }
	
	public boolean isExtendUsingAutomatic() { return extendUsingAutomatic; }
	public void setExtendUsingAutomatic(boolean extendUsingAutomatic) { this.extendUsingAutomatic = extendUsingAutomatic; }

	public boolean isInduceUsingPreferred() { return induceUsingPreferred; }
	public void setInduceUsingPreferred(boolean induceUsingPreferred) { this.induceUsingPreferred = induceUsingPreferred; }

	public boolean isInduceUsingAutomatic() { return induceUsingAutomatic;}
	public void setInduceUsingAutomatic(boolean induceUsingAutomatic) { this.induceUsingAutomatic = induceUsingAutomatic; }

	public boolean isConsiderOtherClasses() { return considerOtherClasses; }
	public void setConsiderOtherClasses(boolean considerOtherClasses) { this.considerOtherClasses = considerOtherClasses; }

	public int getPreferredCountPerRule() { return preferredCountPerRule; }
	public void setPreferredCountPerRule(int preferredCountPerRule) { this.preferredCountPerRule = preferredCountPerRule; }

	
	public Knowledge(ExampleSet dataset, MultiSet<Rule> rules, MultiSet<Rule> preferredConditions, MultiSet<Rule> forbiddenConditions) {
		
		this.isRegression = dataset.getAttributes().getLabel().isNumerical();
		
		this.extendUsingPreferred = false;
		this.extendUsingAutomatic = false;
		this.induceUsingPreferred = false;
		this.induceUsingAutomatic = false;
		this.considerOtherClasses = false;
		this.preferredCountPerRule = Integer.MAX_VALUE;

		int numClasses = (dataset.getAttributes().getLabel().isNominal()) 
			?  dataset.getAttributes().getLabel().getMapping().size() : 1;
		
		for (int i = 0; i < numClasses; ++i) {
			 this.rules.put(i, new ArrayList<Rule>());
			 this.preferredConditions.put(i, new MultiSet<CompoundCondition>());
			 this.forbiddenConditions.put(i, new ArrayList<CompoundCondition>());
			 this.preferredAttributes.put(i, new MultiSet<String>());
			 this.forbiddenAttributes.put(i, new ArrayList<String>());
		}
		
		for (Rule r : rules) {
			SingletonSet set = (SingletonSet)r.getConsequence().getValueSet();
			 int c = (int)set.getValue();
			 this.rules.get(c).add(r);
		}
		
		for (Rule r : preferredConditions) {
			SingletonSet set = (SingletonSet)r.getConsequence().getValueSet();
			 int c = (int)set.getValue();
			 
			 ElementaryCondition ec = (ElementaryCondition) r.getPremise().getSubconditions().get(0);
			 if (ec.getValueSet() instanceof Universum) {
				 this.preferredAttributes.get(c).add(ec.getAttribute(), preferredConditions.getCount(r));
			 } else {
				 this.preferredConditions.get(c).add(r.getPremise(), preferredConditions.getCount(r));
			 }
		}
		
		for (Rule r : forbiddenConditions) {
			SingletonSet set = (SingletonSet)r.getConsequence().getValueSet();
			 int c = (int)set.getValue();
			 
			 ElementaryCondition ec = (ElementaryCondition) r.getPremise().getSubconditions().get(0);
			 if (ec.getValueSet() instanceof Universum) {
				 this.forbiddenAttributes.get(c).add(ec.getAttribute());
			 } else {
				 this.forbiddenConditions.get(c).add(r.getPremise());
			 }
		}
	}
	
	public boolean isForbidden(String attribute, IValueSet set, int classId) {
		boolean out = false;
		
		// check if forbidden attribute
		if (forbiddenAttributes.containsKey(attribute)) {
			return true;
		}
		
		// check all forbidden conditions for class
		for (CompoundCondition c : forbiddenConditions.get(classId)) {
			int conditionsCount = 0;
			int intersectionsCount = 0;

			// analyse all elementary subconditions
			for (ConditionBase cb : c.getSubconditions()) {
				ElementaryCondition ec = (cb instanceof ElementaryCondition) ? (ElementaryCondition)cb : null;
				if (ec != null) {
					if(ec.getAttribute().equals(attribute)) {
						++conditionsCount;
						if (ec.getValueSet().intersects(set)) {
							++intersectionsCount;
						}
					}
					
				} else {
					throw new RuntimeException("Only elementary conditions can be forbidden.");
				}
			}
			
			// value set must intersect with all conditions
			if (conditionsCount > 0 && conditionsCount == intersectionsCount) {
				out = true;
				break;
			}
		}
		
		return out;
	}
	
	public String toString() {
		String out = "extendUsingPreferred=" + extendUsingPreferred + "\n"
				+ "extendUsingAutomatic=" + extendUsingAutomatic + "\n"
				+ "induceUsingPreferred=" + induceUsingPreferred + "\n"
				+ "induceUsingAutomatic=" + induceUsingAutomatic + "\n"
				+ "considerOtherClasses=" + considerOtherClasses + "\n"
				+ "preferredCountPerRule=" + preferredCountPerRule + "\n\n";
		
		out += "Expert rules:\n";
		for (int key: rules.keySet()) {
			out += "\tClass " + key + "\n";
			for (Rule r: rules.get(key)) {
				out += "\t\t" + r.toString() + "\n";
			}
		}
		out += "Preferred conditions:\n";
		for (int key: preferredConditions.keySet()) {
			out += "\tClass " + key + "\n";
			for (CompoundCondition r: preferredConditions.get(key)) {
				out += "\t\t" + r.toString() + "\n";
			}
		}
		out += "Preferred attributes:\n";
		for (int key: preferredAttributes.keySet()) {
			out += "\tClass " + key + "\n";
			for (String r: preferredAttributes.get(key)) {
				out += "\t\t" + r.toString() + "\n";
			}
		}
		out += "Forbidden conditions:\n";
		for (int key: forbiddenConditions.keySet()) {
			out += "\tClass " + key + "\n";
			for (CompoundCondition r: forbiddenConditions.get(key)) {
				out += "\t\t" + r.toString() + "\n";
			}
		}
		
		out += "Forbidden attributes:\n";
		for (int key: forbiddenAttributes.keySet()) {
			out += "\tClass " + key + "\n";
			for (String r: forbiddenAttributes.get(key)) {
				out += "\t\t" + r.toString() + "\n";
			}
		}
		return out;
	}

}
