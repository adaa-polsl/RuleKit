package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.data.IExampleSet;

public class ApproximateClassificationSnC extends ClassificationSnC {
    public ApproximateClassificationSnC(AbstractFinder finder, InductionParameters params) {
        super(finder, params);
    }

    @Override
    public void preprocessClass(IExampleSet dataset, int classId) {
        ApproximateClassificationFinder apx = (ApproximateClassificationFinder)finder;
        apx.resetArrays(dataset, classId);
    }
}
