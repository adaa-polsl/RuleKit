package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.data.condition.AbstractCondition;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.condition.StringCondition;
import adaa.analytics.rules.logic.representation.exampleset.ContrastExampleSet;
import adaa.analytics.rules.logic.representation.exampleset.ContrastRegressionExampleSet;
import adaa.analytics.rules.logic.representation.exampleset.ContrastSurvivalExampleSet;
import adaa.analytics.rules.logic.representation.ruleset.ContrastRuleSet;
import adaa.analytics.rules.logic.representation.ruleset.RuleSetBase;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.INominalMapping;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.logic.representation.rule.Rule;
import adaa.analytics.rules.logic.representation.valueset.SingletonSet;
import adaa.analytics.rules.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ContrastSnC extends ClassificationSnC {

    public ContrastSnC(AbstractFinder finder, InductionParameters params) {
        super(finder, params);

        int ruleType = RuleFactory.CONTRAST;

        if (finder instanceof ContrastRegressionFinder) {
            ruleType = RuleFactory.CONTRAST_REGRESSION;
        } else if (finder instanceof  ContrastSurvivalFinder) {
            ruleType = RuleFactory.CONTRAST_SURVIVAL;
        }

        // replace the factory
       this.factory = new RuleFactory(ruleType,  params, null);
    }

    /**
     * Generates contrast sets on the basis of a training set.
     * @param dataset Training data set.
     * @return Rule set.
     */
    public RuleSetBase run(IExampleSet dataset) {

        // make a contrast dataset
        ContrastExampleSet ces;

        if (factory.getType() == RuleFactory.CONTRAST_REGRESSION) {
            ces = new ContrastRegressionExampleSet( dataset);
        } else if (factory.getType() == RuleFactory.CONTRAST_SURVIVAL) {
            ces = new ContrastSurvivalExampleSet(dataset);
        } else {
            ces = new ContrastExampleSet( dataset);
        }

        ContrastRuleSet rs = (ContrastRuleSet) factory.create(ces);
        IPenalizedFinder pf = (IPenalizedFinder)finder;

        // reset penalties
        pf.getAttributePenalties().init(dataset);

        // determine if multiple mincov should be used
        List<Double> mincovs;
        if (params.getMinimumCoveredAll_list().size() == 0) {
            mincovs = new ArrayList<Double>();
            mincovs.add(params.getMinimumCoveredAll());
        } else {
            mincovs = params.getMinimumCoveredAll_list();
        }

        for (double mincovAll : mincovs) {
            params.setMinimumCoveredAll(mincovAll);

            run(ces, rs);

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
    protected void run(ContrastExampleSet dataset, ContrastRuleSet crs) {
        Logger.log("ContrastSnC.run()\n", Level.FINE);

        // try to get contrast attribute (use label if not specified)
        final IAttribute contrastAttr = dataset.getContrastAttribute();

        INominalMapping mapping = contrastAttr.getMapping();
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

//                        String conditionString = contrastAttr.getName() + " = " + mapping.mapIndex(i) + " || " +
//                                contrastAttr.getName() + " = " + mapping.mapIndex(j);
//
//                        AttributeValueFilter cnd = new AttributeValueFilter(dataset, conditionString);
//                        IExampleSet conditionedSet = new ConditionedExampleSet(dataset, cnd);

                        List<ICondition> cndList = new ArrayList<>(2);
                        cndList.add(new StringCondition(contrastAttr.getName(), AbstractCondition.EComparisonOperator.EQUALS, (double)i));
                        cndList.add(new StringCondition(contrastAttr.getName(), AbstractCondition.EComparisonOperator.EQUALS, (double)j));
                        IExampleSet conditionedSet = dataset.filterWithOr(cndList);

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
