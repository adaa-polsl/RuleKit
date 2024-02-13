package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.rm.example.IExampleSet;

public class ApproximateClassificationSnC extends ClassificationSnC {
    public ApproximateClassificationSnC(AbstractFinder finder, InductionParameters params) {
        super(finder, params);
        numClassThreads = 1; // fix number of class threads
    }

    @Override
    public void preprocessClass(IExampleSet dataset, int classId) {
        ApproximateClassificationFinder apx = (ApproximateClassificationFinder)finder;
        apx.resetArrays(dataset, classId);
    }
}
