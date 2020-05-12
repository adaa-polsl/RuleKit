package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.quality.LogRank;
import utils.TestResourcePathFactory;
import utils.config.TestConfig;
import utils.config.TestDataSetConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCaseFactory {

    private static final String USE_REPORT_KEY = "use_report";

    private static TestCase makeTestCase(TestConfig testConfig, String testCaseName, HashMap<String, Object> params, TestDataSetConfig dataSetConfig) {
        TestCase testCase = new TestCase();
        InductionParameters inductionParameters = InductionParametersFactory.make(params);
        if (testConfig.survivalTime != null) {
            inductionParameters.setInductionMeasure(new LogRank());
            inductionParameters.setPruningMeasure(new LogRank());
            inductionParameters.setVotingMeasure(new LogRank());
        }
        testCase.setParameters(inductionParameters);
        String dataSetFilePath = TestResourcePathFactory.get(dataSetConfig.trainFileName).toString();
        testCase.setDataSetFilePath(dataSetFilePath);
        testCase.setLabelAttribute(dataSetConfig.labelAttribute);
        testCase.setName(testCaseName);
        testCase.setParametersConfigs(params);

        return testCase;
    }

    public static List<TestCase> make(HashMap<String, TestConfig> testsConfig, String reportDirectoryPath) {
        List<TestCase> testCases = new ArrayList<>();
        TestConfig testConfig;
        TestCase testCase;
        for (String key : testsConfig.keySet()) {
            testConfig = testsConfig.get(key);
            for (String configName : testConfig.parametersConfigs.keySet()) {
                for (TestDataSetConfig dataSetConfig : testConfig.datasets) {
                    HashMap<String, Object> parameters = testConfig.parametersConfigs.get(configName);
                    String testCaseName = String.format("%s.%s.%s.txt", key, configName, dataSetConfig.name);
                    testCase = makeTestCase(testConfig, testCaseName, testConfig.parametersConfigs.get(configName), dataSetConfig);

                    String reportFileName;
                    if (parameters.containsKey(USE_REPORT_KEY)) {
                        reportFileName = (String) parameters.get(USE_REPORT_KEY);
                        reportFileName = String.format("%s.%s.%s.txt", key, reportFileName, dataSetConfig.name);
                        testCase.setUsingExistingReportFile(true);
                    } else {
                        reportFileName = testCaseName;
                        testCase.setUsingExistingReportFile(false);
                    }
                    String reportPath = TestResourcePathFactory.get(reportDirectoryPath + reportFileName).toString();
                    testCase.setReportFilePath(reportPath);
                    testCase.setSurvivalTime(testConfig.survivalTime);

                    testCases.add(testCase);
                }
            }
        }
        return testCases;
    }
}
