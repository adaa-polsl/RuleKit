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

import adaa.analytics.rules.data.ColumnMetaData;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Float.NaN;

/**
 * Class for parsing rules from text.
 * @author Adam Gudys
 *
 */
public class RuleParser {

	/**
	 * Parses a rule.
	 * @param s String to be parsed.
	 * @param meta Example set meta data.
	 * @return Rule instance.
	 */
	public static Rule parseRule(String s, ExampleSetMetaData meta) {
		Rule rule = null; 

		Pattern pattern = Pattern.compile("IF\\s+(?<premise>.+)\\s+THEN(?<consequence>\\s+.*|\\s*)");
		Matcher matcher = pattern.matcher(s);

		boolean isSurvival = false;
		if (meta.getAttributeByRole(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
			isSurvival = true;
		}

		if (matcher.find()) {
			String pre = matcher.group("premise");
			String con = matcher.group("consequence");

			ElementaryCondition consequence = null;
			CompoundCondition premise = parseCompoundCondition(pre, meta);

			if (con == null || con.trim().length() == 0) {
				if (!meta.getLabelMetaData().isNumerical()) {
					Logger.log("Empty conclusion for nominal label"+ "\n", Level.WARNING);
				} else {
					consequence = new ElementaryCondition();
					consequence.attribute = meta.getLabelMetaData().getName();
					consequence.valueSet = new SingletonSet(NaN, null);
					consequence.adjustable = false;
					consequence.disabled = false;
				}
			} else {
				consequence = parseElementaryCondition(con, meta);
			}

			if (premise != null && consequence != null) {

				rule = meta.getLabelMetaData().isNominal()
						? new ClassificationRule(premise, consequence)
						: (isSurvival
						? new SurvivalRule(premise, consequence)
						: new RegressionRule(premise, consequence));
			}
		}

		if (rule == null) {
			Logger.log("Omitting expert's knowledge entry: " + s + "\n", Level.WARNING);
		}

    	return rule;
	}
	
	/**
	 * Parses a compound condition.
	 * @param s String to be parsed.
	 * @param meta Example set meta data.
	 * @return Compound condition instance.
	 */
	public static CompoundCondition parseCompoundCondition(String s, ExampleSetMetaData meta) {
		CompoundCondition out = new CompoundCondition();
		
		String tokens[] = s.split("AND");
		for (String t: tokens) {
			ElementaryCondition sub = parseElementaryCondition(t, meta);
			if (sub == null) {
				return null;
			}
			out.addSubcondition(sub);
		}
		
		return out;
	}
	
	/**
	 * Parses an elementary condition.
	 * @param s String to be parsed.
	 * @param meta Example set meta data.
	 * @return Elementary condition instance.
	 */
	public static ElementaryCondition parseElementaryCondition(String s, ExampleSetMetaData meta) {
		ElementaryCondition out = null;

		// remove surrounding white characters
		Pattern regex = Pattern.compile("\\s*(?<internal>.+)\\s*");
		Matcher matcher = regex.matcher(s);
		boolean adjustable = false;
	//	s = matcher.group("internal");
		
		// remove surrounding [] or [[]]
    	int numBrackets = 0;
    	for (; numBrackets < 2; ++numBrackets) {
	    	regex = Pattern.compile("\\[(?<internal>.+)\\]");
	    	matcher = regex.matcher(s);
	    	if (!matcher.find()) {
	    		break;
	    	}
	    	s = matcher.group("internal");
    	}
		
		regex = Pattern.compile("(?<attribute>[\\w\\-\\s]+)\\s*(?<equality>(=|@=))\\s*(?<value>.+)");
    	matcher = regex.matcher(s);
    	
    	if (matcher.find()) {
	    	String attribute = matcher.group("attribute").trim();
	    	String valueString = matcher.group("value");
	    	String equality = matcher.group("equality");
	    	
	    	if (equality.equals("@=")) {
	    		adjustable = true;
	    	}
	    	
	    	IValueSet valueSet = null;

	    	ColumnMetaData attributeMeta = meta.getAttributeByName(attribute);
	    	if (attributeMeta == null) {
				Logger.log("Attribute <" + attribute + "> not found"+ "\n", Level.WARNING);
				return null;
			}

	    	ConditionBase.Type type = (numBrackets == 0) ? ConditionBase.Type.NORMAL :
	    		((numBrackets == 1) ? ConditionBase.Type.PREFERRED : ConditionBase.Type.FORCED);
	    	
	    	// check if universum used as value set
	    	if (Pattern.compile("Any").matcher(valueString).find()) {
	    		valueSet = new Universum();
	    	} else if (attributeMeta.isNominal()) { 
	    		regex = Pattern.compile("\\{(?<discrete>.+)\\}");
		    	matcher = regex.matcher(valueString);
		    	if (matcher.find()) {
		    		String value = matcher.group("discrete");
                    List<String> mapping = new ArrayList<String>(attributeMeta.getMapping().getValues());
					double v = mapping.indexOf(value);
		    		if (v == -1) {
						Logger.log("Invalid value <" + value + "> of the nominal attribute <" + attribute + ">"+ "\n", Level.WARNING);
						return null;
		    		}
		    		valueSet = new SingletonSet(v, mapping);
		    			
		    	}
	    	} else if (attributeMeta.isNumerical()) {
	    		regex = Pattern.compile("\\{(?<discrete>.+)\\}");
		    	matcher = regex.matcher(valueString);
		    	// 
		    	if (matcher.find()) {
		    		String value = matcher.group("discrete");
		    		double v = value.equals("NaN") ? Double.NaN : Double.parseDouble(value);
		    		valueSet = new SingletonSet(v, null);
		    	} else {
		    		boolean leftClosed = Pattern.compile("\\<.+").matcher(valueString).find();
			    	boolean rightClosed = Pattern.compile(".+\\>").matcher(valueString).find();
			    	
			    	regex = Pattern.compile("[\\<\\(](?<lo>[-\\w\\d\\.]+)\\s*,\\s*(?<hi>[-\\w\\d\\.]+)[\\>\\)]");
			    	matcher = regex.matcher(valueString);
	
			    	if (matcher.find()) {

			    		String lo = matcher.group("lo");
			    		String hi = matcher.group("hi");

						double numLo = Double.NaN;
						double numHi = Double.NaN;

			    		if (lo.equals("-inf")) {
			    			numLo = Interval.MINUS_INF;
						} else if (NumberUtils.isNumber(lo)) {
			    			numLo = Double.parseDouble(lo);
						} else {
							Logger.log("Invalid lower interval bound: " + lo + "\n" , Level.WARNING);
							return null;
						}

						if (hi.equals("inf")) {
							numHi = Interval.INF;
						} else if (NumberUtils.isNumber(hi)) {
							numHi = Double.parseDouble(hi);
						} else {
							Logger.log("Invalid upper interval bound: " + hi + "\n", Level.WARNING );
							return null;
						}

				    	valueSet = new Interval(numLo, numHi, leftClosed, rightClosed);
			    	} else {
						Logger.log("Invalid interval: " + valueString, Level.WARNING );
						return null;
					}
		    	}
	    	}
	    	
	    	if (valueSet != null) {
	    		out = new ElementaryCondition(attribute, valueSet);
	    		out.setType(type);
	    		out.setAdjustable(adjustable);
	    	}
    	} else {
			Logger.log("Invalid elementary condition: " + s + "\n", Level.WARNING );
		}
    	
		return out;
	}

}
