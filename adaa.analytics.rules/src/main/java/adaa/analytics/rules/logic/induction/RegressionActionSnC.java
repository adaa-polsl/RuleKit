package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SortedExampleSet;
import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RegressionActionSnC extends AbstractSeparateAndConquer {
    private RegressionActionFinder _finder;

    public RegressionActionSnC(RegressionActionFinder finder, final InductionParameters params) {
        super(params);
        _finder = finder;
        factory = new RuleFactory(RuleFactory.REGRESSION_ACTION, true, params, null);
    }

    @Override
    public RuleSetBase run(ExampleSet trainSet) {
        Logger.log("RegressionSnC.run()\n", Level.FINE);
        double beginTime;
        beginTime = System.nanoTime();

        RuleSetBase ruleset = factory.create(trainSet);
        Attribute label = trainSet.getAttributes().getLabel();
        SortedExampleSet ses = new SortedExampleSet(trainSet, label, SortedExampleSet.INCREASING);
        ses.recalculateAttributeStatistics(ses.getAttributes().getLabel());

        if (factory.getType() == RuleFactory.REGRESSION) {
            double median = ses.getExample(ses.size() / 2).getLabel();
            RegressionRuleSet tmp = (RegressionRuleSet)ruleset;
            tmp.setDefaultValue(median);
        }

        Set<Integer> uncovered = new HashSet<>();
        double weighted_PN = 0;
        // at the beginning rule set does not cover any examples
        for (int id = 0; id < ses.size(); ++id) {
            uncovered.add(id);
            Example ex = ses.getExample(id);
            double w = ses.getAttributes().getWeight() == null ? 1.0 : ex.getWeight();
            weighted_PN += w;
        }

        int totalRules = 0;
        boolean carryOn = true;
        double uncovered_pn = weighted_PN;

        while (carryOn) {
            Logger.log("Uncovered positive weight: " + uncovered_pn +  "/" + weighted_PN + "\n", Level.FINE);

            Rule rule = factory.create(
                    new CompoundCondition(),
                    new Action(label.getName(), new SingletonSet(Double.NaN, null), new SingletonSet(Double.NaN, null)));

            rule.setCoveredPositives(new IntegerBitSet(trainSet.size()));
            rule.setCoveredNegatives(new IntegerBitSet(trainSet.size()));
            //initially, rule covers everything, so all uncovered by the ruleset at the beggining
            //but the "uncovered" gets updated over the time, so we can't use it...
            // the sequence of ids of all examples should be sufficient
            rule.getCoveredPositives().addAll(IntStream.rangeClosed(0, ses.size()-1).boxed().collect(Collectors.toList()));
            rule.getCoveredNegatives().addAll(IntStream.rangeClosed(0, ses.size()-1).boxed().collect(Collectors.toList()));

            double t = System.nanoTime();
            carryOn = (_finder.grow(rule, ses, uncovered) > 0);
            ruleset.setGrowingTime( ruleset.getGrowingTime() + (System.nanoTime() - t) / 1e9);
            rule.setCoveringInformation(rule.covers(ses));
            if (carryOn) {
                if (params.isPruningEnabled()) {
                    Logger.log("Before prunning: " + rule.toString() + "\n" , Level.FINE);
                    t = System.nanoTime();
                    _finder.prune(rule, ses, uncovered);
                    ruleset.setPruningTime( ruleset.getPruningTime() + (System.nanoTime() - t) / 1e9);
                }
                Logger.log("Candidate rule: " + rule.toString() + "\n", Level.FINE);
                Logger.log(".", Level.INFO);

                Covering covered = rule.covers(ses, uncovered);
                rule.setCoveringInformation(covered);
                // remove covered examples
                int previouslyUncovered = uncovered.size();
                uncovered.removeAll(covered.positives);
                uncovered.removeAll(covered.negatives);

                uncovered_pn = 0;
                for (int id : uncovered) {
                    Example e = trainSet.getExample(id);
                    uncovered_pn += trainSet.getAttributes().getWeight() == null ? 1.0 : e.getWeight();
                }

                // stop if number of examples remaining is less than threshold
                if (uncovered_pn <= params.getMaximumUncoveredFraction() * weighted_PN) {
                    carryOn = false;
                }

                // stop and ignore last rule if no new examples covered
                if (uncovered.size() == previouslyUncovered) {
                    carryOn = false;
                } else {
                    ruleset.addRule(rule);
                    Logger.log( "\r" + StringUtils.repeat("\t", 10) + "\r", Level.INFO);
                    Logger.log("\t" + (++totalRules) + " rules" , Level.INFO);
                }
            }
        }

        ruleset.setTotalTime((System.nanoTime() - beginTime) / 1e9);
        return ruleset;
    }
}
