package utils.testcases;

import utils.TestResourcePathFactory;
import utils.config.TestConfig;
import utils.config.TestDataSetConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCaseFactory {

    private static TestCase makeTestCase(String testCaseName, HashMap<String, Object> params, TestDataSetConfig dataSetConfig) {
        TestCase testCase = new TestCase();
        testCase.setParameters(InductionParametersFactory.make(params));
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
                    String testCaseName = String.format("%s.%s.txt", key, configName);
                    testCase = makeTestCase(testCaseName, testConfig.parametersConfigs.get(configName), dataSetConfig);
                    String reportPath = TestResourcePathFactory.get(reportDirectoryPath + testCaseName).toString();
                    testCase.setReportFilePath(reportPath);
                    testCases.add(testCase);
                }
            }
        }
        return testCases;
    }
}
