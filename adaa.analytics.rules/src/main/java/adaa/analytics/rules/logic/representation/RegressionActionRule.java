package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.ActionCovering;
import adaa.analytics.rules.logic.induction.ContingencyTable;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ChiSquareVarianceTest;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.quality.Hypergeometric;
import cern.jet.math.Functions;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.tools.container.Pair;
import org.apache.commons.math.MathException;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import sun.tools.asm.Cover;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RegressionActionRule extends ActionRule {

    double testStatisticValue;

    protected double stddev = 0.0;
    protected double stddev_right = 0.0;

    public RegressionActionRule() {
        super();
    }

    public RegressionActionRule(CompoundCondition premise, Action conclusion) {
        super(premise, conclusion);
    }

    public void calculatePValue(ExampleSet trainSet, ClassificationMeasure measure){
       ChiSquareVarianceTest test = new ChiSquareVarianceTest();

        Rule left  = this.getLeftRule();
        Rule right = this.getRightRule();

        Covering ctLeft = left.covers(trainSet);
        Covering ctRight = right.covers(trainSet);

        double expectedDev = Math.sqrt(trainSet.getStatistics(trainSet.getAttributes().getLabel(), Statistics.VARIANCE));

        Pair<Double, Double> statAndPValueLeft = test.calculateLower(expectedDev, ctLeft.stddev_y, (int)(ctLeft.weighted_p + ctLeft.weighted_n));
        Pair<Double, Double> statAndPValueRight = test.calculateLower(expectedDev, ctRight.stddev_y, (int)(ctRight.weighted_p + ctRight.weighted_n));

        this.weight = measure.calculate(trainSet, ctLeft);
        this.pvalue = statAndPValueLeft.getSecond();
        this.weightRight = measure.calculate(trainSet, ctRight);
        this.pValueRight = statAndPValueRight.getSecond();


        TTest tTest = new TTestImpl();
        double[] sourceVals = ctLeft.positives.stream().map(x -> trainSet.getExample(x).getLabel()).mapToDouble(x->x).toArray();
        double[] targetVals = ctRight.positives.stream().map(x -> trainSet.getExample(x).getLabel()).mapToDouble(x->x).toArray();

        try {
            testStatisticValue = tTest.tTest(sourceVals, targetVals);
        } catch(Exception e) {
            testStatisticValue = Double.NaN;
        }
    }

    public Rule getLeftRule() {

        CompoundCondition premise = new CompoundCondition();
        for (ConditionBase a : this.getPremise().getSubconditions()) {
            if (a.isDisabled()) {
                continue;
            }
            Action ac = (Action)a;
            if (ac.getLeftValue() != null) {
                premise.addSubcondition(new ElementaryCondition(ac.getAttribute(), ac.getLeftValue()));
            }
        }

        Rule r = new RegressionRule(premise, new ElementaryCondition(actionConsequence.getAttribute(), actionConsequence.getLeftValue()));
        r.setWeighted_P(this.getWeighted_P());
        r.setWeighted_N(this.getWeighted_N());

        r.setCoveredPositives(this.getCoveredPositives());
        r.setCoveredNegatives(this.getCoveredNegatives());
        r.setPValue(this.pvalue);
        r.setWeight(this.weight);
        return r;
    }

    public Rule getRightRule() {

        CompoundCondition premise = new CompoundCondition();
        for (ConditionBase a : this.getPremise().getSubconditions()) {
            if (a.isDisabled()){
                continue;
            }
            Action ac = (Action)a;
            if (ac.getRightValue() != null && !ac.getActionNil()) {
                premise.addSubcondition(new ElementaryCondition(ac.getAttribute(), ac.getRightValue()));
            }
        }

        Rule r = new RegressionRule(premise, new ElementaryCondition(actionConsequence.getAttribute(), actionConsequence.getRightValue()));
        r.setWeighted_P(this.getWeighted_N());
        r.setWeighted_N(this.getWeighted_P());

        r.setWeight(this.weightRight);
        r.setPValue(this.pValueRight);
        return r;
    }

    @Override
    public void setCoveringInformation(Covering cov){
        ElementaryCondition cons = this.getConsequence();
        super.setCoveringInformation(cov);
        if (cons instanceof Action) {
            Action conclusion = (Action)cons;
            ((SingletonSet)conclusion.getLeftValue()).setValue(cov.median_y);
            ((SingletonSet)conclusion.getRightValue()).setValue(((ActionCovering) cov).median_y_right);
            this.stddev=cov.stddev_y;
            this.stddev_right = ((ActionCovering) cov).stddev_y_right;
        }
    }

    @Override
    public String toString() {
        double sourceVal = ((SingletonSet)actionConsequence.getLeftValue()).value;
        double targetVal = ((SingletonSet)actionConsequence.getRightValue()).value;
        double loSource = sourceVal - this.stddev;
        double hiSource = sourceVal + this.stddev;
        double loTarget = targetVal - this.stddev_right;
        double hiTarget = targetVal + this.stddev_right;
        return "IF " + premise.toString() + " THEN " + actionConsequence.toString() + " [" + DoubleFormatter.format(loSource) + "," + DoubleFormatter.format(hiSource) + "]"
                + " [" + DoubleFormatter.format(loTarget) + "," + DoubleFormatter.format(hiTarget) + "]";
    }


    private Covering coversCommon(Covering sCovering, Covering tCovering){
        ActionCovering aCov = new ActionCovering();

        aCov.weighted_p = sCovering.weighted_p;
        this.weighted_p = sCovering.weighted_p;
        aCov.weighted_n = sCovering.weighted_n;
        this.weighted_n = sCovering.weighted_n;
        this.weighted_P = sCovering.weighted_P;
        aCov.weighted_P = sCovering.weighted_P;
        this.weighted_N = sCovering.weighted_N;
        aCov.weighted_N = sCovering.weighted_N;
        aCov.positives.addAll(sCovering.positives);
        aCov.negatives.addAll(sCovering.negatives);
        aCov.mean_y = sCovering.mean_y;
        aCov.median_y = sCovering.median_y;
        aCov.stddev_y = sCovering.stddev_y;


        aCov.weighted_pRight = tCovering.weighted_p;
        aCov.weighted_nRight = tCovering.weighted_n;
        aCov.weighted_P_right = tCovering.weighted_P;
        this.weighted_P_right = tCovering.weighted_P;
        aCov.weighted_N_right = tCovering.weighted_N;
        this.weighted_N_right = tCovering.weighted_N;
        aCov.mean_y_right = tCovering.mean_y;
        aCov.median_y_right = tCovering.median_y;
        aCov.stddev_y_right = tCovering.stddev_y;

        return aCov;
    }

    @Override
    public Covering covers(ExampleSet set, Set<Integer> ids){

        Rule source = this.getLeftRule();
        Rule target = this.getRightRule();

        Covering sCovering = source.covers(set, ids);
        Covering tCovering = target.covers(set, ids);

        return coversCommon(sCovering, tCovering);
    }

    @Override
    public Covering covers(ExampleSet set) {
        ActionCovering aCov = new ActionCovering();
        Rule source = this.getLeftRule();
        Rule target = this.getRightRule();

        Covering sCovering = source.covers(set);
        Covering tCovering = target.covers(set);

        return coversCommon(sCovering, tCovering);
    }

    public String printStats() {
        StringBuilder sb = new StringBuilder();
        return prePrintStats(sb)
                .append(", tTest=")
                .append(DoubleFormatter.format(testStatisticValue))
                .append(", sig=")
                .append(Double.isNaN(testStatisticValue) ? "False" : Double.compare(testStatisticValue, 0.05) < 0 ? "True" : "False")
                .append(")").toString();
    }

}
