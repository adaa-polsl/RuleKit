package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.data.ColumnMetaData;
import adaa.analytics.rules.data.EColumnRole;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.logic.rulegenerator.OperatorCommandProxy;
import adaa.analytics.rules.logic.rulegenerator.RuleGenerator;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.operator.OperatorException;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import utils.RuleSetComparator;
import utils.TestResourcePathFactory;
import utils.config.TestConfig;
import utils.config.TestConfigParser;
import utils.reports.Const;
import utils.reports.TestReportWriter;
import utils.testcases.TestCase;
import utils.testcases.TestCaseFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

@RunWith(Theories.class)
public class RegressionSnCTest {

    private static final String CLASS_NAME = RegressionSnCTest.class.getSimpleName();
    private static final String REPORTS_DIRECTORY = Const.REPORTS_IN_DIRECTORY_PATH + CLASS_NAME + '/';

    @DataPoints("Test cases")
    public static TestCase[] getTestCases() throws ParseException {
        String configFilePath = Const.CONFIG_DIRECTORY + CLASS_NAME + ".xml";
        configFilePath = TestResourcePathFactory.get(configFilePath).toString();
        HashMap<String, TestConfig> configs = new TestConfigParser().parse(configFilePath);
        List<TestCase> testCases = TestCaseFactory.make(configs, REPORTS_DIRECTORY);
        return testCases.toArray(new TestCase[0]);
    }

    public RegressionSnCTest() {
        File directory = new File(Const.REPORTS_OUT_DIRECTORY_PATH + CLASS_NAME);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    private void writeReport(TestCase testCase, RuleSetBase ruleSet) {
            try {
                TestReportWriter reportWriter = new TestReportWriter(CLASS_NAME + '/' + testCase.getName());
                reportWriter.write(ruleSet);
                reportWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Theory
    public void runTestCase(@FromDataPoints("Test cases") TestCase testCase) throws IOException, OperatorException {
        RuleGenerator rg = new RuleGenerator();
        rg.setRuleGeneratorParams(testCase.getRuleGeneratorParams());
        RuleSetBase ruleSet = rg.learn(testCase.getExampleSet());


        this.writeReport(testCase, ruleSet);
        RuleSetComparator.assertRulesAreEqual(testCase.getReferenceReport().getRules(), ruleSet.getRules());


        IExampleSet prediction = ruleSet.apply(testCase.getExampleSet());
        ColumnMetaData pa = prediction.getDataTable().getColumnByRole(EColumnRole.prediction.name());
        Assert.assertNotNull(pa);
        Object[] predictionValues = pa.getValues();
        Assert.assertNotNull(predictionValues);
        Assert.assertTrue(predictionValues.length>0);
        Assert.assertTrue(predictionValues instanceof  Double[]);
    }

}