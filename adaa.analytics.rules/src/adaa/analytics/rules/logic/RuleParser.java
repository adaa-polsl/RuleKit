package adaa.analytics.rules.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rapidminer.operator.ports.metadata.AttributeMetaData;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;

public class RuleParser {
	public static Rule parseRule(String s, ExampleSetMetaData meta) {
		Rule rule = null; 
		
    	Pattern pattern = Pattern.compile("IF\\s+(?<premise>.+)\\s+THEN\\s+(?<consequence>.+)");
    	Matcher matcher = pattern.matcher(s);
 	
    	boolean isSurvival = false;
		if (meta.getAttributeByRole(SurvivalRule.SURVIVAL_TIME_ROLE) != null) {
			isSurvival = true;
		}
    	
    	if (matcher.find()) {
	    	String pre = matcher.group("premise");
	    	String con = matcher.group("consequence");
	    	
	    	CompoundCondition premise = parseCompoundCondition(pre, meta);
	    	ElementaryCondition consequence = parseElementaryCondition(con, meta);
	    	
	    	if (premise == null || consequence == null) {
	    		return null;
	    	}
	    	
	    	rule = meta.getLabelMetaData().isNominal() 
	    			? new ClassificationRule(premise, consequence) 
	    			: (isSurvival 
						? new SurvivalRule(premise, consequence)
						: new RegressionRule(premise, consequence));
    	}
    	
    	return rule;
	}
	
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
	
	public static ElementaryCondition parseElementaryCondition(String s, ExampleSetMetaData meta) {
		ElementaryCondition out = null;
    	
		// remove surrounding white characters
		Pattern regex = Pattern.compile("\\s*(?<internal>.+)\\s*");
		Matcher matcher = regex.matcher(s);
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
		
		regex = Pattern.compile("(?<attribute>[\\w\\-]+)\\s*=\\s*(?<value>.+)");
    	matcher = regex.matcher(s);
    	
    	if (matcher.find()) {
	    	String attribute = matcher.group("attribute");
	    	String valueString = matcher.group("value");
	    	
	    	IValueSet valueSet = null;
			
	    	AttributeMetaData attributeMeta = meta.getAttributeByName(attribute);

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
		    		List<String> mapping = new ArrayList<String>();
					mapping.addAll(attributeMeta.getValueSet());
					double v = mapping.indexOf(value);
		    		if (v == -1) {
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
				    		
				    	valueSet = new Interval(
				    			lo.equals("-inf") ? Interval.MINUS_INF : Double.parseDouble(lo), 
				    			hi.equals("inf")? Interval.INF : Double.parseDouble(hi),
				    			leftClosed, rightClosed);
			    	}
		    	}
	    	}
	    	
	    	out = new ElementaryCondition(attribute, valueSet);
	    	out.setType(type);
    	}
    	
		return out;
	}
}
