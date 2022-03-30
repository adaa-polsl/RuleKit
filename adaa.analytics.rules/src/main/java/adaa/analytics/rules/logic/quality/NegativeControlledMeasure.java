package adaa.analytics.rules.logic.quality;

import adaa.analytics.rules.logic.induction.ContingencyTable;
import com.rapidminer.example.ExampleSet;
import org.apache.lucene.search.IndexOrDocValuesQuery;

import java.io.Serializable;

public class NegativeControlledMeasure implements IQualityMeasure, Serializable {

    private static final long serialVersionUID = 8739904892582920942L;

    protected IQualityMeasure internalMeasure;

    protected double maxCovNeg;

    public NegativeControlledMeasure(IQualityMeasure internal, double maxCovNeg) {
        this.internalMeasure = internal;
        this.maxCovNeg = maxCovNeg;
    }

    public boolean verifyNegativeCoverage(ContingencyTable ct) {
        return verifyNegativeCoverage(ct.weighted_p, ct.weighted_n, ct.weighted_P, ct.weighted_N);
    }

    public boolean verifyNegativeCoverage(double p, double n, double P, double N) {
        double n_limit = maxCovNeg * N * p / P;
        return n <= n_limit;
    }

    @Override
    public String getName() {
        return "Negative controlled " + internalMeasure.getName();
    }

    @Override
    public double calculate(ExampleSet dataset, ContingencyTable ct) {
       if (verifyNegativeCoverage(ct)) {
           return internalMeasure.calculate(dataset, ct);
       } else {
           return Double.NEGATIVE_INFINITY;
       }
    }

    @Override
    public double calculate(double p, double n, double P, double N) {
        if (verifyNegativeCoverage(p,n,P,N)) {
            return internalMeasure.calculate(p,n,P,N);
        } else {
            return Double.NEGATIVE_INFINITY;
        }
    }
}
