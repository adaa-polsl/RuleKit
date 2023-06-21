package experiments;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;

public abstract class ForestWrapperBase {

    Model _forest;

    ForestWrapperBase() {    }

    public abstract ExampleSet learn(ExampleSet trainSet) throws OperatorException;

    public abstract ExampleSet predict(ExampleSet testSet) throws OperatorException;
}
