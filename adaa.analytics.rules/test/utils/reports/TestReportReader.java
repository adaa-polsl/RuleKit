package utils.reports;

import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleParser;
import com.rapidminer.operator.ports.metadata.ExampleSetMetaData;
import org.apache.xmlbeans.impl.piccolo.io.FileFormatException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestReportReader {

    private BufferedReader reader;
    private String fileName;
    private ExampleSetMetaData exampleSetMetaData;

    public TestReportReader(String fileName, ExampleSetMetaData exampleSetMetaData) throws IOException {
        this.exampleSetMetaData = exampleSetMetaData;
        this.fileName = fileName;
        reader = new BufferedReader(new FileReader(fileName));
    }

    private void readRules(TestReport report) throws IOException {
        String line;
        List<Rule> rules = new ArrayList<>();
        while ((line = reader.readLine()) != null) {
            if (line.length() == 0) break;
            rules.add(RuleParser.parseRule(line.replace("\t", ""), exampleSetMetaData));
        }
        report.setRules(rules);
    }

    public TestReport read() throws IOException {
        TestReport report = new TestReport(fileName);
        String line;
        while ((line = reader.readLine()) != null) {
            switch(line.replace("\t", "")) {
                case Const.REPORTS_SECTIONS_HEADERS.RULES:
                    readRules(report);
                    break;
                case "":
                    continue;
                default:
                    throw new FileFormatException("Invalid report file format for file: " + fileName);
            }
        }
        return report;
    }

    public void close() throws IOException {
        reader.close();
    }
}
