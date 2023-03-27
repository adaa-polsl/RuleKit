package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class ClassificationFinderPrecalculated extends ClassificationFinder {

    /**
     * Map of precalculated coverings (time optimization).
     * For each attribute there is a set of distinctive values. For each value there is a bit vector of examples covered.
     */
    protected Map<Attribute, Map<Double, IntegerBitSet>> precalculatedCoverings;

    /**
     * Map of precalculated attribute filters (time optimization).
     */
    protected Map<Attribute, Set<Double>> precalculatedFilter;

    /**
     * Initializes induction parameters.
     *
     * @param params Induction parameters.
     */
    public ClassificationFinderPrecalculated(InductionParameters params) {
        super(params);
    }

    /**
     * Precalculates conditions coverings and stores them as bit vectors in @see precalculatedCoverings field.
     * @param trainSet Training set.
     */
    @Override
    public void preprocess(ExampleSet trainSet) {

        precalculatedCoverings = new HashMap<Attribute, Map<Double, IntegerBitSet>>();
        precalculatedFilter = new HashMap<Attribute, Set<Double>>();
        Attributes attributes = trainSet.getAttributes();

        ExampleTable table = trainSet.getExampleTable();

        // iterate over all allowed decision attributes
        for (Attribute attr : attributes) {

            Map<Double, IntegerBitSet> attributeCovering = new TreeMap<Double, IntegerBitSet>();
            precalculatedCoverings.put(attr, attributeCovering);
            precalculatedFilter.put(attr, new TreeSet<Double>());

            // check if attribute is nominal
            if (attr.isNominal()) {
                // prepare bit vectors
                for (int val = 0; val != attr.getMapping().size(); ++val) {
                    attributeCovering.put((double)val, new IntegerBitSet(trainSet.size()));
                }

                // get all distinctive values of attribute
                int id = 0;
                for (Example e : trainSet) {
                    DataRow dr = e.getDataRow() ;
                    double value = dr.get(attr);

                    // omit missing values
                    if (!Double.isNaN(value)) {
                        attributeCovering.get(value).add(id);
                    }
                    ++id;
                }
            }

            // check if attribute is numerical or nominal
        /*    if (attr.isNumerical()) {
                Map<Double, List<Integer>> values2ids = new TreeMap<Double, List<Integer>>();

                // get all distinctive values of attribute
                for (int id = 0; id < table.size(); ++id) {
                    // fixme: iterate over ExampleSet
                    DataRow dr = table.getDataRow(id);
                    double val = dr.get(attr);

                    // exclude missing values from keypoints
                    if (!Double.isNaN(val)) {
                        if (!values2ids.containsKey(val)) {
                            values2ids.put(val, new ArrayList<Integer>());
                        }
                        values2ids.get(val).add(id);
                    }
                }

                Double [] keys = values2ids.keySet().toArray(new Double[values2ids.size()]);
                IntegerBitSet covered = new IntegerBitSet(table.size());

                // check all possible midpoints (number of distinctive attribute values - 1)
                // if only one attribute value - ignore it
                for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
                    double key = keys[keyId];

                    double next = keys[keyId + 1];
                    double midpoint = (key + next) / 2;

                    // this condition covers everything covered previously with some extra objects
                    List<Integer> ids = values2ids.get(key);
                    for (int id : ids) {
                        covered.add(id);
                    }

                    attributeCovering.put(midpoint, covered.clone());
                }
            } */
        }
    }


    /**
     * Induces an elementary condition.
     *
     * @param rule Current rule.
     * @param trainSet Training set.
     * @param uncoveredPositives Set of positive examples uncovered by the model.
     * @param coveredByRule Set of examples covered by the rule being grown.
     * @param allowedAttributes Set of attributes that may be used during induction.
     * @param extraParams Additional parameters.
     * @return Induced elementary condition.
     */
    @Override
    protected ElementaryCondition induceCondition(
            Rule rule,
            ExampleSet trainSet,
            Set<Integer> uncoveredPositives,
            Set<Integer> coveredByRule,
            Set<Attribute> allowedAttributes,
            Object... extraParams) {

        if (allowedAttributes.size() == 0) {
            return null;
        }

        double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();
        Set<Integer> positives = rule.getCoveredPositives();
        double apriori_prec = params.isControlAprioriPrecision()
                ? rule.getWeighted_P() / (rule.getWeighted_P() + rule.getWeighted_N())
                : Double.MIN_VALUE;

        List<Future<ConditionEvaluation>> futures = new ArrayList<Future<ConditionEvaluation>>();

        // iterate over all allowed decision attributes
        for (Attribute attr : allowedAttributes) {

            // consider attributes in parallel
            Future<ConditionEvaluation> future = (Future<ConditionEvaluation>) pool.submit(() -> {

                ConditionEvaluation best = new ConditionEvaluation();

                // check if attribute is numerical or nominal
                if (attr.isNumerical()) {
                    // statistics from all points
                    double left_p = 0;
                    double left_n = 0;
                    double right_p = 0;
                    double right_n = 0;

                    // statistics from points yet to cover
                    int toCover_right_p = 0;
                    int toCover_left_p = 0;

                    class TotalPosNeg {
                        double p = 0;
                        double n = 0;
                        int toCover_p = 0;
                    }

                    Map<Double, TotalPosNeg> totals = new TreeMap<Double, TotalPosNeg>();

                    // get all distinctive values of attribute
                    for (int id : coveredByRule) {
                        DataRow dr = trainSet.getExample(id).getDataRow();
                        double val = dr.get(attr);

                        // exclude missing values from keypoints
                        if (!Double.isNaN(val)) {
                            if (!totals.containsKey(val)) {
                                totals.put(val, new TotalPosNeg());
                            }
                            TotalPosNeg tot = totals.get(val);

                            // put to proper bin depending of class label
                            if (positives.contains(id)) {
                                right_p += 1.0;
                                tot.p += 1.0;
                                if (uncoveredPositives.contains(id)) {
                                    ++toCover_right_p;
                                    ++tot.toCover_p;
                                }
                            } else {
                                right_n += 1.0;
                                tot.n += 1.0;
                            }
                        }
                    }

                    Double [] keys = totals.keySet().toArray(new Double[totals.size()]);

                    // check all possible midpoints (number of distinctive attribute values - 1)
                    // if only one attribute value - ignore it
                    for (int keyId = 0; keyId < keys.length - 1; ++keyId) {
                        double key = keys[keyId];

                        double next = keys[keyId + 1];
                        double midpoint = (key + next) / 2;

                        TotalPosNeg tot = totals.get(key);
                        left_p += tot.p;
                        right_p -= tot.p;
                        left_n += tot.n;
                        right_n -= tot.n;
                        toCover_left_p += tot.toCover_p;
                        toCover_right_p -= tot.toCover_p;

                        // calculate precisions
                        double left_prec = left_p / (left_p + left_n);
                        double right_prec = right_p / (right_p + right_n);

                        // evaluate left-side condition: a in (-inf, v)
                        if (left_prec > apriori_prec) {
                            double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
                                    left_p, left_n, rule.getWeighted_P(), rule.getWeighted_N());

                            if ((quality > best.quality || (quality == best.quality && left_p > best.covered)) && (toCover_left_p > 0)) {
                                ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint));
                                if (checkCandidate(candidate, classId, left_p, left_n, toCover_left_p, rule.getWeighted_P())) {
                                    Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + (double)toCover_left_p +", quality="  + quality + "\n", Level.FINEST);
                                    best.quality = quality;
                                    best.covered = left_p;
                                    best.condition = candidate;
                                }
                            }
                        }

                        // evaluate right-side condition: a in <v, inf)
                        if (right_prec > apriori_prec) {
                            double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
                                    right_p, right_n, rule.getWeighted_P(), rule.getWeighted_N());
                            if ((quality > best.quality || (quality == best.quality && right_p > best.covered)) && (toCover_right_p > 0)) {
                                ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint));
                                if (checkCandidate(candidate, classId, right_p, right_n, toCover_right_p, rule.getWeighted_P())) {
                                    Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + (double)toCover_right_p + ", quality="  + quality + "\n", Level.FINEST);
                                    best.quality = quality;
                                    best.covered = right_p;
                                    best.condition = candidate;
                                }
                            }
                        }
                    }
                } else {

                    // try all possible conditions for an attribute
                    for (int i = 0; i < attr.getMapping().size(); ++i) {

                        IntegerBitSet conditionCovered = precalculatedCoverings.get(attr).get((double)i);
                        double p = conditionCovered.calculateIntersectionSize(rule.getCoveredPositives());
                        int toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
                        double n = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule) - p;

                        // evaluate equality condition a = v
                        double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
                                p, n, rule.getWeighted_P(), rule.getWeighted_N());
                        if ((quality > best.quality || (quality == best.quality && p > best.covered)) && (toCover_p > 0)) {
                            ElementaryCondition candidate =
                                    new ElementaryCondition(attr.getName(), new SingletonSet((double)i, attr.getMapping().getValues()));
                            if (checkCandidate(candidate, classId, p, n, toCover_p, rule.getWeighted_P())) {
                                Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double)toCover_p + ", quality="  + quality + "\n", Level.FINEST);
                                best.quality = quality;
                                best.covered = p;
                                best.condition = candidate;
                            }
                        }
                    }
                }

                return best;

            });

            futures.add(future);
        }

        ConditionEvaluation best = null;

        try {
            for (Future f : futures) {
                ConditionEvaluation eval;

                eval = (ConditionEvaluation)f.get();
                if (best == null || eval.quality > best.quality || (eval.quality == best.quality && eval.covered > best.covered)) {
                    best = eval;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (best.condition != null) {
            Attribute bestAttr = trainSet.getAttributes().get(((ElementaryCondition)best.condition).getAttribute());
            if (bestAttr.isNominal()) {
                allowedAttributes.remove(bestAttr);
            }
        }

        return (ElementaryCondition)best.condition;
    }


    /*
    @Override
    protected ElementaryCondition induceCondition(
            Rule rule,
            ExampleSet trainSet,
            Set<Integer> uncoveredPositives,
            Set<Integer> coveredByRule,
            Set<Attribute> allowedAttributes,
            Object... extraParams) {


        double bestQuality = -Double.MAX_VALUE;
        ElementaryCondition bestCondition = null;
        double mostCovered = 0;
        Attribute ignoreCandidate = null;
        double classId = ((SingletonSet)rule.getConsequence().getValueSet()).getValue();

        IntegerBitSet positives = (IntegerBitSet)extraParams[0];

        ExampleTable table = trainSet.getExampleTable();

        // iterate over all allowed decision attributes
        for (Attribute attr : allowedAttributes) {
            Map<Double, IntegerBitSet> attributeCovering = precalculatedCoverings.get(attr);

            Set<Double> ks = attributeCovering.keySet();
            IntegerBitSet previousConditionCovered = null;

            for (Double value : ks) {

                boolean filtered = false;

                if (precalculatedFilter.get(attr).contains(value)) {
                    //	filtered = true;
                    continue;
                }

                IntegerBitSet conditionCovered = attributeCovering.get(value);

                if (attr.isNumerical()) {
                    // numerical attribute

                    // some conditions become equivalent as rule grows (midpoints can be eliminated)
                    if (previousConditionCovered != null && ((IntegerBitSet)coveredByRule).filteredCompare(previousConditionCovered, conditionCovered)) {
                        precalculatedFilter.get(attr).add(value);
                        previousConditionCovered = conditionCovered;
                        //	filtered = true;
                        continue;
                    }

                    previousConditionCovered = conditionCovered;

                    double apriori_prec = rule.getWeighted_P() / (rule.getWeighted_P() + rule.getWeighted_N());

                    double left_p = conditionCovered.calculateIntersectionSize(positives);
                    double toCover_left_p = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
                    double left_n = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule) - left_p;
                    double left_prec = left_p / (left_p + left_n);

                    IntegerBitSet oppositeCover = conditionCovered.clone();
                    oppositeCover.negate();

                    double right_p = oppositeCover.calculateIntersectionSize(positives);
                    double toCover_right_p = oppositeCover.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
                    double right_n =  oppositeCover.calculateIntersectionSize((IntegerBitSet)coveredByRule) - right_p;
                    double right_prec = right_p / (right_p + right_n);

                    // evaluate left-side condition: a in (-inf, v)
                    if (left_prec > apriori_prec && (right_p + right_n != 0)) {
                        double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
                                left_p, left_n, rule.getWeighted_P(), rule.getWeighted_N());

                        if ((quality > bestQuality || (quality == bestQuality && left_p > mostCovered)) && (toCover_left_p > 0)) {
                            ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(value));
                            if (checkCandidate(candidate, classId, rule.getWeighted_P(), toCover_left_p)) {
                                Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + toCover_left_p + ", quality="  + quality + ", filtered=" + filtered + "\n", Level.FINEST);
                                bestQuality = quality;
                                mostCovered = left_p;
                                bestCondition = candidate;
                                ignoreCandidate = null;
                            }
                        }
                    }

                    // evaluate right-side condition: a in <v, inf)
                    if (right_prec > apriori_prec && (left_p + left_n != 0)) {
                        double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
                                right_p, right_n, rule.getWeighted_P(), rule.getWeighted_N());
                        if ((quality > bestQuality || (quality == bestQuality && right_p > mostCovered)) && (toCover_right_p > 0)) {
                            ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(value));
                            if (checkCandidate(candidate, classId, rule.getWeighted_P(), toCover_right_p)) {
                                Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + toCover_right_p + ", quality="  + quality + ", filtered=" + filtered +"\n", Level.FINEST);
                                bestQuality = quality;
                                mostCovered = right_p;
                                bestCondition = candidate;
                                ignoreCandidate = null;
                            }
                        }
                    }


                } else {
                    // nominal attribute
                    double p = conditionCovered.calculateIntersectionSize(positives);
                    double toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule, (IntegerBitSet)uncoveredPositives);
                    double n = conditionCovered.calculateIntersectionSize((IntegerBitSet)coveredByRule) - p;

                    // evaluate equality condition a = v
                    double quality = ((ClassificationMeasure)params.getInductionMeasure()).calculate(
                            p, n, rule.getWeighted_P(), rule.getWeighted_N());
                    if ((quality > bestQuality || (quality == bestQuality && p > mostCovered)) && (toCover_p > 0) && (p + n != coveredByRule.size())) {
                        ElementaryCondition candidate =
                                new ElementaryCondition(attr.getName(), new SingletonSet(value, attr.getMapping().getValues()));
                        if (checkCandidate(candidate, classId, rule.getWeighted_P(), toCover_p)) {
                            Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + toCover_p + ", quality="  + quality + ", filtered=" + filtered + "\n", Level.FINEST);
                            bestQuality = quality;
                            mostCovered = p;
                            bestCondition = candidate;
                            ignoreCandidate = attr;
                        }
                    }
                }
            }

        }

        if (ignoreCandidate != null) {
            allowedAttributes.remove(ignoreCandidate);
        }

        return bestCondition;
    }
    */

}
