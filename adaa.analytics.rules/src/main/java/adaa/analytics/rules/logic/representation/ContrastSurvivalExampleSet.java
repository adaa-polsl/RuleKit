package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.data.condition.AbstractCondition;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.condition.StringCondition;
import adaa.analytics.rules.rm.comp.TsExampleSet;
import adaa.analytics.rules.rm.example.IExampleSet;
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

    public ContrastSurvivalExampleSet(TsExampleSet exampleSet) {
        super(exampleSet);

        // establish training survival estimator
        trainingEstimator = new KaplanMeierEstimator(exampleSet);

        // establish contrast groups survival estimator
        try {
            INominalMapping mapping = contrastAttribute.getMapping();

            for(String value : mapping.getValues()) {
                ICondition cnd = new StringCondition(contrastAttribute.getName(), AbstractCondition.EComparisonOperator.EQUALS, value);
                IExampleSet conditionedSet = exampleSet.filter(cnd);
                groupEstimators.add(new KaplanMeierEstimator(conditionedSet));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
