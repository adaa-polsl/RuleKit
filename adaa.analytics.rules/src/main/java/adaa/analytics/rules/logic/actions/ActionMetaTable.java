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
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class ActionMetaTable {

    protected IExampleSet trainSet;

    List<Set<MetaValue>> metaValuesList;

    public ActionMetaTable(@NotNull ActionRangeDistribution distribution) {
        trainSet = (IExampleSet) distribution.set.clone();
        metaValuesList = distribution.getMetaValuesByAttribute();
    }

    public abstract List<MetaAnalysisResult> analyze(Example ex, RecommendationTask task);
 }
