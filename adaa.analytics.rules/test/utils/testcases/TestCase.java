package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.representation.ExampleSetMetaData;
import adaa.analytics.rules.logic.representation.Knowledge;
import adaa.analytics.rules.rm.example.IExampleSet;
import utils.ArffFileLoader;
import utils.config.KnowledgeFactory;
import utils.reports.TestReport;
import utils.reports.TestReportReader;

import java.io.IOException;
import java.util.HashMap;

public class TestCase {
    private HashMap<String, Object> parametersConfigs;
    private TestReport referenceReport;
    private IExampleSet exampleSet;
    private InductionParameters parameters;
    private Knowledge knowledge;

    private String name;
    private String dataSetFilePath;
    private String labelAttribute;
    private String survivalTime;
    private String reportFilePath;
    private ArffFileLoader arffFileLoader = new ArffFileLoader();

    private boolean usingExistingReportFile;

    public void setDataSetFilePath(String filePath) {
        this.dataSetFilePath = filePath;
    }

    public void setLabelAttribute(String value) {
        labelAttribute = value;
    }

    public void setReportFilePath(String filePath) {
        this.reportFilePath = filePath;
    }

    public InductionParameters getParameters() {
        return parameters;
    }

    public void setParameters(InductionParameters parameters) {
        this.parameters = parameters;
    }

    public IExampleSet getExampleSet() {
        if (exampleSet == null) {
            if (survivalTime != null) {
                this.exampleSet = arffFileLoader.load(dataSetFilePath, labelAttribute, survivalTime);
            } else {
                this.exampleSet = arffFileLoader.load(dataSetFilePath, labelAttribute);
            }
        }
        return exampleSet;
    }

    public TestReport getReferenceReport() throws  IOException {
        if (referenceReport == null) {
           TestReportReader reportReader = new TestReportReader(reportFilePath, new ExampleSetMetaData(getExampleSet()));
           referenceReport = reportReader.read();
           reportReader.close();
        }
        return referenceReport;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParametersConfigs(HashMap<String, Object> parametersConfigs) {
        this.parametersConfigs = parametersConfigs;
    }

    public Knowledge getKnowledge() {
        if (knowledge == null) {
            IExampleSet exampleSet = getExampleSet();
            KnowledgeFactory knowledgeFactory = new KnowledgeFactory(exampleSet);
            knowledge = knowledgeFactory.make(parametersConfigs);
        }
        return knowledge;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public void setSurvivalTime(String survivalTime) {
        this.survivalTime = survivalTime;
    }

    public boolean isUsingExistingReportFile() {
        return usingExistingReportFile;
    }

    public void setUsingExistingReportFile(boolean usingExistingReportFile) {
        this.usingExistingReportFile = usingExistingReportFile;
    }
}
