package utils.testcases;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.logic.representation.ExampleSetMetaData;
import adaa.analytics.rules.logic.rulegenerator.RuleGeneratorParams;
import adaa.analytics.rules.rm.example.IExampleSet;
import utils.ArffFileLoader;
import utils.TestResourcePathFactory;
import utils.reports.TestReport;
import utils.reports.TestReportReader;

import java.io.IOException;

public class TestCase {
    private TestReport referenceReport;
    private IExampleSet exampleSet;
    private RuleGeneratorParams ruleGeneratorParams;
    private DatasetConfiguration datasetConfiguration;
    private String name;
    private String reportFilePath;

    public void setDatasetConfiguration(DatasetConfiguration datasetConfiguration) {
        this.datasetConfiguration = datasetConfiguration;
    }

    public void setReportFilePath(String filePath) {
        this.reportFilePath = filePath;
    }

    public IExampleSet getExampleSet() {
        if (exampleSet == null) {
            ArffFileLoader arffFileLoader = new ArffFileLoader();
            this.exampleSet = arffFileLoader.load(TestResourcePathFactory.get(datasetConfiguration.trainElements.get(0).inFile).toString(), datasetConfiguration.label);
            datasetConfiguration.applyParametersToExempleSet(this.exampleSet);
        }
        return exampleSet;
    }

    public TestReport getReferenceReport() throws IOException {
        if (referenceReport == null) {
            TestReportReader reportReader = new TestReportReader(reportFilePath, new ExampleSetMetaData(getExampleSet()));
            referenceReport = reportReader.read();
            reportReader.close();
        }
        return referenceReport;
    }

    public RuleGeneratorParams getRuleGeneratorParams() {
        return ruleGeneratorParams;
    }

    public void setRuleGeneratorParams(RuleGeneratorParams ruleGeneratorParams) {
        this.ruleGeneratorParams = ruleGeneratorParams;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }


}
