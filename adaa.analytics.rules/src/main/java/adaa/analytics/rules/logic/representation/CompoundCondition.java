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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.util.*;

public class CompoundCondition extends ConditionBase {

	private static final long serialVersionUID = -2110506055974272967L;

	protected List<ConditionBase> subconditions = new ArrayList<ConditionBase>();
	
	protected LogicalOperator operator;
	
	public void setLogicalOperator(LogicalOperator operator) { this.operator = operator; }
	public void addSubcondition(ConditionBase cnd) { subconditions.add(cnd); }
	public void removeSubcondition(ConditionBase cnd) { subconditions.remove(cnd); }
	
	public List<ConditionBase> getSubconditions() { return subconditions; }
	
	public CompoundCondition() {
		this.operator = LogicalOperator.CONJUNCTION;
	}
	
	@Override
	protected boolean internalEvaluate(Example ex) {
		for (ConditionBase cond : subconditions) {
			boolean partial = cond.evaluate(ex);
			if (operator == LogicalOperator.CONJUNCTION && partial == false) {
				return false;
			} else if (operator == LogicalOperator.ALTERNATIVE && partial == true) {
				return true;
			}
		}
		return (operator == LogicalOperator.CONJUNCTION) ? true : false;
	}
	
	
	@Override
	protected void internalEvaluate(ExampleSet set, Set<Integer> outIndices) {
		
		IntegerBitSet temp = new IntegerBitSet(set.size());
		
		int i = 0;
		for (ConditionBase cond : subconditions) {
			cond.evaluate(set, temp);

			if (i == 0 || operator == LogicalOperator.ALTERNATIVE) {
				outIndices.addAll(temp);
			} else if (operator == LogicalOperator.CONJUNCTION) {
				outIndices.retainAll(temp);
			} 
			
			++i;
		}
		
	}
	
	
	public String toString() {
		String s = "";
		String op = operator == LogicalOperator.ALTERNATIVE ? " OR " : " AND "; 
		
		Map<String, ElementaryCondition> shortened = new HashMap<String, ElementaryCondition>();
		Set<ConditionBase> unshortened = new HashSet<ConditionBase>();
		
		for (ConditionBase cnd : subconditions) {
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
		
		// add unshortened conditions
		for (ConditionBase cnd : unshortened) {
			s += cnd.toString() + op; 
		}
		
		// add shortened conditions
		for (ConditionBase cnd : shortened.values()) {
			s += cnd.toString() + op; 
		}
		
		s = s.substring(0, Math.max(0, s.length() - op.length()));
		
		if (type == Type.FORCED) {
			s = "[[" + s + "]]";
		} else if (type == Type.PREFERRED) {
			s = "[" + s + "]";
		}
		
		return s;
	}
	
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else {
			CompoundCondition ref = (obj instanceof CompoundCondition) ? (CompoundCondition)obj : null;
			if (ref != null) {
				// compare operators
				boolean res = this.operator == ref.operator;
				
				// compare sizes
				res &= this.subconditions.size() == ref.getSubconditions().size();
				
				// compare subconditions
				Iterator<ConditionBase> it1 = this.subconditions.listIterator();
				Iterator<ConditionBase> it2 = ref.subconditions.listIterator();

				while (res && it1.hasNext() && it2.hasNext()) {
					ConditionBase c1 = it1.next();
					ConditionBase c2 = it2.next();
					res &= this.subconditions.contains(c2) && ref.subconditions.contains(c1);
				}
				return res;
				
			} else {
				return false;
			}
		}
	}
	
	@Override
	public int hashCode() {
		int result = 0;
		for (ConditionBase condition : subconditions) {
			result += condition.hashCode();
		}
		result = 31 * result + operator.hashCode();
		return result;
	}
	
	@Override
	public Set<String> getAttributes() {
		Set<String> atts = new HashSet<String>();
		for (ConditionBase c: subconditions) {
			atts.addAll(c.getAttributes());
		}
		return atts;
	}
	
	
}
