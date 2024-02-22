//package experiments;
//
//import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
//import adaa.analytics.rules.logic.actions.recommendations.RecommendationTask;
//import adaa.analytics.rules.logic.induction.*;
//import adaa.analytics.rules.logic.quality.ClassificationMeasure;
//import adaa.analytics.rules.rm.example.IAttribute;
//import adaa.analytics.rules.rm.example.Example;
//import adaa.analytics.rules.rm.example.IExampleSet;
//
//public class ExperimentTask {
//    public static class ExampleSetEvaluation {
//        public double sourceClassAcc;
//        public double targetClassAcc;
//        public double balancedAcc;
//        public int sourceExamplesCount;
//        public int targetExamplesCount;
//        public int correctSourcePredictions;
//        public int correctTargetPredictions;
//        public ExampleSetEvaluation(IExampleSet exampleSet, FileDescription dataFileDesc) {
//            IAttribute label = exampleSet.getAttributes().getLabel();
//            IAttribute pred = exampleSet.getAttributes().getPredictedLabel();
//            int sourceId = label.getMapping().getIndex(dataFileDesc.getSourceClass());
//            int targetId = label.getMapping().getIndex(dataFileDesc.getTargetClass());
//            correctTargetPredictions = 0;
//            correctSourcePredictions = 0;
//            sourceExamplesCount = 0;
//            for (Example ex : exampleSet) {
//
//                if (Double.compare(ex.getValue(label), sourceId) == 0) {
//                    sourceExamplesCount += 1;
//                }
//
//                if (Double.compare(ex.getValue(label), ex.getValue(pred)) == 0) {
//                    if (Double.compare(ex.getValue(label), sourceId) == 0) {
//                        correctSourcePredictions += 1;
//                    } else if (Double.compare(ex.getValue(label), targetId) == 0) {
//                        correctTargetPredictions += 1;
//                    }
//                }
//            }
//
//            targetExamplesCount = exampleSet.size() - sourceExamplesCount;
//            sourceClassAcc = (double)correctSourcePredictions / sourceExamplesCount;
//            targetClassAcc = (double)correctTargetPredictions / targetExamplesCount;
//
//            balancedAcc = (sourceClassAcc + targetClassAcc) / 2.0;
//        }
//
//    }
//
//    public AbstractSeparateAndConquer getActionSnC(ActionInductionParameters params) {
//        return new ActionSnC(new ActionFinder(params), params);
//    }
//
//    public ActionSnC getBackwardActionSnC(ActionInductionParameters params) {
//        return new BackwardActionSnC(new ActionFinder(params), params);
//    }
//
//    public AbstractSeparateAndConquer getRuleSnc(InductionParameters params) {
//        return new ClassificationSnC(new ClassificationFinder(params), params);
//    }
//
//    public void preprocessParams(ActionInductionParameters params, FileDescription file) {
//        params.addClasswiseTransition(file.getSourceClass(), file.getTargetClass());
//    }
//
//    public ActionInductionParameters createParamsObject(ActionFindingParameters findingParameters) {
//        return new ActionInductionParameters(findingParameters);
//    }
//
//    public RecommendationTask getRecommendationTask(ActionInductionParameters params, FileDescription file, IExampleSet set, ClassificationMeasure measure) {
//        IAttribute label = set.getAttributes().getLabel();
//        int sourceId = label.getMapping().getIndex(file.getSourceClass());
//        int targetId = label.getMapping().getIndex(file.getTargetClass());
//        return new ClassificationRecommendationTask(params.isPruningEnabled(), false, measure, sourceId, targetId);
//    }
//
//}
