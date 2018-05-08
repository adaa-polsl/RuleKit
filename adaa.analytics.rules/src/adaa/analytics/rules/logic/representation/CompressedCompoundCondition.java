package adaa.analytics.rules.logic.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompressedCompoundCondition extends CompoundCondition {

	public CompressedCompoundCondition(CompoundCondition cc) {
		Map<String, ElementaryCondition> shortened = new HashMap<String, ElementaryCondition>();
		Set<ConditionBase> unshortened = new HashSet<ConditionBase>();
		
		for (ConditionBase cnd : cc.getSubconditions()) {
			if (cnd instanceof ElementaryCondition && cnd.isPrunable() == true) {
				ElementaryCondition ec = (ElementaryCondition)cnd;
				String attr = ec.getAttribute();
				
				if (shortened.containsKey(attr)) {
					ElementaryCondition old = shortened.get(attr);
					ec = old.intersect(ec);
				}
		
				shortened.put(attr, ec);
			} else {
				unshortened.add(cnd);
			}
		}
		
		for (ConditionBase cnd : unshortened) {
			subconditions.add(cnd);
		}
		
		for (ConditionBase cnd : shortened.values()) {
			subconditions.add(cnd);
		}
	}
}
