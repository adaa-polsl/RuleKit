package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.representation.Knowledge;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import utils.ArffFileLoader;
import utils.config.KnowledgeFactory;
import utils.reports.TestReport;
import utils.reports.TestReportReader;

import java.io.IOException;
import java.util.HashMap;

public class TestCase {
    private HashMap<String, Object> parametersConfigs;
    private TestReport referenceReport;
    private ExampleSet exampleSet;
    private InductionParameters parameters;
    private Knowledge knowledge;

    private String name;
    private String dataSetFilePath;
    private String labelAttribute;
    private String survivalTime;
    private String reportFilePath;

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

    public ExampleSet getExampleSet() throws OperatorException, OperatorCreationException {
        if (exampleSet == null) {
            if (survivalTime != null) {
                this.exampleSet = ArffFileLoader.load(dataSetFilePath, labelAttribute, survivalTime);
            } else {
                this.exampleSet = ArffFileLoader.load(dataSetFilePath, labelAttribute);
            }
        }
        return exampleSet;
    }

    public TestReport getReferenceReport() throws OperatorException, OperatorCreationException, IOException {
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

    public Knowledge getKnowledge() throws OperatorException, OperatorCreationException {
        if (knowledge == null) {
            ExampleSet exampleSet = getExampleSet();
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
