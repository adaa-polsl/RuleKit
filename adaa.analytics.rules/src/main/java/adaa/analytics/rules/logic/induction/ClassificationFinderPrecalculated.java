package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class ClassificationFinderPrecalculated extends ClassificationFinder {

    class PrecalculatedCovering {
        Attribute attribute;
        double value;
        boolean complement; // negated for nominal, right-sided for numerical
        IntegerBitSet mask;
        int prefixSums[]; // store prefix sums for every 100th element

        PrecalculatedCovering(Attribute attribute, double value, boolean complement, int size) {
            this.attribute = attribute;
            this.value = value;
            this.complement = complement;

            mask = new IntegerBitSet(size);
            prefixSums = new int[size / 100];
        }
    }

    /**
     * Map of precalculated coverings (time optimization).
     * For each attribute there is a set of distinctive values. For each value there is a bit vector of examples covered.
     */
    protected List<PrecalculatedCovering> precalculatedCoverings;


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
    /*
    @Override
    public void preprocess(ExampleSet trainSet) {

        // do nothing for weighted datasets
        if (trainSet.getAttributes().getWeight() != null) {
            return;
        }

        precalculatedCoverings = new ArrayList<PrecalculatedCovering>();

        Attributes attributes = trainSet.getAttributes();

        List<Future> futures = new ArrayList<Future>();

        // iterate over all allowed decision attributes
        for (Attribute attr : attributes) {

            Future f = pool.submit( () -> {

                ArrayList<PrecalculatedCovering> localCoverings = new ArrayList<PrecalculatedCovering>();

                // check if attribute is nominal
                if (attr.isNominal()) {
                    // prepare structures for all
                    for (int val = 0; val != attr.getMapping().size(); ++val) {
                        localCoverings.add(new PrecalculatedCovering(attr, (double)val, false, trainSet.size()));
                        localCoverings.add(new PrecalculatedCovering(attr, (double)val, true, trainSet.size()));
                    }

                    // get all distinctive values of attribute
                    int id = 0;
                    for (Example e : trainSet) {
                        DataRow dr = e.getDataRow();
                        double value = dr.get(attr);

                        // omit missing values
                        if (!Double.isNaN(value)) {
                            localCoverings.get((int)value * 2).mask.add(id);
                        }
                        ++id;
                    }

                    for (int val = 0; val != attr.getMapping().size(); ++val) {
                        PrecalculatedCovering fov = localCoverings.get(val * 2);
                        PrecalculatedCovering rev = localCoverings.get(val * 2 + 1);
                        fov.mask.negate(rev.mask);
                    }
                } else {
                    Integer [] ids = new Integer[trainSet.size()];
                    Arrays.sort(ids, Comparator.comparingDouble(a -> trainSet.getExample(a).getValue(attr)));


                }

                synchronized (this) {
                    precalculatedCoverings.addAll(localCoverings);
                }
            });

            futures.add(f);
        }

        try {
            for (Future f : futures) {
                f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


        /*
        // this will handle nominal attributes
        super.preprocess();

        // iterate over all allowed decision attributes
        for (Attribute attr : attributes) {




                    // check if attribute is numerical or nominal
            if (attr.isNumerical()) {
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
            }
        }

    }
    */



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
    /*
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
        Attribute weightAttr = trainSet.getAttributes().getWeight();
        Set<Integer> positives = rule.getCoveredPositives();
        double P = rule.getWeighted_P();
        double N = rule.getWeighted_N();

        double apriori_prec = params.isControlAprioriPrecision()
                ? P / (P + N)
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
                        if (Double.isNaN(val)) {
                            continue;
                        }

                        TotalPosNeg tot = totals.computeIfAbsent(val, (k) -> new TotalPosNeg());
                        double w = (weightAttr != null) ? dr.get(weightAttr) : 1.0;

                        // put to proper bin depending of class label
                        if (positives.contains(id)) {
                            right_p += w;
                            tot.p += w;
                            if (uncoveredPositives.contains(id)) {
                                ++toCover_right_p;
                                ++tot.toCover_p;
                            }
                        } else {
                            right_n += w;
                            tot.n += w;
                        }
                    }

                    Double [] keys = totals.keySet().toArray(new Double[totals.size()]);
                    //Logger.log(", " + keys.length, Level.INFO);

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

                        TotalPosNeg totNext = totals.get(next);
                        if ((tot.n == 0 && totNext.n == 0) || (tot.p == 0 && totNext.p == 0)) {
                            continue;
                        }


                        // calculate precisions
                        double left_prec = left_p / (left_p + left_n);
                        double right_prec = right_p / (right_p + right_n);

                        // evaluate left-side condition: a in (-inf, v)
                        if (left_prec > apriori_prec && toCover_left_p > 0) {
                            double quality = params.getInductionMeasure().calculate(left_p, left_n, P, N);
                            quality = modifier.modifyQuality(quality, attr.getName(), left_p, toCover_left_p);

                            if (quality > best.quality || (quality == best.quality && left_p > best.covered)) {
                                ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_le(midpoint));
                                if (checkCandidate(candidate, classId, left_p, left_n, toCover_left_p, P)) {
                                    Logger.log("\tCurrent best: " + candidate + " (p=" + left_p + ", n=" + left_n + ", new_p=" + (double) toCover_left_p + ", quality=" + quality + "\n", Level.FINEST);
                                    best.quality = quality;
                                    best.covered = left_p;
                                    best.condition = candidate;
                                    best.opposite = false;
                                }
                            }
                        }

                        // evaluate right-side condition: a in <v, inf)
                        if (right_prec > apriori_prec && toCover_right_p > 0) {
                            double quality = params.getInductionMeasure().calculate(right_p, right_n, P, N);
                            quality = modifier.modifyQuality(quality, attr.getName(), right_p, toCover_right_p);

                            if (quality > best.quality || (quality == best.quality && right_p > best.covered)) {
                                ElementaryCondition candidate = new ElementaryCondition(attr.getName(), Interval.create_geq(midpoint));
                                if (checkCandidate(candidate, classId, right_p, right_n, toCover_right_p, P)) {
                                    Logger.log("\tCurrent best: " + candidate + " (p=" + right_p + ", n=" + right_n + ", new_p=" + (double) toCover_right_p + ", quality=" + quality + "\n", Level.FINEST);
                                    best.quality = quality;
                                    best.covered = right_p;
                                    best.condition = candidate;
                                    best.opposite = false;
                                }
                            }
                        }
                    }
                } else { // nominal attribute

                    // weighted case - no precalculated converings
                    if (precalculatedCoverings == null) {
                        // sum of positive and negative weights for all values
                        double[] p = new double[attr.getMapping().size()];
                        double[] n = new double[attr.getMapping().size()];

                        int[] toCover_p = new int[attr.getMapping().size()];

                        // get all distinctive values of attribute
                        for (int id : coveredByRule) {
                            DataRow dr = trainSet.getExample(id).getDataRow();
                            double value = dr.get(attr);

                            // omit missing values
                            if (Double.isNaN(value)) {
                                continue;
                            }

                            int castedValue = (int) value;
                            double w = (weightAttr != null) ? dr.get(weightAttr) : 1.0;

                            if (positives.contains(id)) {
                                p[castedValue] += w;
                                if (uncoveredPositives.contains(id)) {
                                    ++toCover_p[castedValue];
                                }

                            } else {
                                n[castedValue] += w;
                            }
                        }

                        // try all possible conditions
                        for (int i = 0; i < attr.getMapping().size(); ++i) {
                            // evaluate equality condition a = v
                            double quality = params.getInductionMeasure().calculate(p[i], n[i], P, N);
                            quality = modifier.modifyQuality(quality, attr.getName(), p[i], toCover_p[i]);

                            if ((quality > best.quality || (quality == best.quality && p[i] > best.covered)) && (toCover_p[i] > 0)) {
                                ElementaryCondition candidate =
                                        new ElementaryCondition(attr.getName(), new SingletonSet((double) i, attr.getMapping().getValues()));
                                if (checkCandidate(candidate, classId, p[i], n[i], toCover_p[i], P)) {
                                    Logger.log("\tCurrent best: " + candidate + " (p=" + p[i] + ", n=" + n[i] + ", new_p=" + (double) toCover_p[i] + ", quality=" + quality + "\n", Level.FINEST);
                                    best.quality = quality;
                                    best.covered = p[i];
                                    best.condition = candidate;
                                    best.opposite = false;
                                }
                            }
                        }

                    } else {
                        // unweighted case
                        // try all possible conditions
                        for (int i = 0; i < attr.getMapping().size(); ++i) {

                            // evaluate straight condition
                            IntegerBitSet conditionCovered = precalculatedCoverings.get(attr).get((double) i);
                            double p = conditionCovered.calculateIntersectionSize(rule.getCoveredPositives());
                            int toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule, (IntegerBitSet) uncoveredPositives);
                            double n = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule) - p;

                            // no need to analyze conditions that do not alter covering
                            //if (p == rule.getWeighted_p() && n == rule.getWeighted_n()) {
                            //continue;
                            //}

                            double prec = p / (p + n);
                            if (prec > apriori_prec && toCover_p > 0) {
                                // evaluate equality condition a = v
                                double quality = params.getInductionMeasure().calculate(p, n, P, N);
                                quality = modifier.modifyQuality(quality, attr.getName(), p, toCover_p);
                                // prefer (gender = female) over (gender = !male) for boolean attributes
                                if (quality > best.quality ||
                                        (quality == best.quality && (p > best.covered || best.opposite))) {
                                    ElementaryCondition candidate =
                                            new ElementaryCondition(attr.getName(), new SingletonSet((double) i, attr.getMapping().getValues()));
                                    if (checkCandidate(candidate, classId, p, n, toCover_p, P)) {
                                        Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double) toCover_p + ", quality=" + quality + "\n", Level.FINEST);
                                        best.quality = quality;
                                        best.covered = p;
                                        best.condition = candidate;
                                        best.opposite = false;
                                    }
                                }
                            }

                            // evaluate complementary condition if enabled
                            if (!params.isConditionComplementEnabled()) {
                                continue;
                            }

                            conditionCovered = precalculatedCoveringsComplement.get(attr).get((double) i);
                            p = conditionCovered.calculateIntersectionSize(rule.getCoveredPositives());
                            toCover_p = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule, (IntegerBitSet) uncoveredPositives);
                            n = conditionCovered.calculateIntersectionSize((IntegerBitSet) coveredByRule) - p;

                            prec = p / (p + n);
                            if (prec > apriori_prec && toCover_p > 0) {
                                // evaluate equality condition a = v
                                double quality = params.getInductionMeasure().calculate(p, n, P, N);
                                quality = modifier.modifyQuality(quality, attr.getName(), p, toCover_p);

                                if (quality > best.quality || (quality == best.quality && p > best.covered)) {
                                    ElementaryCondition candidate =
                                            new ElementaryCondition(attr.getName(), new SingletonSetComplement((double) i, attr.getMapping().getValues()));
                                    if (checkCandidate(candidate, classId, p, n, toCover_p, P)) {
                                        Logger.log("\tCurrent best: " + candidate + " (p=" + p + ", n=" + n + ", new_p=" + (double) toCover_p + ", quality=" + quality + "\n", Level.FINEST);
                                        best.quality = quality;
                                        best.covered = p;
                                        best.condition = candidate;
                                        best.opposite = true;
                                    }
                                }
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
                ConditionEvaluation eval = (ConditionEvaluation)f.get();
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
    */

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
