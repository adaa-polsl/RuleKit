package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.logic.induction.ContingencyTable;

import java.io.Serializable;

public class ContrastRule extends ClassificationRule {

    private static final long serialVersionUID = -8521948425335810974L;

    /**
     * Name of the attribute role used for grouping.
     */
    public static final String CONTRAST_ATTRIBUTE_ROLE = "contrast_attribute";

    protected double redundancy = -1;

    public void setRedundancy(double v) {
        redundancy = v;
    }

    public double getRedundancy() {
        return redundancy;
    }


    /**
     * Creates empty contrast rule.
     */
    public ContrastRule() {
        super();
    }

    /**
     * Creates contrast rule with a given premise and a consequence.
     *
     * @param premise     Rule premise.
     * @param consequence Rule consequence.
     */
    public ContrastRule(CompoundCondition premise, ElementaryCondition consequence) {
        super(premise, consequence);
    }

    @Override
    public String printStats() {
        double prec = weighted_p / (weighted_p + weighted_n);
        double contrast_prec = weighted_n / (weighted_p + weighted_n);
        double supp = weighted_p / weighted_P;
        double contrast_supp = weighted_n / weighted_N;

        String s =
                "(supp=" + (int) weighted_p + "/" + (int) weighted_P + "=" + DoubleFormatter.format(supp) +
                        ", neg_supp=" + (int) weighted_n + "/" + (int) weighted_N + "=" + DoubleFormatter.format(contrast_supp) +
                        ", d_supp=" + DoubleFormatter.format(supp - contrast_supp) +
                        ", neg2pos_supp =" + DoubleFormatter.format(contrast_supp / supp) +
                        ", prec=" + DoubleFormatter.format(prec) +
                        ", d_prec=" + DoubleFormatter.format(prec - contrast_prec) +
                        ", redundancy=" + DoubleFormatter.format(redundancy);


        for (String key : stats.keySet()) {
            s += ", " + key + "=" + stats.get(key);
        }

        s += ")";

        return s;
    }

    @Override
    public String getTableHeader() {
        return super.getTableHeader() + ",redundancy";
    }

    /**
     * Converts a rule to semicolon-separated tabular form with selected statistics.
     *
     * @return Tabular rule representation.
     */
    @Override
    public String toTable() {
        return super.toTable() + ',' + redundancy;
    }
}