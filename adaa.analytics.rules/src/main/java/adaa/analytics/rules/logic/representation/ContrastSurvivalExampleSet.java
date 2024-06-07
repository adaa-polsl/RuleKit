package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.List;

public class ContrastSurvivalExampleSet extends ContrastExampleSet {

    /** Training set estimator. */
    protected KaplanMeierEstimator trainingEstimator;

    /** Collection of Kaplan-Meier estimators for contrast groups. */
    protected List<KaplanMeierEstimator> groupEstimators = new ArrayList<KaplanMeierEstimator>();

    /** Gets {@link #groupEstimators} */
    public List<KaplanMeierEstimator> getGroupEstimators() { return groupEstimators; }

    /** Gets {@link #trainingEstimator}}. */
    public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }

    public ContrastSurvivalExampleSet(SimpleExampleSet exampleSet) {
        super(exampleSet);

        // establish training survival estimator
        trainingEstimator = new KaplanMeierEstimator(exampleSet);

        // establish contrast groups survival estimator
        try {
            NominalMapping mapping = contrastAttribute.getMapping();

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttribute, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                ExampleSet conditionedSet = new ConditionedExampleSet(exampleSet, cnd);
                groupEstimators.add(new KaplanMeierEstimator(conditionedSet));
            }

        } catch (ExpressionEvaluationException e) {
            e.printStackTrace();
        }
    }

    public ContrastSurvivalExampleSet(ContrastSurvivalExampleSet rhs) {
        super(rhs);
        this.trainingEstimator = rhs.trainingEstimator;
        this.groupEstimators = rhs.groupEstimators;
    }
}
