package adaa.analytics.rules.logic.actions;

import adaa.analytics.rules.logic.actions.recommendations.ClassificationRecommendationTask;
import adaa.analytics.rules.logic.actions.recommendations.RecommendationTask;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.induction.RegressionActionInductionParameters;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.CompoundCondition;
import adaa.analytics.rules.logic.representation.ElementaryCondition;
import adaa.analytics.rules.logic.representation.RegressionRule;
import adaa.analytics.rules.logic.representation.SingletonSet;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class ActionMetaTable {

    protected ExampleSet trainSet;

    List<Set<MetaValue>> metaValuesList;

    public ActionMetaTable(@NotNull ActionRangeDistribution distribution) {
        trainSet = (ExampleSet) distribution.set.clone();
        metaValuesList = distribution.getMetaValuesByAttribute();
    }

    public abstract List<MetaAnalysisResult> analyze(Example ex, RecommendationTask task);
 }
