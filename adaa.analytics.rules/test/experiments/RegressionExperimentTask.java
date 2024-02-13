package experiments;

import adaa.analytics.rules.logic.actions.recommendations.RecommendationTask;
import adaa.analytics.rules.logic.actions.recommendations.RegressionRecommendationTask;
import adaa.analytics.rules.logic.induction.*;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.apache.commons.lang.math.DoubleRange;
import org.apache.commons.lang.math.Range;


public class RegressionExperimentTask extends ExperimentTask {

    public static class RegressionExampleSetEvaluation  {

        private double MSE;
        private int wellPredictedCount = 0;

        public RegressionExampleSetEvaluation(IExampleSet exampleSet) {
            final int n = exampleSet.size();
            double[] labelValues = new double[n];

            for (int i = 0 ; i < n; i++){
                Example ex = exampleSet.getExample(i);
                labelValues[i] = ex.getLabel();
            }

            double sum = 0.0;
            double stddevOfLabel =  Math.sqrt(org.apache.commons.math3.stat.StatUtils.populationVariance(labelValues));
            for (int i = 0; i < n; i++) {
                Example ex = exampleSet.getExample(i);
                double label = ex.getLabel();
                double pred = ex.getPredictedLabel();

                double wellMin = label - stddevOfLabel;
                double wellMax = label + stddevOfLabel;

                Range range = new DoubleRange(wellMin, wellMax);

                if (range.containsDouble(pred)) {
                    wellPredictedCount++;
                }

                double error = label - pred;
                sum += error * error;
            }


            MSE = 0.0;
            MSE = sum / n;
        }

        public double getMSE() { return MSE; }
        public double getRMSE() { return Math.sqrt(MSE);}
        public int getWellPredictedCount() { return wellPredictedCount; }
    }

    public AbstractSeparateAndConquer getActionSnC(ActionInductionParameters params) {
        RegressionActionInductionParameters raip = (RegressionActionInductionParameters)params;
        raip.setRegressionOrder(RegressionActionInductionParameters.RegressionOrder.ANY);
        return new RegressionActionSnC(new RegressionActionFinder(raip), raip);
    }

    public ActionSnC getBackwardActionSnC(ActionInductionParameters params) {
        return new BackwardActionSnC(new ActionFinder(params), params);
    }

    public AbstractSeparateAndConquer getRuleSnc(InductionParameters params) {
        return new RegressionSnC(new RegressionFinder(params), params);
    }

    public void preprocessParams(ActionInductionParameters params, FileDescription file) {
        RegressionFileDescription rfd = (RegressionFileDescription)file;
        ((RegressionActionInductionParameters)params).setCanOverlapConsequences(rfd.getCanOverlapConsequences());
    }

    public ActionInductionParameters createParamsObject(ActionFindingParameters findingParameters) {
        return new RegressionActionInductionParameters(findingParameters);
    }

    public RecommendationTask getRecommendationTask(ActionInductionParameters params, FileDescription file, IExampleSet set, ClassificationMeasure measure) {

        return new RegressionRecommendationTask(params.isPruningEnabled(), false, measure, RegressionActionInductionParameters.RegressionOrder.ANY);

    }
}
