package utils.testcases;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetConfiguration;
import utils.TestResourcePathFactory;
import utils.config.TestConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TestCaseFactory {

    public static List<TestCase> make(HashMap<String, TestConfig> testsConfigMap, String reportDirectoryPath) {
        List<TestCase> testCases = new ArrayList<>();
        TestConfig testConfig;
        for (String key : testsConfigMap.keySet()) {
            testConfig = testsConfigMap.get(key);
            for (ParamSetConfiguration paramSetConfiguration: testConfig.paramSetConfigurations) {
                for (DatasetConfiguration dataSetConfig : testConfig.datasets) {
                    String dataSetConfigName = getDatasetConfigName(dataSetConfig);
                    String testCaseName = String.format("%s.%s.%s.txt", key, paramSetConfiguration.getName(), dataSetConfigName);

                    TestCase testCase = new TestCase();
                    testCase.setDatasetConfiguration(dataSetConfig);
                    testCase.setName(testCaseName);
                    testCase.setRuleGeneratorParams( paramSetConfiguration.generateRuleGeneratorParams());

                    String reportPath = TestResourcePathFactory.get(reportDirectoryPath + testCaseName).toString();
                    testCase.setReportFilePath(reportPath);
                    testCases.add(testCase);
                }
            }
        }
        return testCases;
    }

    private static String getDatasetConfigName(DatasetConfiguration dataSetConfig)
    {
        Path path = Paths.get(dataSetConfig.trainElements.get(0).inFile);
        String ret = path.getFileName().toString();
        return ret.substring(0, ret.lastIndexOf('.'));
    }
}
