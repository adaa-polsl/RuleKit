package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.representation.rule.ContrastRule;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContrastIndicators {

    public static final String POSITIVE_SUPPORT = "support";
    public static final String NEGATIVE_SUPPORT = "neg_support";
    public static final String DELTA_SUPPORT = "delta_support";
    public static final String RELATIVE_SUPPORT = "neg2pos_support";

    public static final String POSITIVE_PRECISION = "precision";
    public static final String NEGATIVE_PRECISION = "neg_precision";
    public static final String DELTA_PRECISION = "delta_precision";

    public  Map<String, Double> values = new LinkedHashMap<String, Double>();

    public double get(String name) {
        return values.get(name);
    }

    public ContrastIndicators() {
        values.put(POSITIVE_SUPPORT, 0.0);
        values.put(NEGATIVE_SUPPORT, 0.0);
        values.put(DELTA_SUPPORT, 0.0);
        values.put(RELATIVE_SUPPORT, 0.0);

        values.put(POSITIVE_PRECISION, 0.0);
        values.put(NEGATIVE_PRECISION, 0.0);
        values.put(DELTA_PRECISION, 0.0);

    }

    public ContrastIndicators(Iterable<ContrastRule> rules) {
        this();

        int numRules = 0;

        for (ContrastRule r: rules) {
            // support indicators
            double p_supp = r.weighted_p / r.weighted_P;
            double n_supp = r.weighted_n / r.weighted_N;

            values.put(POSITIVE_SUPPORT, values.get(POSITIVE_SUPPORT) + p_supp);
            values.put(NEGATIVE_SUPPORT, values.get(NEGATIVE_SUPPORT) + n_supp);
            values.put(DELTA_SUPPORT,  values.get(DELTA_SUPPORT) +  p_supp - n_supp);
            values.put(RELATIVE_SUPPORT, values.get(RELATIVE_SUPPORT) +  n_supp / p_supp);

            // precision indicators
            double p_prec = r.weighted_p / (r.weighted_p + r.weighted_n);
            double n_prec = r.weighted_n / (r.weighted_p + r.weighted_n);

            values.put(POSITIVE_PRECISION, values.get(POSITIVE_PRECISION) + p_prec);
            values.put(NEGATIVE_PRECISION, values.get(NEGATIVE_PRECISION) + n_prec);
            values.put(DELTA_PRECISION, values.get(DELTA_PRECISION) + p_prec - n_prec);

            ++numRules;
        }

        for (String k : values.keySet()) {
            values.put(k, values.get(k) / numRules);
        }
    }
}
