/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.ExampleSet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing user's knowledge.
 * @author Adam Gudys
 *
 */
public class Knowledge implements Serializable {
	/** Serialization id */
	private static final long serialVersionUID = 7649251655125558583L;
	
	/** Flag indicating if initial (user's) rules should be extended with preferred conditions/attributes. */
	protected boolean extendUsingPreferred;
	
	/** Flag indicating if initial (user's) rules should be extended with automatic conditions/attributes. */
	protected boolean extendUsingAutomatic;
	
	/** Flag indicating if new rules should be induced with preferred conditions and attributes. */
	protected boolean induceUsingPreferred;
	
	/** Flag indicating if new rules should be induced with automatic conditions. */
	protected boolean induceUsingAutomatic;
	
	/** Flag indicating whether automatic induction should be performed for classes for which no 
	* 	user's requirements have been defined (classification problems only) .
	*/
	protected boolean considerOtherClasses;
	
	/** Maximum number of preferred conditions per rule. */
	protected int preferredConditionsPerRule;
	
	/** Maximum number of preferred attributes per rule. */
	protected int preferredAttributesPerRule;
	
	/** Auxiliary files indicating whether the knowledge concerns regression problem. */
	protected boolean isRegression;
	
	/** Auxiliary files indicating number classes (classification problems only). */
	protected int numClasses;
	
	
	
	/** Maps a class identifier to a list of initial rules (for regression problems, 0 is used as a class id). */
	protected Map<Integer, List<Rule>> rules = new HashMap<Integer, List<Rule>>();
	
	/** Maps a class identifier to a list of preferred conditions (for regression problems, 0 is used as a class id). */
	protected Map<Integer, MultiSet<CompoundCondition>> preferredConditions = new HashMap<Integer, MultiSet<CompoundCondition>>();
	
	/** Maps a class identifier to a list of preferred attributes (for regression problems, 0 is used as a class id). */
	protected Map<Integer, MultiSet<String>> preferredAttributes = new HashMap<Integer, MultiSet<String>>();
	
	/** Maps a class identifier to a list of forbidden conditions (for regression problems, 0 is used as a class id). */
	protected Map<Integer, List<CompoundCondition>> forbiddenConditions = new HashMap<Integer, List<CompoundCondition>>();
	
	/** Maps a class identifier to a list of forbidden attributes (for regression problems, 0 is used as a class id). */
	protected Map<Integer, List<String>> forbiddenAttributes = new HashMap<Integer, List<String>>();

	
	
	/** Gets element of {@link #rules} for a given class identifier. */
	public List<Rule> getRules(int classId) { return rules.get(classId); }
	
	/** Gets element of {@link #preferredConditions} for a given class identifier. */
	public MultiSet<CompoundCondition> getPreferredConditions(int classId) { return preferredConditions.get(classId); }
	
	/** Gets element of {@link #preferredAttributes} for a given class identifier. */
	public MultiSet<String> getPreferredAttributes(int classId) { return preferredAttributes.get(classId); }
	
	/** Gets element of {@link #forbiddenConditions} for a given class identifier. */
	public List<CompoundCondition> getForbiddenConditions(int classId) { return forbiddenConditions.get(classId); }
	
	/** Gets element of {@link #forbiddenAttributes} for a given class identifier. */
	public List<String> getForbiddenAttributes(int classId) { return forbiddenAttributes.get(classId); }
	
	
	/** Gets element of {@link #rules} for 0th (default) class identifier. */
	public List<Rule> getRules() { return rules.get(0); }
	
	/** Gets element of {@link #preferredConditions} for 0th (default) class identifier. */
	public MultiSet<CompoundCondition> getPreferredConditions() { return preferredConditions.get(0); }
	
	/** Gets element of {@link #preferredAttributes} for 0th (default) class identifier. */
	public MultiSet<String> getPreferredAttributes() { return preferredAttributes.get(0); }
	
	/** Gets element of {@link #forbiddenConditions} for 0th (default) class identifier. */
	public List<CompoundCondition> getForbiddenConditions() { return forbiddenConditions.get(0); }
	
	/** Gets element of {@link #forbiddenAttributes} for 0th (default) class identifier. */
	public List<String> getForbiddenAttributes() { return forbiddenAttributes.get(0); }

	
	/** Gets {@link #extendUsingPreferred}. */
	public boolean isExtendUsingPreferred() { return extendUsingPreferred; }
	/** Sets {@link #extendUsingPreferred}. */
	public void setExtendUsingPreferred(boolean extendUsingPreferred) { this.extendUsingPreferred = extendUsingPreferred; }
	
	/** Gets {@link #extendUsingAutomatic}. */
	public boolean isExtendUsingAutomatic() { return extendUsingAutomatic; }
	/** Sets {@link #extendUsingAutomatic}. */
	public void setExtendUsingAutomatic(boolean extendUsingAutomatic) { this.extendUsingAutomatic = extendUsingAutomatic; }

	/** Gets {@link #induceUsingPreferred}. */
	public boolean isInduceUsingPreferred() { return induceUsingPreferred; }
	/** Sets {@link #induceUsingPreferred}. */
	public void setInduceUsingPreferred(boolean induceUsingPreferred) { this.induceUsingPreferred = induceUsingPreferred; }

	/** Gets {@link #induceUsingAutomatic}. */
	public boolean isInduceUsingAutomatic() { return induceUsingAutomatic;}
	/** Sets {@link #induceUsingAutomatic}. */
	public void setInduceUsingAutomatic(boolean induceUsingAutomatic) { this.induceUsingAutomatic = induceUsingAutomatic; }

	/** Gets {@link #considerOtherClasses}. */
	public boolean isConsiderOtherClasses() { return considerOtherClasses; }
	/** Sets {@link #considerOtherClasses}. */
	public void setConsiderOtherClasses(boolean considerOtherClasses) { this.considerOtherClasses = considerOtherClasses; }

	/** Gets {@link #preferredConditionsPerRule}. */
	public int getPreferredConditionsPerRule() { return preferredConditionsPerRule; }
	/** Sets {@link #preferredConditionsPerRule}. */
	public void setPreferredConditionsPerRule(int preferredConditionsPerRule) { this.preferredConditionsPerRule = preferredConditionsPerRule; }

	/** Gets {@link #preferredAttributesPerRule}. */
	public int getPreferredAttributesPerRule() { return preferredAttributesPerRule; }
	/** Sets {@link #preferredAttributesPerRule}. */
	public void setPreferredAttributesPerRule(int preferredAttributesPerRule) { this.preferredAttributesPerRule = preferredAttributesPerRule; }

	/**
	 * Initializes knowledge from collections of initial rules, preferred/forbidden conditions and attributes.
	 * @param dataset 
	 * @param rules Collection of initial rules.
	 * @param preferredConditions Collection of preferred conditions (it also contains preferred attributes).
	 * @param forbiddenConditions Collection of forbidden conditions (it also contains forbidden attributes).
	 */
	public Knowledge(ExampleSet dataset, MultiSet<Rule> rules, MultiSet<Rule> preferredConditions, MultiSet<Rule> forbiddenConditions) {
		
		this.isRegression = dataset.getAttributes().getLabel().isNumerical();
		
		this.extendUsingPreferred = false;
		this.extendUsingAutomatic = false;
		this.induceUsingPreferred = false;
		this.induceUsingAutomatic = false;
		this.considerOtherClasses = false;
		this.preferredConditionsPerRule = Integer.MAX_VALUE;
		this.preferredAttributesPerRule = Integer.MAX_VALUE;

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
	
	/**
	 * Checks if given condition is forbidden by the knowledge (0 is used as a class id). 
	 * @param attribute Attribute upon the condition is built.
	 * @param set Value set in the condition.
	 * @return Test result.
	 */
	public boolean isForbidden(String attribute, IValueSet set) {
		return isForbidden(attribute, set, 0);
	}
	
	/**
	 * Checks if given condition is forbidden by the knowledge for specified class.
	 * @param attribute Attribute upon the condition is built.
	 * @param set Value set in the condition.
	 * @param classId Class identifier 
	 * @return Test result.
	 */
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
	
	/**
	 * Generates text representation of user's knowledge.
	 * @return Text representation of knowledge.
	 */
	public String toString() {
		String out = "extendUsingPreferred=" + extendUsingPreferred + "\n"
				+ "extendUsingAutomatic=" + extendUsingAutomatic + "\n"
				+ "induceUsingPreferred=" + induceUsingPreferred + "\n"
				+ "induceUsingAutomatic=" + induceUsingAutomatic + "\n"
				+ "considerOtherClasses=" + considerOtherClasses + "\n"
				+ "preferredConditionsPerRule=" + preferredConditionsPerRule + "\n"
				+ "preferredAttributesPerRule=" + preferredAttributesPerRule + "\n\n";
		
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
				out += "\t\t" + preferredConditions.get(key).getCount(r) + ": " + r.toString() + "\n";
			}
		}
		out += "Preferred attributes:\n";
		for (int key: preferredAttributes.keySet()) {
			out += "\tClass " + key + "\n";
			for (String r: preferredAttributes.get(key)) {
				out += "\t\t" + preferredAttributes.get(key).getCount(r) + ": " + r.toString() + "\n";
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
