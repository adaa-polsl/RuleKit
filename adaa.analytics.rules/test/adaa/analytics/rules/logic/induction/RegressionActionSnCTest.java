package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
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

import static org.junit.Assert.*;
@RunWith(Theories.class)
public class RegressionActionSnCTest {
    private static final String CLASS_NAME = RegressionActionSnCTest.class.getSimpleName();
    private static final String REPORTS_DIRECTORY = Const.REPORTS_IN_DIRECTORY_PATH + CLASS_NAME + '/';

    @DataPoints("Test cases")
    public static TestCase[] getTestCases() throws ParseException {
        String configFilePath = Const.CONFIG_DIRECTORY + CLASS_NAME + ".xml";
        configFilePath = TestResourcePathFactory.get(configFilePath).toString();
        HashMap<String, TestConfig> configs = new TestConfigParser().parse(configFilePath);
        List<TestCase> testCases = TestCaseFactory.make(configs, REPORTS_DIRECTORY);
        return testCases.toArray(new TestCase[0]);
    }

    public RegressionActionSnCTest() {
        File directory = new File(Const.REPORTS_OUT_DIRECTORY_PATH + CLASS_NAME);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    private void writeReport(String reportName, RuleSetBase ruleSet) {
        try {
            TestReportWriter reportWriter = new TestReportWriter(CLASS_NAME + '/' + reportName);
            reportWriter.write(ruleSet);
            reportWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Theory
    public void runTestCase(@FromDataPoints("Test cases") TestCase testCase) throws OperatorException, OperatorCreationException, IOException {
        InductionParameters params = testCase.getParameters();
        ActionFindingParameters afp = new ActionFindingParameters();
        afp.setUseNotIntersectingRangesOnly(ActionFindingParameters.RangeUsageStrategy.ALL);
        RegressionActionInductionParameters aParams = new RegressionActionInductionParameters(afp);
        aParams.setMaximumUncoveredFraction(params.getMaximumUncoveredFraction());
        aParams.setMaxGrowingConditions(params.getMaxGrowingConditions());
        aParams.setInductionMeasure(params.getInductionMeasure());
        aParams.setPruningMeasure(params.getPruningMeasure());
        aParams.setIgnoreMissing(params.isIgnoreMissing());
        aParams.setMinimumCovered(params.getMinimumCovered());
        aParams.setVotingMeasure(params.getVotingMeasure());
        aParams.setEnablePruning(false);
        aParams.setRegressionOrder(RegressionActionInductionParameters.RegressionOrder.BETTER);
        params.setEnablePruning(false);

        RegressionFinder regressionFinder = new RegressionFinder(params);
        RegressionSnC regressionSnC = new RegressionSnC(regressionFinder, params);
        RuleSetBase regressionRuleSet = regressionSnC.run(testCase.getExampleSet());

        RegressionActionFinder finder = new RegressionActionFinder(aParams);
        RegressionActionSnC snc = new RegressionActionSnC(finder, aParams);
        RuleSetBase ruleSet = snc.run(testCase.getExampleSet());

        for (Rule rule : ruleSet.getRules()){
            RegressionActionRule rar = (RegressionActionRule)rule;

            Action consequence = (Action)rar.getConsequence();
            double source = ((SingletonSet)consequence.getLeftValue()).getValue();
            double target = ((SingletonSet)consequence.getRightValue()).getValue();
          //  assertTrue(Double.compare(target, source) <= 0);
        }

        this.writeReport(testCase.getName(), ruleSet);
       // RuleSetComparator.assertRulesAreEqual(testCase.getReferenceReport().getRules(), ruleSet.getRules());
    }
}