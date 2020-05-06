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
package utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import adaa.analytics.rules.logic.representation.SurvivalRule;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.PlatformUtilities;
import com.rapidminer5.operator.io.ArffExampleSource;

import adaa.analytics.rules.utils.RapidMiner5;

public class ArffFileLoader {

	private static ExampleSet loadExampleSet(String filePath, String labelParameterName, String survivalTimeParameter) throws OperatorCreationException, OperatorException {
		System.setProperty(PlatformUtilities.PROPERTY_RAPIDMINER_HOME, Paths.get("").toAbsolutePath().toString());
		LogService.getRoot().setLevel(Level.OFF);
		RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);

		RapidMiner.init();

		ArffExampleSource arffSource = RapidMiner5.createOperator(ArffExampleSource.class);
		//role setter allows for deciding which attribute is class attribute
		ChangeAttributeRole roleSetter = (ChangeAttributeRole) OperatorService.createOperator(ChangeAttributeRole.class);

		File arffFile = Paths.get(filePath).toFile();

		arffSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffFile.getAbsolutePath());
		roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelParameterName);
		roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

		if (survivalTimeParameter != null) {
			List<String[]> roles = new ArrayList<>();
			roles.add(new String[]{survivalTimeParameter, SurvivalRule.SURVIVAL_TIME_ROLE});
			roleSetter.setListParameter(roleSetter.PARAMETER_CHANGE_ATTRIBUTES, roles);
		}

		Process process = new com.rapidminer.Process();
		process.getRootOperator().getSubprocess(0).addOperator(arffSource);
		process.getRootOperator().getSubprocess(0).addOperator(roleSetter);

		arffSource.getOutputPorts().getPortByName("output").connectTo(
				roleSetter.getInputPorts().getPortByName("example set input"));

		roleSetter.getOutputPorts().getPortByName("example set output").connectTo(
				process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));

		IOContainer c = process.run();
		//parsed arff file
		return (ExampleSet)c.getElementAt(0);
	}


	public static ExampleSet load(String filePath, String labelParameterName, String survivalTimeParameter) throws OperatorException, OperatorCreationException {
		return loadExampleSet(filePath, labelParameterName, survivalTimeParameter);
	}

	public static ExampleSet load(String filePath, String labelParameterName) throws OperatorCreationException, OperatorException {
		return loadExampleSet(filePath, labelParameterName, null);
	}
}
