package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.List;

public class ContrastSurvivalExampleSet extends SortedExampleSetEx implements IContrastExampleSet {

    protected Attribute contrastAttribute;

    /** Training set estimator. */
    protected KaplanMeierEstimator trainingEstimator;

    /** Collection of Kaplan-Meier estimators for contrast groups. */
    protected List<KaplanMeierEstimator> groupEstimators = new ArrayList<KaplanMeierEstimator>();

    public Attribute getContrastAttribute() { return contrastAttribute; }

    /** Gets {@link #groupEstimators} */
    public List<KaplanMeierEstimator> getGroupEstimators() { return groupEstimators; }

    /** Gets {@link #trainingEstimator}}. */
    public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }

    public ContrastSurvivalExampleSet(SimpleExampleSet exampleSet) {
        super(exampleSet, exampleSet.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE), SortedExampleSetEx.INCREASING);

        contrastAttribute = (exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

        Attribute survTime = exampleSet.getAttributes().getSpecial(SurvivalRule.SURVIVAL_TIME_ROLE);

        // establish training survival estimator
        trainingEstimator = new KaplanMeierEstimator(this);

        // establish contrast groups survival estimator
        try {
            NominalMapping mapping = contrastAttribute.getMapping();

            for (int i = 0; i < mapping.size(); ++i) {
                AttributeValueFilterSingleCondition cnd = new AttributeValueFilterSingleCondition(
                        contrastAttribute, AttributeValueFilterSingleCondition.EQUALS, mapping.mapIndex(i));

                ExampleSet conditionedSet = new ConditionedExampleSet(exampleSet, cnd);
                SortedExampleSetEx cses = new SortedExampleSetEx(conditionedSet, survTime, SortedExampleSet.INCREASING);

                groupEstimators.add(new KaplanMeierEstimator(cses));
            }

        } catch (ExpressionEvaluationException e) {
            e.printStackTrace();
        }
    }

    public ContrastSurvivalExampleSet(ContrastSurvivalExampleSet rhs) {
        super(rhs);
        this.contrastAttribute = rhs.contrastAttribute;
        this.trainingEstimator = rhs.trainingEstimator;
        this.groupEstimators = rhs.groupEstimators;
    }
}
