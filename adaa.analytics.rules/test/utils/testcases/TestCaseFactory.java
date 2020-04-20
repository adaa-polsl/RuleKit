package utils.testcases;

import utils.TestResourcePathFactory;
import utils.config.TestConfig;
import utils.config.TestDataSetConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCaseFactory {

    public static List<TestCase> make(HashMap<String, TestConfig> testsConfig, String reportDirectoryPath) {
        List<TestCase> testCases = new ArrayList<>();
        TestConfig testConfig;
        TestCase testCase;
        for (String key : testsConfig.keySet()) {
            testConfig = testsConfig.get(key);
            for (String configName : testConfig.parametersConfigs.keySet()) {
                testCase = new TestCase();
                for (TestDataSetConfig dataSetConfig : testConfig.datasets) {
                    testCase.setParameters(InductionParametersFactory.make(testConfig.parametersConfigs.get(configName)));
                    String dataSetFilePath = TestResourcePathFactory.get(dataSetConfig.trainFileName).toString();
                    testCase.setDataSetFilePath(dataSetFilePath);
                    testCase.setLabelAttribute(dataSetConfig.labelAttribute);
                    String testCaseName = String.format("%s.%s.txt", key, configName);
                    testCase.setName(testCaseName);
                    String reportPath = TestResourcePathFactory.get(reportDirectoryPath + testCaseName).toString();
                    testCase.setReportFilePath(reportPath);
                    testCases.add(testCase);
                }
            }
        }
        return testCases;
    }
}
