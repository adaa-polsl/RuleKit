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

import java.util.HashMap;
import java.util.HashSet;
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
