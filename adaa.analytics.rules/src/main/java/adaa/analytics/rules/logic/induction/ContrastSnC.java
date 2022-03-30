package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilter;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.NominalMapping;

import java.util.logging.Level;

public class ContrastSnC extends ClassificationSnC {

    private static double[] MINCOV_ALLS = {0.8, 0.5, 0.2, 0.1};

    //private static double[] MINCOV_ALLS = {0.1};

    public ContrastSnC(AbstractFinder finder, InductionParameters params) {
        super(finder, params);

        int ruleType = RuleFactory.CONTRAST;

        if (finder instanceof  ContrastRegressionFinder) {
            ruleType = RuleFactory.CONTRAST_REGRESSION;
        } else if (finder instanceof  ContrastSurvivalFinder) {
            ruleType = RuleFactory.CONTRAST_SURVIVAL;
        }

        // replace the factory
       this.factory = new RuleFactory(ruleType, true, params, null);
    }

    /**
     * Generates contrast sets on the basis of a training set.
     * @param dataset Training data set.
     * @return Rule set.
     */
    public RuleSetBase run(ExampleSet dataset) {
        ContrastRuleSet rs = (ContrastRuleSet) factory.create(dataset);
        IPenalizedFinder pf = (IPenalizedFinder)finder;

        // reset
        pf.getAttributePenalties().init(dataset);

        // determine if multiple mincov should be used
        double [] mincovs;
        if (params.getMinimumCoveredAll() >= 0) {
            mincovs = new double[] { params.getMinimumCoveredAll() };
        } else {
            mincovs = MINCOV_ALLS;
        }

        for (double mincovAll : mincovs) {
            params.setMinimumCoveredAll(mincovAll);

            run(dataset, rs);

            // reset penalty when multiple passes
            if (params.getMaxPassesCount() > 1) {
                pf.getAttributePenalties().reset();
            }
        }

        return rs;
    }

    /**
     * Generates contrast sets on the basis of a training set.
     * @param dataset Training data set.
     * @return Rule set.
     */
    public void run(ExampleSet dataset, ContrastRuleSet crs) {
        Logger.log("ContrastSnC.run()\n", Level.FINE);

        // try to get contrast attribute (use label if not specified)
        final Attribute contrastAttr = (dataset.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? dataset.getAttributes().getLabel()
                : dataset.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

        NominalMapping mapping = contrastAttr.getMapping();
        IPenalizedFinder pf = (IPenalizedFinder)finder;

        try {
            // one-vs-all mode
            Logger.log("Contrast one vs others:\n" , Level.INFO);
            RuleSetBase rs;

            // multipass loop
            for (int pass = 0; pass < params.getMaxPassesCount() ; ++pass) {
                rs = super.run(dataset);

                // disable compensation after first covering
                pf.getAttributePenalties().disableCompensation();

                int previousCount = crs.getRules().size();
                for (Rule r : rs.getRules()) {
                    // fixme - ugly injection
                    ContrastRule cr = (ContrastRule) r;
                    cr.putStat("mincov_all", params.getMinimumCoveredAll());
                    cr.putStat("pass_number", pass + 1);

                    int groupId = (int) ((SingletonSet) cr.getConsequence().getValueSet()).getValue();
                    crs.add(mapping.mapIndex(groupId), ContrastRuleSet.OTHER_CLASSES, cr);
                }
                // break when no new CS were induced
                if (crs.getRules().size() == previousCount) {
                    break;
                }
            }

            // one-vs-one mode only in multinomial problems
            if (mapping.size() > 2 && params.isBinaryContrastIncluded()) {

                for (int i = 0; i < mapping.size(); ++i) {
                    for (int j = i + 1; j < mapping.size(); ++j) {
                        // one-vs-all mode
                        String group1 = mapping.mapIndex(i);
                        String group2 = mapping.mapIndex(j);

                        Logger.log("\nContrast " + group1 +" vs " + group2 + "\n" , Level.INFO);

                        String conditionString = contrastAttr.getName() + " = " + mapping.mapIndex(i) + " || " +
                                contrastAttr.getName() + " = " + mapping.mapIndex(j);

                        AttributeValueFilter cnd = new AttributeValueFilter(dataset, conditionString);
                        ExampleSet conditionedSet = new ConditionedExampleSet(dataset, cnd);
                        rs = super.run(conditionedSet);

                        for (Rule r : rs.getRules()) {
                            // fixme - ugly injection
                            ContrastRule cr = (ContrastRule)r;
                            cr.putStat("mincov_all", params.getMinimumCoveredAll());

                            if (r.getConsequence().getValueSet().contains(i)) {
                                crs.add(group1, group2, cr);
                            } else {
                                crs.add(group2, group1, cr);
                            }
                        }
                    }
                }

            }
       } catch (Exception e) {
           e.printStackTrace();
       }
    } 
}
