package utils.reports;

import adaa.analytics.rules.logic.representation.Rule;
import adaa.analytics.rules.logic.representation.RuleSetBase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class TestReportWriter {

    private PrintWriter printWriter;

    private void createReportsDirectoryIfNotExist() {
        File directory = new File(Const.REPORTS_OUT_DIRECTORY_PATH);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    public TestReportWriter(String fileName) throws IOException {
       createReportsDirectoryIfNotExist();
       printWriter = new PrintWriter(Const.REPORTS_OUT_DIRECTORY_PATH + fileName);
    }

    public void write(RuleSetBase ruleSet) {
        printWriter.println("\n");
        printWriter.println(Const.REPORTS_SECTIONS_HEADERS.RULES);

        for (Rule rule : ruleSet.getRules()) {
            String ruleString = RuleStringFactory.make(rule);
            printWriter.println("\t" + ruleString);
        }
    }

    public void close() {
        printWriter.close();
    }
}
