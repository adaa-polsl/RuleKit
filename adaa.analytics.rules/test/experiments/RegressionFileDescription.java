package experiments;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.*;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import utils.ArffFileLoader;

import java.io.File;
import java.nio.file.Paths;

import static experiments.VerificationExperiment.trainSetPercentage;

public class RegressionFileDescription extends FileDescription {
    RegressionFileDescription() throws OperatorCreationException {
        super();
    }

    protected boolean canConclusionIntersect;

    public boolean getCanOverlapConsequences() {
        return canConclusionIntersect;
    }

    String getPathModificator() {
        if (canConclusionIntersect) return "overlapping_conclusion/";
        else return "";
    }

    public RegressionFileDescription(String fileName, String labelName, boolean interectingConclusions, int folds) throws OperatorException, OperatorCreationException {
        file_name = fileName;
        canConclusionIntersect = interectingConclusions;
        path_to_file = Paths.get(VerificationExperiment.dataDirectory, fileName);
        if (!(new File(path_to_file.toString())).exists()) {
            throw new RuntimeException(fileName + "doesn't exists in " + VerificationExperiment.dataDirectory);
        }

        trainSets = new ExampleSet[folds];
        testSets = new ExampleSet[folds];
        nFolds = folds;

        if (nFolds < 1) {
            throw new IllegalArgumentException("Value of folds must be positive greater than 0");
        }
        ArffFileLoader arffFileLoader = new ArffFileLoader();

        ExampleSet wholeData = arffFileLoader.load(Paths.get(this.getFilePath()), labelName);

        PartitionBuilder partitionBuilder = new ShuffledPartitionBuilder(true, 42);

        final double[] ratios = new double[]{trainSetPercentage, 1-trainSetPercentage};
        final int TRAIN_IDX = 0;
        final int TEST_IDX = 1;
        for (int i = 0; i < nFolds; i++) {
            Partition partition = new Partition(ratios, wholeData.size(), partitionBuilder);
            SplittedExampleSet splitted = new SplittedExampleSet(wholeData, partition);


            splitted.selectSingleSubset(TRAIN_IDX);
            trainSets[i] = new SortedExampleSet(mutator.materializeExamples(splitted), splitted.getAttributes().getLabel(), 0);
            splitted.clearSelection();
            splitted.selectSingleSubset(TEST_IDX);
            testSets[i] = new SortedExampleSet(mutator.materializeExamples(splitted), splitted.getAttributes().getLabel(), 0);
        }
    }
}
