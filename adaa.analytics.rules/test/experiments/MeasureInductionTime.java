package experiments;

import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.RuleSetBase;
import com.rapidminer.RapidMiner;
import adaa.analytics.rules.rm.example.IExampleSet;
import com.rapidminer.gui.tools.dialogs.wizards.dataimport.csv.CSVFileReader;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.LineParser;
import org.junit.Assert;
import org.junit.Test;
import org.renjin.repackaged.guava.base.Strings;
import utils.ArffFileLoader;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MeasureInductionTime {
    private List<String[]> getDatasetDescriptions() throws OperatorException {
        if (!RapidMiner.isInitialized()) {
            RapidMiner.init();
        }

        File datasetsFile = new File(VerificationExperiment.dataDirectory + "_datasets.csv");
        LineParser parser = new LineParser();
        parser.setUseQuotes(true);
        parser.setSplitExpression(LineParser.SPLIT_BY_SEMICOLON_EXPRESSION);
        NumberFormat nf = NumberFormat.getInstance();
        CSVFileReader reader = new CSVFileReader(datasetsFile, true, parser, nf);
        List<String[]> datasets;
        try {
            datasets = reader.readData(200);
        } catch (IOException ex) {
            throw new RuntimeException("Couldn't load dataset description. Reason: " + ex.getMessage());
        }
        return datasets;
    }

    @Test
    public void measure() throws OperatorCreationException, OperatorException, IOException {
        List<String[]> datasets = getDatasetDescriptions();
        List<String> results = new ArrayList<>();
        for(String[] row : datasets) {
            if (row.length < 11)
                continue;
            if (Strings.isNullOrEmpty(row[9]) && Strings.isNullOrEmpty(row[10]))
                continue;
            String file_name = row[0] + ".arff";
            System.out.println(file_name);
            Path path_to_file = Paths.get(VerificationExperiment.dataDirectory, file_name);
            if (!(new File(path_to_file.toString())).exists()) {
                throw new RuntimeException(file_name + "doesn't exists in " + VerificationExperiment.dataDirectory);
            }

            String source_class = row[9];
            String target_class = row[10];
            ArffFileLoader arffFileLoader = new ArffFileLoader();

            IExampleSet wholeData = arffFileLoader.load(Paths.get(path_to_file.toString()), "class");


            ActionFindingParameters afp = new ActionFindingParameters();
            afp.setUseNotIntersectingRangesOnly(ActionFindingParameters.RangeUsageStrategy.NOT_INTERSECTING);

            ClassificationMeasure measure = new ClassificationMeasure(ClassificationMeasure.C2);

            ActionInductionParameters params = new ActionInductionParameters(afp);
            params.setInductionMeasure(measure);
            params.setPruningMeasure(measure);
            params.setVotingMeasure(measure);
            params.setEnablePruning(true);
            params.setIgnoreMissing(true);
            params.setMinimumCovered(5);
            params.setMaximumUncoveredFraction(0.05);
            params.setMaxGrowingConditions(0);
            params.addClasswiseTransition(source_class, target_class);
            ActionFinder af = new ActionFinder(params);

            ActionSnC actionSnC = new ActionSnC(af, params);
            BackwardActionSnC backwardActionSnC = new BackwardActionSnC(af, params);

            Long before = System.currentTimeMillis();
            RuleSetBase f = actionSnC.run(wholeData);
            System.out.println("F: " + f.getRules().size());
            Long after = System.currentTimeMillis();
            Long diff = after - before;

            Long beforeB = System.currentTimeMillis();
            RuleSetBase b = backwardActionSnC.run(wholeData);
            System.out.println("B: " + b.getRules().size());
            Long afterB = System.currentTimeMillis();
            Long diffB = afterB - beforeB;

            results.add(file_name + ";" + diff.toString() + ";" + diffB.toString());
        }

        StringWriter sw = new StringWriter();
        sw.append("dataset;forward;backward\r\n");
        results.forEach(x -> sw.append(x).append("\r\n"));
        Files.write(Paths.get(VerificationExperiment.dataDirectory + "_times.csv"), sw.toString().getBytes(), StandardOpenOption.CREATE);
        Assert.assertEquals(0, 0);
    }
}
