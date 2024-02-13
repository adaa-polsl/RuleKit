package adaa.analytics.rules.logic.actions.recommendations;

import adaa.analytics.rules.logic.actions.MetaExample;
import adaa.analytics.rules.logic.actions.MetaValue;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.induction.RegressionActionInductionParameters;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.SortedExampleSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class RegressionRecommendationTask extends RecommendationTask {

    RegressionActionInductionParameters.RegressionOrder order;
    Example sourceExample;

    public RegressionRecommendationTask(boolean enablePruning, boolean multipleRecommendations, ClassificationMeasure qualityMeasure, RegressionActionInductionParameters.RegressionOrder order) {
        super(enablePruning, multipleRecommendations, qualityMeasure);
        this.order = order;
    }

    @Override
    public ActionRule createRule() {
        return new RegressionActionRule();
    }

    @Override
    public void setExample(Example ex) {
        sourceExample = ex;
    }

    @Override
    public IValueSet getSourceValue(IAttribute label) {
        return new SingletonSet(sourceExample.getLabel(), null);
    }

    @Override
    public IValueSet getTargetValue(IAttribute label) {
        RegressionRule r = metaPremise2Rule(finalMetaexample, label);
        r.setCoveringInformation(r.covers(trainingSet));
        return new SingletonSet(r.getConsequenceValue(), null);
    }

    @Override
    public IExampleSet preprocessExamples(IExampleSet examples) {
        trainingSet = new SortedExampleSet(examples, examples.getAttributes().getLabel(), SortedExampleSet.INCREASING);
        return trainingSet;
    }

    private RegressionRule metaPremise2Rule(MetaExample me, IAttribute label) {
        CompoundCondition cc = new CompoundCondition();

        me.toPremise().forEach((String attr, ElementaryCondition ec) -> cc.addSubcondition(ec));

        return new RegressionRule(cc, new ElementaryCondition(label.getName(), new SingletonSet(Double.NaN, null)));
    }

    @Override
    public double rankMetaPremise(MetaExample metaPremise, IExampleSet examples) {

        Covering covering = new Covering();

        RegressionRule rule = metaPremise2Rule(metaPremise, examples.getAttributes().getLabel());

        covering = rule.covers(examples);

        if (covering.weighted_p < 1.0) {
            return 0.0;}

        double measure = this.measure.calculate(examples, covering);
        return Double.isNaN(measure) ? Double.MIN_VALUE : measure;
    }

    @Override
    public MetaValue getBestMetaValue(Set<String> allowedAttributes, Map<String, Set<MetaValue>> metaValuesByAttribute, MetaExample contra, IExampleSet examples) {
        double best_quality = Double.NEGATIVE_INFINITY;

        Stream<Map.Entry<String, Set<MetaValue>>> allowed =
                metaValuesByAttribute
                        .entrySet()
                        .stream()
                        .filter(x -> allowedAttributes.contains(x.getKey()));

        String[] attributes = allowedAttributes.toArray(new String[0]);
        Map<String, Integer> atrToInt = new HashMap<>();
        CopyOnWriteArrayList<MetaValue> candidates = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Double> qualities = new CopyOnWriteArrayList<>();

        for (int i = 0; i < attributes.length; i++) {

            atrToInt.put(attributes[i], i);
            qualities.add(best_quality);
            candidates.add(null);
        }



        allowed
                .parallel()
                .forEach(
                        (x) -> {
                            MetaExample localContra = new MetaExample(contra);
                            Integer index = atrToInt.get(x.getKey());
                            double Q = qualities.get(index);

                            for (MetaValue cand : x.getValue()) {

                                localContra.add(cand);
                                double q = Double.MIN_VALUE;
                                RegressionRule rule = metaPremise2Rule(localContra, examples.getAttributes().getLabel());
                                Covering cov = rule.covers(examples);
                                rule.setCoveringInformation(cov);
                                try {
                                    q = measure.calculate(examples, cov);
                                } catch (AssertionError ex) {
                                    continue;
                                }
                                localContra.remove(cand);

                                if (q >= Q && isOrderCorrect(sourceExample.getLabel(), rule.getConsequenceValue())) {

                                    candidates.set(index, cand);
                                    qualities.set(index, q);
                                    Q = q;
                                }
                            }
                        }
                );

        if (qualities.isEmpty())
            return null;
        Optional<Double> max = qualities.stream().max(Double::compareTo);
        int idx = qualities.indexOf(max.get());
        return candidates.get(idx);
    }

    private boolean isOrderCorrect(double source, double target) {
        switch (order) {
            case BETTER:
                return target > source;
            case WORSE:
                return source < target;
            case ANY:
                return Double.compare(source, target) != 0;
        }
        throw new RuntimeException("Unknown value of regression order.");
    }
}
