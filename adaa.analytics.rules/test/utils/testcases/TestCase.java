package utils.testcases;

import adaa.analytics.rules.logic.induction.InductionParameters;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import utils.ArffFileLoader;
import utils.reports.TestReport;
import utils.reports.TestReportReader;

import java.io.IOException;

public class TestCase {
    private TestReport referenceReport;
    private ExampleSet exampleSet;
    private InductionParameters parameters;

    private String name;
    private String dataSetFilePath;
    private String labelAttribute;
    private String reportFilePath;

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
            this.exampleSet = ArffFileLoader.load(dataSetFilePath, labelAttribute);
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

    @Override
    public String toString() {
        return this.name;
    }
}
