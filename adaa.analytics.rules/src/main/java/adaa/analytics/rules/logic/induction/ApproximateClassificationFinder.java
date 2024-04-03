package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class ApproximateClassificationFinder extends ClassificationFinder {

    static class ConditionCandidate extends ElementaryCondition {

        public double quality = -Double.MAX_VALUE;
        public double p = 0;
        public double n = 0;
        public boolean opposite = false;
        public int blockId = -1;

        public ConditionCandidate() {}

        public ConditionCandidate(String attribute, IValueSet valueSet) {
            super(attribute, valueSet);
        }
    }

    // Example description:
    // [0-31] - example id (32 bits)
    // [32-47] - block id  (16 bits)
    // [48-61] - free
    // 61 - positive flag
    // 62 - new flag
    // 63 - coverage flag
    protected static final long MASK_IDENTIFIER = 0x0000FFFFFFFFL;
    protected static final long MASK_BIN        = 0xFFFF00000000L;
    protected static final long OFFSET_BIN      = 32L;

    protected static final long FLAG_POSITIVE   = 1L << 61;
    protected static final long FLAG_NEW        = 1L << 62;
    protected static final long FLAG_COVERED    = 1L << 63;

    protected long[][] descriptions;
    protected int[][] mappings;

    protected int[][] bins_positives;
    protected int[][] bins_negatives;
    protected int[][] bins_newPositives;
    protected int[][] bins_begins;

    protected int[][] ruleRanges;

    protected IExampleSet trainSet;

    Map<String, Object> arrayCopies = new HashMap<String,Object>();

    /**
     * Initializes induction parameters.
     *
     * @param params Induction parameters.
     */
    public ApproximateClassificationFinder(InductionParameters params) {
        super(params);
    }

    @Override
    public IExampleSet preprocess(IExampleSet dataset) {
        int n_examples = dataset.size();
        int n_attributes = dataset.getAttributes().size();

        trainSet = dataset;
        descriptions = new long[n_attributes][n_examples];
        mappings = new int[n_attributes][n_examples];

        bins_positives = new int[n_attributes][];
        bins_negatives = new int[n_attributes][];
        bins_newPositives = new int[n_attributes][];
        bins_begins = new int[n_attributes][];

        ruleRanges = new int[n_attributes][2];

        for (IAttribute attr: dataset.getAttributes()) {
            int ia = attr.getTableIndex();
            int n_vals = attr.isNominal() ? attr.getMapping().size() : params.getApproximateBinsCount();

            bins_positives[ia] = new int [n_vals];
            bins_negatives[ia] = new int[n_vals];
            bins_newPositives[ia] = new int[n_vals];
            bins_begins[ia] = new int[n_vals];

            determineBins(dataset, attr, descriptions[ia], mappings[ia], bins_begins[ia], ruleRanges[ia]);

            arrayCopies.put("ruleRanges", (Object)Arrays.stream(ruleRanges).map(int[]::clone).toArray(int[][]::new));

            if (attr.isNominal()) {
                // get orders
                Integer[] valuesOrder = new Integer[attr.getMapping().size()];
                List<String> labels = new ArrayList<>();
                labels.addAll(attr.getMapping().getValues());
                Collections.sort(labels);
                for (int j = 0; j < labels.size(); ++j) {
                    valuesOrder[j] = attr.getMapping().getIndex(labels.get(j));
                }
                attributeValuesOrder.put(attr, valuesOrder);
            }
        }

        return dataset;
    }

    /**
     * Removes irrelevant conditions from the rule using hill-climbing strategy.
     * @param rule Rule to be pruned.
     * @param dataset Training set.
     * @return Updated covering object.
     */
    @Override
    public void prune(
            final Rule rule,
            final IExampleSet dataset,
            final Set<Integer> uncovered)
    {
        super.prune(rule, dataset, uncovered);

        // get all covered examples
        IntegerBitSet covered = rule.getCoveredPositives().clone();
        covered.addAll(rule.getCoveredNegatives());

        int[][] copy_ranges = (int[][])arrayCopies.get("ruleRanges");

        // reset all arrays using the coverage
        int n_examples = dataset.size();

        for (IAttribute attr: dataset.getAttributes()) {
            int attribute_id = attr.getTableIndex();

            ruleRanges[attribute_id][0] = 0;
            ruleRanges[attribute_id][1] = copy_ranges[attribute_id][1];

            Arrays.fill(bins_positives[attribute_id], 0);
            Arrays.fill(bins_negatives[attribute_id], 0);
            Arrays.fill(bins_newPositives[attribute_id], 0);

            long[] descriptions_row = descriptions[attribute_id];
            int[] mappings_row = mappings[attribute_id];

            int[] positives_row = bins_positives[attribute_id];
            int[] negatives_row = bins_negatives[attribute_id];
            int[] newPositives_row = bins_newPositives[attribute_id];

            // iterate over examples
            for (int i = 0; i < n_examples; ++i) {

                int example_id = (int) (descriptions_row[i] & MASK_IDENTIFIER);
                int bid = (int) ((descriptions_row[i] & MASK_BIN) >> OFFSET_BIN);

                // check if covered by current rule
                if (covered.contains(example_id)) {
                   // it could become covered
                    descriptions_row[i] |= FLAG_COVERED;


                    if ((descriptions_row[i] & FLAG_POSITIVE) != 0) {
                        ++positives_row[bid];

                        if ((descriptions_row[i] & FLAG_NEW) != 0) {
                            ++newPositives_row[bid];
                        }

                    } else {
                        ++negatives_row[bid];
                    }

                }
            }
        }

        // establish rule ranges using only remaining conditions
        for (ConditionBase sub : rule.getPremise().getSubconditions()) {
            ConditionCandidate cnd = (ConditionCandidate)sub;

            IAttribute attr = dataset.getAttributes().get(cnd.getAttribute());
            int aid = attr.getTableIndex();

            if (attr.isNominal()) {
                if (cnd.opposite) {
                    ruleRanges[aid][0] = cnd.blockId + 1;
                    ruleRanges[aid][1] = cnd.blockId;
                } else {
                    ruleRanges[aid][0] = cnd.blockId;
                    ruleRanges[aid][1] = cnd.blockId + 1;
                }
            } else {
                if (cnd.opposite) {
                    if (cnd.blockId > ruleRanges[aid][0]) {
                        ruleRanges[aid][0] = cnd.blockId;
                    }
                } else {
                    if (cnd.blockId < ruleRanges[aid][1]) {
                        ruleRanges[aid][1] = cnd.blockId;
                    }
                }
            }
        }

        printArrays();
    }

    /**
     * Postprocesses a rule.
     *
     * @param rule Rule to be postprocessed.
     * @param dataset Training set.
     *
     */
    public void postprocess(
            final Rule rule,
            final IExampleSet dataset) {

        // restore original arrays (preserve only covered flags)
        int[][] copy_positives = (int[][])arrayCopies.get("bins_positives");
        int[][] copy_negatives = (int[][])arrayCopies.get("bins_negatives");
        int[][] copy_newPositives = (int[][])arrayCopies.get("bins_newPositives");

        int[][] copy_ranges = (int[][])arrayCopies.get("ruleRanges");

        // iterate over descriptions
        for (int ia = 0; ia < descriptions.length; ++ia) {
            long [] row = descriptions[ia];
            for (int i = 0; i < row.length; ++i) {
                long desc = row[i];
                if ((desc & FLAG_COVERED) != 0) {
                    desc &= ~FLAG_NEW;
                }

                desc |= FLAG_COVERED; // mark as covered for a next empty rule
                row[i] = desc;
            }

            // restore bin counters
            for (int bid = 0; bid < bins_positives[ia].length; ++bid) {
                bins_positives[ia][bid] = copy_positives[ia][bid];
                bins_negatives[ia][bid] = copy_negatives[ia][bid];

                // alter copy as well
                bins_newPositives[ia][bid] = copy_newPositives[ia][bid] =
                        copy_newPositives[ia][bid] - bins_newPositives[ia][bid];
            }

            // restore ranges
            ruleRanges[ia][0] = 0;
            ruleRanges[ia][1] = copy_ranges[ia][1];
        }

        Logger.log("Restore arrays from copy\n", Level.FINER);
        printArrays();

        super.postprocess(rule, dataset);
    }


    /**
     * Induces an elementary condition.
     *
     * @param rule Current rule.
     * @param dataset Training set.
     * @param uncoveredPositives Set of positive examples uncovered by the model.
     * @param coveredByRule Set of examples covered by the rule being grown.
     * @param allowedAttributes Set of attributes that may be used during induction.
     * @param extraParams Additional parameters.
     * @return Induced elementary condition.
     */
    @Override
    protected ElementaryCondition induceCondition(
            Rule rule,
            IExampleSet dataset,
            Set<Integer> uncoveredPositives,
            Set<Integer> coveredByRule,
            Set<IAttribute> allowedAttributes,
            Object... extraParams) {

        if (allowedAttributes.size() == 0) {
            return null;
        }

        double classId = ((SingletonSet) rule.getConsequence().getValueSet()).getValue();
        IAttribute weightAttr = dataset.getAttributes().getWeight();
        Set<Integer> positives = rule.getCoveredPositives();
        double P = rule.getWeighted_P();
        double N = rule.getWeighted_N();
        double P_new = (double)uncoveredPositives.size();

        double apriori_prec = params.isControlAprioriPrecision()
                ? P / (P + N)
                : Double.MIN_VALUE;

        List<Future<ConditionCandidate>> futures = new ArrayList<Future<ConditionCandidate>>();

        int covered_p = 0;
        int covered_n = 0;
        int covered_new_p = 0;

        // use first  attribute to establish number of covered elements
        for (int bid = ruleRanges[0][0]; bid < ruleRanges[0][1]; ++bid) {
            covered_p += bins_positives[0][bid];
            covered_n += bins_negatives[0][bid];
            covered_new_p += bins_newPositives[0][bid];
        }


        // iterate over all allowed decision attributes
        for (IAttribute attr : dataset.getAttributes()) {

            if (!allowedAttributes.contains(attr)) {
                continue;
            }
            // consider attributes in parallel
            int finalCovered_p = covered_p;
            int finalCovered_n = covered_n;
            int finalCovered_new_p = covered_new_p;
            Future<ConditionCandidate> future = (Future<ConditionCandidate>) pool.submit(() -> {

                ConditionCandidate best = new ConditionCandidate();
                int attribute_id = attr.getTableIndex();

                long[] cur_descriptions = descriptions[attribute_id];

                int[] cur_positives = bins_positives[attribute_id];
                int[] cur_negatives = bins_negatives[attribute_id];
                int[] cur_newPositives = bins_newPositives[attribute_id];
                int[] cur_begins = bins_begins[attribute_id];

                class Stats {
                    double p = 0;
                    double n = 0;
                    double p_new = 0;

                    Stats(double p, double n, double p_new) {
                        this.p = p;
                        this.n = n;
                        this.p_new = p_new;
                    }
                }

                Stats[] stats = new Stats[2];

                // numerical attribute
                if (attr.isNumerical()) {
                    int first_bid = ruleRanges[attribute_id][0];
                    int last_bid = ruleRanges[attribute_id][1];

                    // omit empty bins from the beginning and from the end
                    while (first_bid < last_bid && (cur_positives[first_bid] + cur_negatives[first_bid] == 0)) {
                        ++first_bid;
                    }

                    while (first_bid < last_bid && (cur_positives[last_bid - 1] + cur_negatives[last_bid - 1] == 0)) {
                        --last_bid;
                    }

                    stats[0] = new Stats(cur_positives[first_bid], cur_negatives[first_bid], cur_newPositives[first_bid]);
                    stats[1] = new Stats(finalCovered_p - stats[0].p, finalCovered_n - stats[0].n, finalCovered_new_p - stats[0].p_new);

                    // iterate over blocks
                    for (int bid = first_bid + 1; bid < last_bid; ++bid) {
                        // omit conditions:
                        // - preceding empty bins - they may appear as coverage drops
                        // - dividing positive-only or negative-only bins
                        int cur_p = cur_positives[bid];
                        int cur_n = cur_negatives[bid];
                        int prev_p = cur_positives[bid - 1];
                        int prev_n = cur_negatives[bid - 1];

                        if ((cur_p + cur_n != 0) && (prev_p + cur_p != 0) && (prev_n + cur_n != 0)) {

                            // evaluate both conditions
                            for (int c = 0; c < 2; ++c) {
                                double prec = stats[c].p / (stats[c].p + stats[c].n);

                                if (prec > apriori_prec && stats[c].p_new > 0) {
                                    double quality = params.getInductionMeasure().calculate(stats[c].p, stats[c].n, P, N);

                                    // better then current best
                                    if (quality > best.quality || (quality == best.quality && stats[c].p > best.p)) {

                                        int left_id = (int) (cur_descriptions[cur_begins[bid] - 1] & MASK_IDENTIFIER);
                                        int right_id = (int) (cur_descriptions[cur_begins[bid]] & MASK_IDENTIFIER);

                                        double midpoint = (dataset.getExample(left_id).getValue(attr) + dataset.getExample(right_id).getValue(attr)) / 2;

                                        IValueSet interval = (c == 0)
                                                ? Interval.create_le(midpoint)
                                                : Interval.create_geq(midpoint);

                                        ConditionCandidate candidate = new ConditionCandidate(attr.getName(), interval);
                                        if (checkCandidate(candidate, classId, stats[c].p, stats[c].n, stats[c].p_new, P, uncoveredPositives.size(), rule.getRuleOrderNum())) {
                                            //Logger.log("\tCurrent best: " + candidate + " (p=" + stats[c].p + ", n=" + stats[c].n + ", new_p=" + (double) stats[c].p_new + ", quality=" + quality + ")\n", Level.FINEST);
                                            best = candidate;
                                            best.quality = quality;
                                            best.p = stats[c].p;
                                            best.n = stats[c].n;
                                            best.opposite = (c == 1);
                                            best.blockId = bid;
                                        }
                                    }
                                }
                            }
                        }

                        // update stats
                        stats[0].p += cur_p;
                        stats[0].n += cur_n;
                        stats[0].p_new += cur_newPositives[bid];

                        stats[1].p -= cur_p;
                        stats[1].n -= cur_n;
                        stats[1].p_new -= cur_newPositives[bid];
                    }
                } else { // nominal attribute

                    // they will be reassigned anyway
                    stats[0] = new Stats(0, 0, 0);
                    stats[1] = new Stats(finalCovered_p - stats[0].p, finalCovered_n - stats[0].n, finalCovered_new_p - stats[0].p_new);

                    for (int j = 0; j < attr.getMapping().size(); ++j) {
                        int bid = attributeValuesOrder.get(attr)[j];

                        // update stats
                        stats[0].p = cur_positives[bid];
                        stats[0].n = cur_negatives[bid];
                        stats[0].p_new = cur_newPositives[bid];

                        stats[1].p = finalCovered_p - stats[0].p;
                        stats[1].n = finalCovered_n - stats[0].n;
                        stats[1].p_new = finalCovered_new_p - stats[0].p_new;

                        // evaluate both conditions
                        for (int c = 0; c < 2; ++c) {
                            double prec = stats[c].p / (stats[c].p + stats[c].n);

                            if (prec > apriori_prec && stats[c].p_new > 0) {
                                double quality = params.getInductionMeasure().calculate(stats[c].p, stats[c].n, P, N);
                                boolean opposite = (c == 1);

                                // better than current best
                                if (quality > best.quality || (quality == best.quality && (stats[c].p > best.p ||
                                        (stats[c].p == best.p && best.opposite && !opposite)))) {

                                    IValueSet interval = !opposite
                                            ? new SingletonSet((double) bid, attr.getMapping().getValues())
                                            : new SingletonSetComplement((double) bid, attr.getMapping().getValues());

                                    ConditionCandidate candidate = new ConditionCandidate(attr.getName(), interval);
                                    if (checkCandidate(candidate, classId, stats[c].p, stats[c].n, stats[c].p_new, P, uncoveredPositives.size(), rule.getRuleOrderNum())) {
                                        //Logger.log("\tCurrent best: " + candidate + " (p=" + stats[c].p + ", n=" + stats[c].n + ", new_p=" + (double) stats[c].p_new + ", quality=" + quality + ")\n", Level.FINEST);
                                        best = candidate;
                                        best.quality = quality;
                                        best.p = stats[c].p;
                                        best.n = stats[c].n;
                                        best.opposite = opposite;
                                        best.blockId = bid;
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

        ConditionCandidate best = null;

        try {
            for (Future f : futures) {
                ConditionCandidate current = (ConditionCandidate)f.get();

                if (current != null && current.getAttribute() != null) {
                    Logger.log("\tAttribute best: " + current + ", quality=" +
                            current.quality + ", p=" + current.p + ", n=" + current.n, Level.FINEST);   IAttribute attr = dataset.getAttributes().get(current.getAttribute());
                    if (attr.isNumerical()) {
                        updateMidpoint(dataset, current);
                    }
                    Logger.log(", adjusted: " + current + "\n", Level.FINEST);
                }

                if (best == null || current.quality > best.quality || (current.quality == best.quality && current.p > best.p)) {
                    best = current;
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (best != null) {
            IAttribute bestAttr = dataset.getAttributes().get(best.getAttribute());

            if (bestAttr == null) {
                return null; // empty condition - discard
            }

            if (bestAttr.isNumerical()) {
                updateMidpoint(dataset, best);
            } else {
                allowedAttributes.remove(bestAttr);
            }

            Logger.log("\tFinal best: " + best + ", quality=" + best.quality + "\n", Level.FINEST);
        }

		return best;

    }

    protected void notifyConditionAdded(ConditionBase cnd) {
        ConditionCandidate candidate = (ConditionCandidate)cnd;
        IAttribute attr =  trainSet.getAttributes().get(candidate.getAttribute());

        int aid = attr.getTableIndex();
        int blockId = candidate.blockId;

        if (attr.isNominal()) {
            if (candidate.opposite) {
                excludeExamplesFromArrays(trainSet, attr, candidate.blockId, candidate.blockId + 1);
                ruleRanges[aid][0] = blockId + 1;
                ruleRanges[aid][1] = blockId;
            } else {
                excludeExamplesFromArrays(trainSet, attr, ruleRanges[aid][0], candidate.blockId);
                excludeExamplesFromArrays(trainSet, attr, candidate.blockId + 1, ruleRanges[aid][1]);
                ruleRanges[aid][0] = blockId;
                ruleRanges[aid][1] = blockId + 1;
            }
        } else {
            if (candidate.opposite) {
                excludeExamplesFromArrays(trainSet, attr, ruleRanges[aid][0], candidate.blockId);
                ruleRanges[aid][0] = blockId;
            } else {
                excludeExamplesFromArrays(trainSet, attr, candidate.blockId,  ruleRanges[aid][1]);
                ruleRanges[aid][1] = blockId;
            }
        }

        Logger.log("After exclusion\n", Level.FINEST);
        printArrays();

        super.notifyConditionAdded(cnd);
    }

    protected void determineBins(IExampleSet dataset, IAttribute attr,
                                 long[] descriptions,
                                 int[] mappings,
                                 int[] binsBegins,
                                 int[] ruleRanges) {


        Logger.log("Establishing bins: " + attr.getName() + "\n", Level.FINER);

        double [] vals = new double[dataset.size()];
        Integer[] sortedIds = new Integer[dataset.size()];

        for (int i = 0; i < dataset.size(); ++i) {
            sortedIds[i] = i;
            vals[i] = dataset.getExample(i).getValue(attr);
        }


        /*
        class ValuesComparator implements IntComparator {
            double [] vals;

            ValuesComparator(double[] vals) {
                this.vals = vals;
            }

            @Override
            public int compare(int i, int j) {
               return Double.compare(vals[i], vals[j]);
            }
        }

        // get sorted mappings
        IntArrays.quickSort(mappings, new ValuesComparator(vals));
        */

        Arrays.sort(sortedIds, Comparator.comparingDouble(a -> vals[a]));
        Arrays.sort(vals);

       for (int i = 0; i < sortedIds.length; ++i) {
           int example_id = sortedIds[i];
           descriptions[i] |= example_id;
           mappings[example_id] = i;
       }

        class Bin {
            int begin;
            int end;

            public Bin(int begin, int end) {
                this.begin = begin;
                this.end = end;
            }
        }

        class SizeBinComparator implements Comparator<Bin> {
            @Override
            public int compare(Bin p, Bin q) {
                return Integer.compare(q.end - q.begin, p.end - p.begin);
            }
        }

        class IndexBinComparator implements Comparator<Bin> {
            @Override
            public int compare(Bin p, Bin q) {
                return Integer.compare(p.begin, q.begin);
            }
        }

        PriorityQueue<Bin> bins = new PriorityQueue<Bin>(binsBegins.length, new SizeBinComparator());
        PriorityQueue<Bin> finalBins = new PriorityQueue<Bin>(binsBegins.length, new IndexBinComparator());

        bins.add(new Bin(0, mappings.length));

        while (bins.size() > 0 && (bins.size() + finalBins.size()) < binsBegins.length) {
            Bin b = bins.poll();

            int id = (b.end + b.begin) / 2;
            double midval = vals[id];

            // decide direction
            if (vals[b.begin] == midval) {
                // go up
                while (vals[id] == midval) {
                    ++id;
                }
            } else {
                while (vals[id - 1] == midval) {
                    --id;
                }
            }

            Bin leftBin = new Bin(b.begin, id);

            // if bin is uniform
            if (vals[leftBin.begin] == vals[leftBin.end - 1]) {
                finalBins.add(leftBin);
            } else {
                bins.add(leftBin);
            }

            Bin rightBin = new Bin(id, b.end);
            if (vals[rightBin.begin] == vals[rightBin.end - 1]) {
                finalBins.add(rightBin);
            } else {
                bins.add(rightBin);
            }
        }

        // move non-uniform bins to the final bin collection
        while (bins.size() > 0) {
            finalBins.add(bins.poll());
        }

        long bid = 0;
        while (finalBins.size() > 0) {
            Bin b = finalBins.poll();

            for (int i = b.begin; i < b.end; ++i) {
                descriptions[i] |= bid << OFFSET_BIN;
            }

            binsBegins[(int) bid] = b.begin;
            ++bid;
        }

        ruleRanges[0] = 0;
        ruleRanges[1] = (int) bid;
      // print bins
        for (int i = 0; i < ruleRanges[1]; ++i) {
            int lo = binsBegins[i];
            int hi = (i == ruleRanges[1] - 1) ? trainSet.size() : binsBegins[i+1] - 1;
            Logger.log("[" + lo  + ", " + hi + "]:" + vals[lo] + "\n", Level.FINER);
        }
    }

    protected void excludeExamplesFromArrays(IExampleSet dataset, IAttribute attr, int binLo, int binHi) {

        Logger.log("Excluding examples: " + attr.getName() + " from [" + binLo + "," + binHi + "]\n", Level.FINER);

        if (binLo == binHi) {
            return;
        }

        int n_examples = dataset.size();
        int src_row = attr.getTableIndex();
        long[] src_descriptions = descriptions[src_row];

        // exclude examples in other attributes
        List<Future<Object>> futures = new ArrayList<Future<Object>>();

        int exLo = bins_begins[src_row][binLo];
        int exHi = ((binHi == bins_begins[src_row].length) || (bins_begins[src_row][binHi] == 0))
                ? n_examples
                : bins_begins[src_row][binHi];

        // clear bins for the current attribute
        Arrays.fill(bins_positives[src_row],  binLo, binHi, 0);
        Arrays.fill(bins_negatives[src_row],  binLo, binHi,0);
        Arrays.fill(bins_newPositives[src_row],  binLo, binHi,0);

        for (int i = exLo; i < exHi; ++i) {
            src_descriptions[i] &= ~FLAG_COVERED; // remove from covered
        }

        for (IAttribute other : dataset.getAttributes() ) {

            if (other == attr) {
                continue;
            }

            int dst_row = other.getTableIndex();

            // if nominal attribute was already used
            /*
            if (other.isNominal() && Math.abs(ruleRanges[dst_row][1] - ruleRanges[dst_row][0]) == 1) {
                continue;
            }
             */

            Future<Object> future = pool.submit(() -> {

                int[] dst_positives = bins_positives[dst_row];
                int[] dst_negatives = bins_negatives[dst_row];
                int[] dst_newPositives = bins_newPositives[dst_row];

                long[] dst_descriptions = descriptions[dst_row];
                int[] dst_mapping = mappings[dst_row];
                int[] dst_ranges = ruleRanges[dst_row];

                for (int i = exLo; i < exHi; ++i) {

                    int example_id = (int)(src_descriptions[i] & MASK_IDENTIFIER);
                    int map = dst_mapping[example_id];
                    long desc = dst_descriptions[map];

                    int bid = (int) ((desc & MASK_BIN) >> OFFSET_BIN);

                    boolean opposite = dst_ranges[0] > dst_ranges[1]; // this indicate nominal opposite condition
                    int dst_bin_lo = Math.min(dst_ranges[0], dst_ranges[1]);
                    int dst_bin_hi = Math.max(dst_ranges[0], dst_ranges[1]);

                    // update stats only in bins covered by the rule
                    boolean in_range = (bid >= dst_bin_lo && bid < dst_bin_hi) || (opposite && (bid < dst_bin_lo || bid >= dst_bin_hi));

                    if (in_range && ((desc & FLAG_COVERED) != 0)) {

                        if ((desc & FLAG_POSITIVE) != 0) {
                            --dst_positives[bid];
                            if ((desc & FLAG_NEW) != 0) {
                                --dst_newPositives[bid];
                            }
                        } else {
                            --dst_negatives[bid];
                        }

                        dst_descriptions[map] &= ~FLAG_COVERED; // remove from covered
                    }
                }

                return null;
            });

            futures.add(future);
        }

        // wait until updates are finished
        try {
            for (Future f : futures) {
               f.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }


    protected void resetArrays(IExampleSet dataset, int targetLabel) {

        int n_examples = dataset.size();

        int[][] copy_ranges = (int[][])arrayCopies.get("ruleRanges");

        for (IAttribute attr: dataset.getAttributes()) {
            int attribute_id = attr.getTableIndex();

            Arrays.fill(bins_positives[attribute_id], 0);
            Arrays.fill(bins_negatives[attribute_id], 0);
            Arrays.fill(bins_newPositives[attribute_id], 0);
            ruleRanges[attribute_id][0] = 0;
            ruleRanges[attribute_id][1] = copy_ranges[attribute_id][1];

            long[] descriptions_row = descriptions[attribute_id];
            int[] mappings_row = mappings[attribute_id];

            int[] positives_row = bins_positives[attribute_id];
            int[] negatives_row = bins_negatives[attribute_id];
            int[] newPositives_row = bins_newPositives[attribute_id];

            // iterate over examples
            for (int i = 0; i < n_examples; ++i) {

                int example_id = (int)(descriptions_row[i] & MASK_IDENTIFIER);
                int bid = (int)((descriptions_row[i] & MASK_BIN) >> OFFSET_BIN);

                int label = (int)dataset.getExample(example_id).getLabel();

                descriptions_row[i] |= FLAG_NEW; // mark as new
                descriptions_row[i] |= FLAG_COVERED; // mark as covered (empty rule)

                if (label == targetLabel) {
                    descriptions_row[i] |= FLAG_POSITIVE; // mark as positive
                    ++positives_row[bid];
                    ++newPositives_row[bid];
                } else {
                    descriptions_row[i] &= ~FLAG_POSITIVE; // mark as negative
                    ++negatives_row[bid];
                }

            }
        }

        // reset rule ranges


        Logger.log("Reset arrays for class " + targetLabel + "\n", Level.FINER);
        printArrays();

        // make copies of arrays that are altered during induction
        arrayCopies.put("bins_positives", (Object)Arrays.stream(bins_positives).map(int[]::clone).toArray(int[][]::new));
        arrayCopies.put("bins_newPositives", (Object)Arrays.stream(bins_newPositives).map(int[]::clone).toArray(int[][]::new));
        arrayCopies.put("bins_negatives", (Object)Arrays.stream(bins_negatives).map(int[]::clone).toArray(int[][]::new));

    }

    protected void printArrays() {

        if (false) {
            return;
        }

        int prev_pn = -1;
        boolean ok = true;

        for (IAttribute attr: trainSet.getAttributes()) {
            int attribute_id = attr.getTableIndex();

            int bin_p = 0, bin_n = 0, bin_new_p = 0, bin_outside = 0;

            boolean opposite = ruleRanges[attribute_id][0] > ruleRanges[attribute_id][1]; // this indicate nominal opposite condition
            int lo = Math.min(ruleRanges[attribute_id][0], ruleRanges[attribute_id][1]);
            int hi = Math.max(ruleRanges[attribute_id][0], ruleRanges[attribute_id][1]);

            for (int i = 0; i < bins_positives[attribute_id].length; ++i) {

                if ((i >= lo &&  i < hi) || (opposite && (i < lo || i >= hi)) ) {
                    bin_p += bins_positives[attribute_id][i];
                    bin_n += bins_negatives[attribute_id][i];
                    bin_new_p += bins_newPositives[attribute_id][i];
                } else {
                    bin_outside += bins_positives[attribute_id][i] + bins_negatives[attribute_id][i];
                }
            }

            int p = 0, n = 0, new_p = 0;
            for (int i = 0; i < descriptions[attribute_id].length; ++i) {
                long desc = descriptions[attribute_id][i];

                if ((desc & FLAG_COVERED) != 0) {

                    if ((desc & FLAG_POSITIVE) != 0) {
                        ++p;

                        if ((desc & FLAG_NEW) != 0) {
                            ++new_p;
                        }

                    } else {
                        ++n;
                    }
                }
            }

            if ((bin_outside > 0) || (p != bin_p) || (n != bin_n) || (new_p != bin_new_p) || (prev_pn > 0 && prev_pn != p + n)) {
               ok = false;
            }

            prev_pn = p+n;

            Logger.log("" + attr.getName() + ": [" + ruleRanges[attribute_id][0] + "," + ruleRanges[attribute_id][1] + "],"
                    + "bin_p=" + bin_p + ", bin_n=" + bin_n + ", sum=" +  (bin_p + bin_n) + ", bin_new_p=" + bin_new_p + ", bin_outside=" + bin_outside
                    + ",\t\t p=" + p + ", n=" + n + ", sum=" +  (p + n) + ", new_p=" + new_p
                    + "\n", Level.FINER);

        }

        if (ok == false) {
            Logger.log("Error\n", Level.FINER);
         //   System.exit(0);

        }

      /*
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

       */
    }

    protected void updateMidpoint(IExampleSet dataset, ConditionCandidate candidate) {
        IAttribute bestAttr = dataset.getAttributes().get(candidate.getAttribute());


        // alter midpoint
        int attribute_id = bestAttr.getTableIndex();

        long[] cur_descriptions = descriptions[attribute_id];
        int[] cur_positives = bins_positives[attribute_id];
        int[] cur_negatives = bins_negatives[attribute_id];
        int[] cur_begins = bins_begins[attribute_id];

        // seek to the first non-empty bin on the left from the cutting point
        int bid = candidate.blockId - 1;
        while (cur_positives[bid] + cur_negatives[bid] == 0) {
            --bid;
        }

        // find the rightmost covered example in this bin
        int i = cur_begins[bid + 1] - 1;
        while ((cur_descriptions[i] & FLAG_COVERED) == 0L) {
            --i;
        }
        int left_id = (int) (cur_descriptions[i] & MASK_IDENTIFIER);

        // find the leftmost covered example in the bin right to the cutting point
        i = cur_begins[candidate.blockId];
        while ((cur_descriptions[i] & FLAG_COVERED) == 0L) {
            ++i;
        }
        int right_id = (int) (cur_descriptions[i] & MASK_IDENTIFIER);

        // recalculate interval
        double midpoint = (dataset.getExample(left_id).getValue(bestAttr) + dataset.getExample(right_id).getValue(bestAttr)) / 2;

        IValueSet interval = (candidate.opposite == false)
                ? Interval.create_le(midpoint)
                : Interval.create_geq(midpoint);
        candidate.setValueSet(interval);
    }
}
