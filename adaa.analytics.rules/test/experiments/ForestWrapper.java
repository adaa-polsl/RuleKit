package experiments;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.OperatorVersion;
import com.rapidminer.operator.learner.tree.RandomForestLearner;
import com.rapidminer.tools.OperatorService;

public class ForestWrapper extends ForestWrapperBase {

    @SuppressWarnings("deprecation")
    private RandomForestLearner _learner;

    public ForestWrapper(int numberOfTrees) throws OperatorCreationException {
        super();
        _learner = OperatorService.createOperator(RandomForestLearner.class);
        _learner.setCompatibilityLevel(new OperatorVersion("6.5.000"));
        _learner.setParameter(RandomForestLearner.PARAMETER_NUMBER_OF_TREES, Integer.toString(numberOfTrees));
        _learner.setParameter(RandomForestLearner.PARAMETER_MAXIMAL_DEPTH, Integer.toString(20));
        _learner.setParameter(RandomForestLearner.PARAMETER_CRITERION, "gain_ratio");
        _learner.setParameter(RandomForestLearner.PARAMETER_NO_PRE_PRUNING, "false");
        _learner.setParameter(RandomForestLearner.PARAMETER_MINIMAL_GAIN, Double.toString(0.1));
        _learner.setParameter(RandomForestLearner.PARAMETER_MINIMAL_LEAF_SIZE, Integer.toString(2));
        _learner.setParameter(RandomForestLearner.PARAMETER_MINIMAL_SIZE_FOR_SPLIT, Integer.toString(4));
        _learner.setParameter(RandomForestLearner.PARAMETER_NUMBER_OF_PREPRUNING_ALTERNATIVES, Integer.toString(3));
        _learner.setParameter(RandomForestLearner.PARAMETER_NO_PRUNING, "false");
        _learner.setParameter(RandomForestLearner.PARAMETER_CONFIDENCE, Double.toString(0.25));
        _learner.setParameter(RandomForestLearner.PARAMETER_USE_HEURISTIC_SUBSET_RATION, "true");


    }

    @Override
    public ExampleSet learn(ExampleSet trainSet) throws OperatorException {
        _forest = _learner.learn(trainSet);
        return _forest.apply(trainSet);
    }

    @Override
    public ExampleSet predict(ExampleSet testSet) throws OperatorException {
        if (_forest == null) {
            throw new RuntimeException("Unitialized forest used");
        }
        return _forest.apply(testSet);
    }

}
