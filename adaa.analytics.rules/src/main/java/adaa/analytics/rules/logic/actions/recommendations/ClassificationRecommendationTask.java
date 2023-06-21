package adaa.analytics.rules.logic.actions.recommendations;

import adaa.analytics.rules.logic.actions.MetaExample;
import adaa.analytics.rules.logic.actions.MetaValue;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.AnyValueSet;
import adaa.analytics.rules.logic.representation.IValueSet;
import adaa.analytics.rules.logic.representation.SingletonSet;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.omg.CORBA.Any;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class ClassificationRecommendationTask extends RecommendationTask {
    public int from;
    public int to;

    public ClassificationRecommendationTask(boolean enablePruning, boolean multipleRecommendations, ClassificationMeasure qualityMeasure, int sourceClassID, int targetClassID) {
        super(enablePruning, multipleRecommendations, qualityMeasure);
        from = sourceClassID;
        to = targetClassID;
    }

    @Override
    public void setExample(Example ex) {

    }

    @Override
    public IValueSet getSourceValue(Attribute label) {
        if (Double.compare(from, -1.0) == 0) return new AnyValueSet();
        return new SingletonSet(from, label.getMapping().getValues());
    }

    @Override
    public IValueSet getTargetValue(Attribute label) {
        return new SingletonSet(to, label.getMapping().getValues());
    }

    @Override
    public ExampleSet preprocessExamples(ExampleSet examples) {
        return examples;
    }

    public double rankMetaPremise(MetaExample metaPremise, ExampleSet examples) {
        Set<Integer> pos = new HashSet<>();
        Set<Integer> neg = new HashSet<>();
        Covering covering = metaPremise.getCoverageForClass(examples, to, pos, neg);

        if (covering.weighted_p < 1.0) {
            return 0.0;}

        double measure = this.measure.calculate(examples, covering);
        return Double.isNaN(measure) ? Double.MIN_VALUE : measure;
    }

    public MetaValue getBestMetaValue(Set<String> allowedAttributes, Map<String, Set<MetaValue>> metaValuesByAttributeLocal, MetaExample contra, ExampleSet examples) {

        double best_quality = Double.NEGATIVE_INFINITY;

        Stream<Map.Entry<String, Set<MetaValue>>> allowed =
                metaValuesByAttributeLocal
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

                                double q = rankMetaPremise(localContra, examples);

                                localContra.remove(cand);

                                if (q >= Q) {

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
}
