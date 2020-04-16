package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import org.junit.Before;
import org.junit.Test;
import utils.ArffFileLoader;
import utils.RuleSetComparator;
import utils.TestResourcePathFactory;
import utils.reports.Const;
import utils.reports.TestReport;
import utils.reports.TestReportReader;
import utils.reports.TestReportWriter;

import java.io.IOException;

public class ClassificationSnCTest {

    private static final double MAX_UNCOVERED_FRACTION = 0.0;
    private static final double MIN_RULE_COVERED = 8.0;
    private static final double MAX_GROWING = 0.0;
    private static final boolean PRUNING_ENABLED = true;
    private static final boolean IGNORE_MISSING = false;

    private static final String TRAIN_DATA_FILE = TestResourcePathFactory.getResourcePath("deals-train.arff").toString();
    private static final String LABEL_ATTRIBUTE = "Future Customer";
    private static final String REPORT_FILE_NAME = ClassificationSnCTest.class.getSimpleName() + ".txt";

    private ExampleSet exampleSet;
    private TestReport referenceReport;

    @Before
    public void setUp() throws Exception {
        this.exampleSet = ArffFileLoader.load(TRAIN_DATA_FILE, LABEL_ATTRIBUTE);
        String reportFilePath = TestResourcePathFactory
                .getResourcePath(Const.REPORTS_IN_DIRECTORY_PATH + REPORT_FILE_NAME).toString();
        TestReportReader reportReader = new TestReportReader(reportFilePath, new ExampleSetMetaData(exampleSet));
        referenceReport = reportReader.read();
        reportReader.close();
    }

    private void writeReport(RuleSetBase ruleSet) {
        try {
            TestReportWriter reportWriter = new TestReportWriter(REPORT_FILE_NAME);
            reportWriter.write(ruleSet);
            reportWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRun() {
        InductionParameters params = new InductionParameters();
        params.setMaximumUncoveredFraction(MAX_UNCOVERED_FRACTION);
        params.setMinimumCovered(MIN_RULE_COVERED);
        params.setEnablePruning(PRUNING_ENABLED);
        params.setIgnoreMissing(IGNORE_MISSING);
        params.setMaxGrowingConditions(MAX_GROWING);

        ClassificationFinder finder = new ClassificationFinder(params);
        ClassificationSnC snc = new ClassificationSnC(finder, params);

        RuleSetBase ruleSet = snc.run(exampleSet);
        this.writeReport(ruleSet);

        RuleSetComparator.assertRulesAreEqual(referenceReport.getRules(), ruleSet.getRules());
    }
}