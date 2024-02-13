package experiments;

import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.SortedExampleSet;
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
}
