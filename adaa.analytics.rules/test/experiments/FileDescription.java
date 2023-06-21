package experiments;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.set.StratifiedPartitionBuilder;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import org.renjin.repackaged.guava.io.Files;
import utils.ArffFileLoader;
import utils.Mutator;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static experiments.VerificationExperiment.trainSetPercentage;

class FileDescription {
    protected Path path_to_file;
    protected String source_class;
    protected String target_class;
    protected String file_name;
    protected ExampleSet[] trainSets;
    protected ExampleSet[] testSets;
    protected Mutator mutator = new Mutator();
    int nFolds;

    FileDescription(String fileName, String source, String target, int folds) throws OperatorException, OperatorCreationException {
        file_name = fileName;
        path_to_file = Paths.get(VerificationExperiment.dataDirectory, fileName);
        if (!(new File(path_to_file.toString())).exists()) {
            throw new RuntimeException(fileName + "doesn't exists in " + VerificationExperiment.dataDirectory);
        }
        source_class = source;
        target_class = target;
        trainSets = new ExampleSet[folds];
        testSets = new ExampleSet[folds];
        nFolds = folds;

        if (nFolds < 1) {
            throw new IllegalArgumentException("Value of folds must be positive greater than 0");
        }
        ArffFileLoader arffFileLoader = new ArffFileLoader();

        ExampleSet wholeData = arffFileLoader.load(Paths.get(this.getFilePath()), "class");

        StratifiedPartitionBuilder partitionBuilder = new StratifiedPartitionBuilder(wholeData, true, 42);

        final double[] ratios = new double[]{trainSetPercentage, 1-trainSetPercentage};
        final int TRAIN_IDX = 0;
        final int TEST_IDX = 1;
        for (int i = 0; i < nFolds; i++) {
            Partition partition = new Partition(ratios, wholeData.size(), partitionBuilder);
            SplittedExampleSet splitted = new SplittedExampleSet(wholeData, partition);


            splitted.selectSingleSubset(TRAIN_IDX);
            trainSets[i] = mutator.materializeExamples(splitted);
            splitted.clearSelection();
            splitted.selectSingleSubset(TEST_IDX);
            testSets[i] = mutator.materializeExamples(splitted);
        }
    }

    FileDescription() throws OperatorCreationException {
    }

    String getFileName() { return file_name; }
    String getFileNameWithoutExtension() { return Files.getNameWithoutExtension(file_name); }
    String getFilePath() { return path_to_file.toString(); }
    String getSourceClass() { return source_class; }
    String getTargetClass() { return target_class; }
    int getFoldCount() { return nFolds; }

    String getResultFileName(String qName) {
        return Files.getNameWithoutExtension(path_to_file.toString()) + "-" + qName + ".log";
    }

    String getPathModificator() {return "";}

    ExampleSet getTestSetForFold(int fold) {
        return testSets[fold];
    }
    ExampleSet getTrainSetForFold(int fold) {
        return trainSets[fold];
    }
}
