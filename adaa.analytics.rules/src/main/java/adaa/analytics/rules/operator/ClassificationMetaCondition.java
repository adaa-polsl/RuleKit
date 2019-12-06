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

/**
 * Class representing condition fulfilled by classification problems.
 * Condition is verified on the basis of metadeta of example set connected to the one of operator's input:
 * - training set,
 * - labelled data.
 * 
 * @author Adam Gudys
 *
 */
public class ClassificationMetaCondition extends ParameterCondition {

	/**
	 * Invokes base class constructor.
	 * @param parameterHandler Operator which inputs are examined.
	 */
	public ClassificationMetaCondition(ParameterHandler parameterHandler) {
		super(parameterHandler, false);
	}

	/**
	 * Verifies if conditon is fulfilled.
	 * @return Test result.
	 */
	@Override
	public boolean isConditionFullfilled() {
		Operator operator = (Operator)parameterHandler;
				
		if (operator == null) {
			throw new IllegalAccessError("Invalid classification meta condition!");
		}
		
		// try learner operators
		InputPort port = operator.getInputPorts().getPortByName("training set");
		if (port == null) {
			port = operator.getInputPorts().getPortByName("labelled data");
		} 
		
		ExampleSetMetaData setMeta = (ExampleSetMetaData) port.getMetaData();
		boolean out =
			setMeta != null && 
			setMeta.getLabelMetaData() != null &&
			setMeta.getLabelMetaData().isNominal() &&
			setMeta.getAttributeByRole(SurvivalRule.SURVIVAL_TIME_ROLE) == null;
		
		return out;
	}

}
