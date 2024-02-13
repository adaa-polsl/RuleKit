package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
import adaa.analytics.rules.logic.actions.recommendations.RecommendationTask;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class OptimizedActionMetaTable extends ActionMetaTable {

    private final Map<String, Set<MetaValue>> metaValuesByAttribute = new HashMap<>();
    private final List<String> stableAttributes;

    public OptimizedActionMetaTable(ActionRangeDistribution distribution, List<String> stableAttributes){
        super(distribution);
        this.stableAttributes = stableAttributes;
        metaValuesList.forEach(metaValues -> {
            Set<MetaValue> toAdd = new HashSet<>(metaValues);
            metaValuesByAttribute.put(metaValues.stream().findAny().get().getAttribute(), toAdd);
        });
    }

    @Override
    public List<MetaAnalysisResult> analyze(Example ex, RecommendationTask task) {
        List<MetaAnalysisResult> ret = new ArrayList<>();
        MetaExample primeMe = new MetaExample();
        HashSet<String> allowedAttributes = new HashSet<>();

        task.setExample(ex);
        trainSet = task.preprocessExamples(trainSet);

        //Deep copy - it is enough that we copy the references of MetaValues, as we do not modify the meta-values later on
        Map<String, Set<MetaValue>> metaValuesByAttributeLocal = new HashMap<>();
        for (Map.Entry<String, Set<MetaValue>> entry : metaValuesByAttribute.entrySet()) {
            metaValuesByAttributeLocal.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }

        List<MetaValue> proposedPrime = new CopyOnWriteArrayList<>();

        metaValuesByAttributeLocal
                .entrySet()
                .parallelStream()
                .forEach(metas -> metas.getValue().stream().filter(mv -> mv.contains(ex)).findFirst().ifPresent(proposedPrime::add));
        //can't add directly to primeMe, because it is not thread safe
        //TODO consider making it thread safe
        proposedPrime.forEach(primeMe::add);

        if (primeMe.getAllValues().isEmpty()) {
            return new ArrayList<>();
            //throw new RuntimeException("The example ex was not covered by any metaexample");
        }


        metaValuesByAttributeLocal.forEach((String x, Set<MetaValue> y) -> y.removeIf(z -> z.contains(ex)));

        while(true) {
            Iterator<IAttribute> it = trainSet.getAttributes().allAttributes();

            it.forEachRemaining(x -> {
                        if (x.equals(trainSet.getAttributes().getLabel())) {
                            return;
                        }
                        if (stableAttributes.contains(x.getName())){
                            return;
                        }
                        allowedAttributes.add(x.getName());
                    }
            );

            MetaExample contraMe = new MetaExample();
            double bestQ = task.rankMetaPremise(contraMe, trainSet);
            boolean grown = true;
            boolean pruned = task.getPruningEnabled();
            while (grown) {
                MetaValue best = task.getBestMetaValue(allowedAttributes, metaValuesByAttributeLocal, contraMe, trainSet);

                if (best == null) {
                    break;
                }
                contraMe.add(best);
                double currQ = task.rankMetaPremise(contraMe, trainSet);

                if (currQ >= bestQ) {
                    allowedAttributes.remove(best.getAttribute());

                    bestQ = currQ;
                    grown = true;
                    Logger.log("Found best meta-value: " + best + " at quality " + bestQ + "\r\n", Level.FINE);
                } else {
                    contraMe.remove(best);
                    grown = false;
                }
                if (Double.compare(currQ, 1.0) == 0) {
                    grown = false;
                }
            }



            while (pruned) {
                MetaValue candidateToRemoval = null;
                double currQ = 0.0;
                MetaExample currentValues = new MetaExample(contraMe);
                for (MetaValue mv : currentValues.getAllValues()) {
                    contraMe.remove(mv);

                    double q = task.rankMetaPremise(contraMe, trainSet);

                    if (q >= currQ) {
                        currQ = q;
                        candidateToRemoval = mv;
                    }

                    contraMe.add(mv);
                }

                if (candidateToRemoval != null && currQ >= bestQ) {
                    contraMe.remove(candidateToRemoval);
                    bestQ = currQ;
                } else {
                    pruned = false;
                }
            }

            if (contraMe.getSize() == 0) {

                break;
            } else {

                for (MetaValue mv :  contraMe.getAllValues()) {
                    metaValuesByAttributeLocal.get(mv.getAttribute()).remove(mv);
                }
            }
            task.setFinalTargetMetaexample(contraMe);
            MetaAnalysisResult result = new MetaAnalysisResult(ex, primeMe, contraMe, task, trainSet);
            ret.add(result);

            if (!task.getMultiplRecommendationsEnabled())
                break;
        }
        return ret;
    }


}
