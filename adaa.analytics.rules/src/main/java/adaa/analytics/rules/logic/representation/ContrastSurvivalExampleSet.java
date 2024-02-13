package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.AttributeValueFilterSingleCondition;
import adaa.analytics.rules.rm.example.set.ConditionedExampleSet;
import adaa.analytics.rules.rm.example.set.SimpleExampleSet;
import adaa.analytics.rules.rm.example.table.INominalMapping;

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
            INominalMapping mapping = contrastAttribute.getMapping();

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttribute, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                IExampleSet conditionedSet = new ConditionedExampleSet(exampleSet,cnd);
                groupEstimators.add(new KaplanMeierEstimator(conditionedSet));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
//        catch (ExpressionEvaluationException e) {
//            e.printStackTrace();
//        }
    }
}
