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
package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.ExampleSet;

public class RuleFactory {
	
	public static final int CLASSIFICATION = 1; 
	public static final int REGRESSION = 2;
	public static final int SURVIVAL = 3;
	
	protected int type;
	protected boolean isVoting = true;
	protected Knowledge knowledge;
	protected InductionParameters params;
	
	public int getType() { return this.type; }
	
	public RuleFactory(int type, boolean isVoting) {
		this.type = type;
	}
	
	public RuleFactory(int type, boolean isVoting, InductionParameters params, Knowledge knowledge) {
		this.params = params;
		this.type = type;
		this.knowledge = knowledge;
	}
	
	public Rule create(CompoundCondition premise, ElementaryCondition consequence) {
		switch (type) {
		case CLASSIFICATION:
			return new ClassificationRule(premise, consequence);
		case REGRESSION:
			return new RegressionRule(premise, consequence);
		case SURVIVAL:
			return new SurvivalRule(premise, consequence);
		}
		
		return null;
	}
	
	public RuleSetBase create(ExampleSet set) {
		switch (type) {
		case CLASSIFICATION:
			return new ClassificationRuleSet(set, isVoting, params, knowledge);
		case REGRESSION:
			return new RegressionRuleSet(set, isVoting, params, knowledge);
		case SURVIVAL:
			return new SurvivalRuleSet(set, isVoting, params, knowledge);
		}
		
		return null;
	}
	
}
