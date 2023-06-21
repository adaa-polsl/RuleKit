package adaa.analytics.rules.logic.actions.recommendations;

import adaa.analytics.rules.logic.actions.MetaExample;
import adaa.analytics.rules.logic.actions.MetaValue;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.ActionRule;
import adaa.analytics.rules.logic.representation.IValueSet;
import com.lowagie.text.Meta;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

import java.util.Map;
import java.util.Set;

public abstract class RecommendationTask {
    protected boolean pruningEnabled;
    protected boolean generateMultipleRecommendations;
    protected MetaExample finalMetaexample;
    protected ExampleSet trainingSet;
    protected ClassificationMeasure measure;

    protected RecommendationTask(boolean enablePruning, boolean multipleRecommendations, ClassificationMeasure qualityFunction) {
        pruningEnabled = enablePruning;
        generateMultipleRecommendations = multipleRecommendations;
        measure = qualityFunction;
    }

    public abstract void setExample(Example ex);
    public void setFinalTargetMetaexample(MetaExample ex) {
        finalMetaexample = ex;
    }

    public abstract IValueSet getSourceValue(Attribute label);
    public abstract IValueSet getTargetValue(Attribute label);

    public ExampleSet preprocessExamples(ExampleSet examples) {
        trainingSet = examples;
        return  examples;
    }

    public ActionRule createRule() {
        return new ActionRule();
    }

    public boolean getPruningEnabled() { return  pruningEnabled; }
    public boolean getMultiplRecommendationsEnabled() { return generateMultipleRecommendations; }
    public abstract double rankMetaPremise(MetaExample metaPremise, ExampleSet examples);
    public abstract MetaValue getBestMetaValue(Set<String> allowedAttributes, Map<String, Set<MetaValue>> metaValuesByAttribute, MetaExample contra, ExampleSet examples) ;
}
