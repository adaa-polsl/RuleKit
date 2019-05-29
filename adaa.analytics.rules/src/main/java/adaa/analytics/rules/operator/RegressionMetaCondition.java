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
package adaa.analytics.rules.operator;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.conditions.ParameterCondition;

public class RegressionMetaCondition extends ParameterCondition {

	protected Operator operator;
	
	
	public RegressionMetaCondition(ParameterHandler parameterHandler, boolean becomeMandatory, Operator operator) {
		super(parameterHandler, becomeMandatory);
		this.operator = operator;
	}
	
	@Override
	public boolean isConditionFullfilled() {
		if (operator == null) {
			throw new IllegalAccessError("Invalid classification meta condition!");
		}
		
		// try learner operators
		InputPort port = operator.getInputPorts().getPortByName("training set");
		if (port == null) {
			port = operator.getInputPorts().getPortByName("labelled data");
		} 
		
		ExampleSetMetaData setMeta = (ExampleSetMetaData)port.getMetaData();
		boolean out =
			setMeta != null && 
			setMeta.getLabelMetaData() != null &&
			setMeta.getLabelMetaData().isNumerical() &&
			setMeta.getAttributeByRole(SurvivalRule.SURVIVAL_TIME_ROLE) == null;
		
		return out;
	}

}
