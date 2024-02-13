package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;

public class CorrectPredictionCondition implements ICondition {
    private static final long serialVersionUID = -2971139314612252926L;

    public CorrectPredictionCondition() {
    }

    public CorrectPredictionCondition(IExampleSet exampleSet, String parameterString)
//            throws ConditionCreationException
    {
        boolean missingLabel = exampleSet.getAttributes().getLabel() == null;
        boolean missingPrediction = exampleSet.getAttributes().getPredictedLabel() == null;
//        if (missingLabel && missingPrediction) {
//            throw new ConditionCreationException(I18N.getErrorMessage("CorrectPredictionCondition.missing_label_and_prediction", new Object[0]));
//        } else if (missingLabel) {
//            throw new ConditionCreationException(I18N.getErrorMessage("CorrectPredictionCondition.missing_label", new Object[0]));
//        } else if (missingPrediction) {
//            throw new ConditionCreationException(I18N.getErrorMessage("CorrectPredictionCondition.missing_prediction", new Object[0]));
//        }
    }

    /** @deprecated */
    @Deprecated
    public ICondition duplicate() {
        return this;
    }

    public boolean conditionOk(Example example) {
        return example.equalValue(example.getAttributes().getLabel(), example.getAttributes().getPredictedLabel());
    }
}
