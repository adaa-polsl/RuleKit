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

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ClassificationRuleSet;
import adaa.analytics.rules.utils.RapidMiner5;
import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.PlatformUtilities;
import com.rapidminer5.operator.io.ArffExampleSource;
import org.junit.Test;
import utils.TestResourcePathFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import static org.junit.Assert.assertEquals;

public class ExpertRuleGeneratorTest {

    private final static String TRAIN_DEALS_FILE = "/data/deals-train.arff";
    private final static String TRAIN_DEALS_DOUBLED_FILE = "/data/deals-train-x2.arff";

    private final static String LABEL_ATTRIBUTE = "Future Customer";

    private void initRapidMiner() {
        System.setProperty(PlatformUtilities.PROPERTY_RAPIDMINER_HOME, Paths.get("").toAbsolutePath().toString());
        LogService.getRoot().setLevel(Level.OFF);
        RapidMiner.setExecutionMode(RapidMiner.ExecutionMode.COMMAND_LINE);

        RapidMiner.init();
    }

    private ExampleSet getExampleSet(String filePath) throws OperatorCreationException, OperatorException {


        ArffExampleSource trainArff = RapidMiner5.createOperator(ArffExampleSource.class);

        com.rapidminer.Process process = new com.rapidminer.Process();
        process.getRootOperator().getSubprocess(0).addOperator(trainArff);

        trainArff.getOutputPorts().getPortByName("output").connectTo(
                process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));

        trainArff.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, filePath);

        IOContainer out = process.run();
        IOObject[] objects = out.getIOObjects();

        return  (ExampleSet) objects[0];
    }

    private ClassificationRuleSet trainModel(ExampleSet exampleSet) throws OperatorCreationException, OperatorException {

        ChangeAttributeRole trainRoleSetter = OperatorService.createOperator(ChangeAttributeRole.class);
        AbstractLearner ruleGenerator = RapidMiner5.createOperator(ExpertRuleGenerator.class);

        com.rapidminer.Process process = new com.rapidminer.Process();
        process.getRootOperator().getSubprocess(0).addOperator(trainRoleSetter);
        process.getRootOperator().getSubprocess(0).addOperator(ruleGenerator);

        trainRoleSetter.getOutputPorts().getPortByName("example set output").connectTo(ruleGenerator.getInputPorts().getPortByName("training set"));
        ruleGenerator.getOutputPorts().getPortByName("model").connectTo(
                process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));

        process.getRootOperator().getSubprocess(0).getInnerSources().getPortByIndex(0).connectTo(trainRoleSetter.getInputPorts().getPortByName("example set input"));

        trainRoleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, LABEL_ATTRIBUTE);
        trainRoleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);

        ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_MIN_RULE_COVERED, "8");
        ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_INDUCTION_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.BinaryEntropy));
        ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_PRUNING_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.BinaryEntropy));
        //ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_PRUNING_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.UserDefined));
        //ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_USER_PRUNING_EQUATION, "2 * p / n");
        ruleGenerator.setParameter(ExpertRuleGenerator.PARAMETER_VOTING_MEASURE, ClassificationMeasure.getName(ClassificationMeasure.C2));

        IOContainer ioInput = new IOContainer(exampleSet);
        IOContainer out = process.run(ioInput);

        IOObject[] objects = out.getIOObjects();

        return (ClassificationRuleSet) objects[0];
    }

    private void assertRulesSetsEquals(ClassificationRuleSet expected, ClassificationRuleSet actual) {
        assertEquals("Both models should have same number of rules",
                expected.getRules().size(),
                actual.getRules().size());
        for (int i = 0; i < expected.getRules().size(); i++) {
            assertEquals("Rules from both sets should be the same",
                    expected.getRules().get(i).toString(),
                    actual.getRules().get(i).toString());
        }
    }

    @Test
    public void testRuleInductionOnSplittedExampleSet() throws OperatorException, OperatorCreationException {
        this.initRapidMiner();

        Path trainFilePath = TestResourcePathFactory.get(TRAIN_DEALS_FILE);
        Path doubledFilePath = TestResourcePathFactory.get(TRAIN_DEALS_DOUBLED_FILE);
        ExampleSet exampleSet = getExampleSet(trainFilePath.toString());
        ExampleSet doubledExampleSet = getExampleSet(doubledFilePath.toString());
        SplittedExampleSet splittedExampleSet = new SplittedExampleSet(doubledExampleSet, 0.5, SplittedExampleSet.LINEAR_SAMPLING, false, 0);

        ClassificationRuleSet ruleSet = this.trainModel(exampleSet);
        splittedExampleSet.selectSingleSubset(0);
        ClassificationRuleSet ruleSetOnFirstSubset = this.trainModel(splittedExampleSet);
        splittedExampleSet.selectSingleSubset(1);
        ClassificationRuleSet ruleSetOnSecondSubset = this.trainModel(splittedExampleSet);

        assertEquals("Both models should have same number of rules",
                ruleSet.getRules().size(),
                ruleSetOnFirstSubset.getRules().size());
        assertEquals("Both models should have same number of rules",
                ruleSet.getRules().size(),
                ruleSetOnSecondSubset.getRules().size());
        assertEquals("Both models should have same number of rules",
                ruleSetOnFirstSubset.getRules().size(),
                ruleSetOnSecondSubset.getRules().size());
        assertRulesSetsEquals(ruleSet, ruleSetOnFirstSubset);
        assertRulesSetsEquals(ruleSetOnFirstSubset, ruleSetOnSecondSubset);
    }
}

