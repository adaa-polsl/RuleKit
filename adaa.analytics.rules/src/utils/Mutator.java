package utils;

import adaa.analytics.rules.logic.actions.ActionMetaTable;
import adaa.analytics.rules.logic.actions.MetaAnalysisResult;
import adaa.analytics.rules.logic.actions.recommendations.RecommendationTask;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.logic.representation.model.ActionRuleSet;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.set.AttributeValueFilter;
import adaa.analytics.rules.rm.example.set.ConditionedExampleSet;
import org.apache.commons.math.stat.descriptive.rank.Median;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Mutator {

//    private MaterializeDataInMemory materializer;

    public Mutator() {
//        if (!RapidMiner.isInitialized()) {
//            RapidMiner.init();
//        }
//        materializer = OperatorService.createOperator(MaterializeDataInMemory.class);
    }

    public IExampleSet materializeExamples(IExampleSet set) {
        // TODO materializer
//        return  materializer.apply(set);
        return set;
    }

    private void mutateNominalAttribute(Example toBeMutated, IAttribute mutatedAttribute, Action suggestedMutation) {
        if (suggestedMutation.getActionNil()) return;
        double newValue = ((SingletonSet) suggestedMutation.getRightValue()).getValue();

        toBeMutated.setValue(mutatedAttribute, newValue);
    }

    private void mutateNumericalAttribute(Example toBeMutated, IAttribute mutatedAttribute, Action suggestedMutation, IExampleSet trainSet, String targetClassName) {
        Interval proposedInterval = ((Interval) suggestedMutation.getRightValue());
        if (proposedInterval == null)
            return;
        IAttribute classAtr = trainSet.getAttributes().getLabel();
        ConditionedExampleSet filtered = null;

        AttributeValueFilter condition = new AttributeValueFilter(
                trainSet,
                mutatedAttribute.getName() + " " + proposedInterval.getLeftSign() + " " + proposedInterval.getLeft() + " && " + mutatedAttribute.getName() + " " + proposedInterval.getRightSign() + " " + proposedInterval.getRight() + "&&" + classAtr.getName() + "=" + targetClassName);
        try {
            filtered = new ConditionedExampleSet(trainSet, condition);
        } catch( Exception ex) {
            System.out.println("Wrong expression in example filtering");
        }
        List<Double> vals = new LinkedList<>();
        if (filtered != null) {
            for (Example ex : filtered) {
                Double currentValue = ex.getValue(mutatedAttribute);
                vals.add(currentValue);
            }
        }

        Median median = new Median();
        double newValue = median.evaluate(vals.stream().mapToDouble(Double::doubleValue).toArray());

        toBeMutated.setValue(mutatedAttribute, newValue);
    }

//    private void mutateNumericalAttributeInRegression(Example toBeMutated, IAttribute mutatedAttribute, Action suggestedMutation, IExampleSet trainSet) {
//        Interval proposedInterval = ((Interval) suggestedMutation.getRightValue());
//        if (proposedInterval == null)
//            return;
//        ConditionedExampleSet filtered = null;
//
//        AttributeValueFilter condition = new AttributeValueFilter(
//                trainSet,
//                mutatedAttribute.getName() + " " + proposedInterval.getLeftSign() + " " + proposedInterval.getLeft() + " && " + mutatedAttribute.getName() + " " + proposedInterval.getRightSign() + " " + proposedInterval.getRight());
//        try {
//            filtered = new ConditionedExampleSet(trainSet, condition);
//        } catch( Exception ex) {
//            System.out.println("Wrong expression in example filtering");
//        }
//        List<Double> vals = new LinkedList<>();
//        if (filtered != null) {
//            for (Example ex : filtered) {
//                Double currentValue = ex.getValue(mutatedAttribute);
//                vals.add(currentValue);
//            }
//        }
//        // org.apache.commons.math3.stat.StatUtils.mode()
//        Median median = new Median();
//        double newValue = median.evaluate(vals.stream().mapToDouble(Double::doubleValue).toArray());
//
//        toBeMutated.setValue(mutatedAttribute, newValue);
//    }

//    public IExampleSet mutateExamples(IExampleSet toBeMutated, ActionMetaTable regressionMetaTable, ActionRuleSet usedRecommendations, IExampleSet trainSet, RegressionRecommendationTask task) throws OperatorException {
//        IExampleSet result = materializer.apply(toBeMutated);
//        if (result.getAttributes().getPredictedLabel() != null) {
//            result.getAttributes().remove(result.getAttributes().getPredictedLabel());
//        }
//
//        for (Example current: result) {
//            List<MetaAnalysisResult> recoms = regressionMetaTable.analyze(current, task);
//            if (recoms.isEmpty()) {
//                current.setValue(current.getAttributes().get("mutated_rule_id"), -1964);
//                continue;
//            }
//
//            MetaAnalysisResult toApply = recoms.get(0);
//            ActionRule ar = toApply.getActionRule();
//            ar.setCoveringInformation(ar.covers(trainSet));
//            usedRecommendations.addRule(ar);
//            mutateExampleForRegression(trainSet, result, current, ar);
//        }
//
//        return result;
//    }

    public IExampleSet mutateExamples(IExampleSet splitted, ActionMetaTable metaTable, String sourceClass, String targetClass, ActionRuleSet usedRecommendations, IExampleSet trainSet, RecommendationTask task) {

        // TODO materializer
//        IExampleSet result = materializer.apply(splitted);
        IExampleSet result = splitted;
        if (result.getAttributes().getPredictedLabel() != null) {
            result.getAttributes().remove(result.getAttributes().getPredictedLabel());
        }
        int failedCount = 0;


        for (Example current : result) {
            List<MetaAnalysisResult> recoms = new ArrayList<>();
            try {
                 recoms = metaTable.analyze(current, task);
            }catch(NullPointerException ex) {
                ex.printStackTrace();
            }
            if (recoms.isEmpty()) {
                failedCount++;
                current.setValue(current.getAttributes().get("mutated_rule_id"), -1964);
                continue;
            }
            MetaAnalysisResult golden = recoms.get(0);
            ActionRule asRule = golden.getActionRule();
            asRule.setCoveringInformation(asRule.covers(trainSet));

            usedRecommendations.addRule(asRule);
            mutateExampleInClassification(targetClass, trainSet, result, current, asRule);
            //  current.setValue(current.getAttributes().get("mutated_rule_id"), -1);

        }
        System.out.println("Failed to mutate by recoms " + failedCount + " examples");
        return result;
    }

    private void mutateExampleInClassification(String targetClass, IExampleSet trainSet, IExampleSet result, Example current, ActionRule asRule) {
        for (ConditionBase cond : asRule.getPremise().getSubconditions()) {
            Action action = (Action) cond;

            IAttribute attributeToMutate = result.getAttributes().get(action.getAttribute());
            if (attributeToMutate.isNominal()) {
                mutateNominalAttribute(current, attributeToMutate, action);
            } else {
                mutateNumericalAttribute(current, attributeToMutate, action, trainSet, targetClass);
            }
        }
    }

    private List<ActionRule> getApplicableRules(Example example, ActionRuleSet ruleSet) {
       return ruleSet.getRules()
                .stream()
                .map(ActionRule.class::cast)
                .filter(x -> x.getPremise().evaluate(example))
                .collect(Collectors.toList());
    }

//    private ActionRule getApplicableRule_sourceStrategy(List<ActionRule> applicableRules) {
//        applicableRules.sort(Comparator.comparingDouble(Rule::getWeight));
//        return applicableRules.get(applicableRules.size() - 1);
//    }

    private ActionRule getApplicableRule_targetStrategy(List<ActionRule> applicableRules) {

        applicableRules.sort(Comparator.comparingDouble(ActionRule::getWeightRight));
        return applicableRules.get(applicableRules.size() - 1);
    }

    public IExampleSet mutateExamples(IExampleSet splitted, ActionRuleSet ruleSet, IExampleSet trainSet, String targetClassName) {
        //ExampleSet result = new ExampleSet();
//        IExampleSet result = materializer.apply(splitted);
        // TODO materializer
        IExampleSet result = splitted;

        if (result.getAttributes().getPredictedLabel() != null) {
            result.getAttributes().remove(result.getAttributes().getPredictedLabel());
        }
        int failedCount = 0;
        for (Example current : result) {

            List<ActionRule> applicableRules = getApplicableRules(current, ruleSet);

            if (applicableRules.size() < 1) {
                failedCount++;
                continue;
            }


            ActionRule toApply = getApplicableRule_targetStrategy(applicableRules);

            mutateExampleInClassification(targetClassName, trainSet, result, current, toApply);
            current.setValue(current.getAttributes().get("mutated_rule_id"), ruleSet.getRules().indexOf(toApply) + 1);

        }
        System.out.println("Failed to mutate " + failedCount + " examples");
        return result;
    }

//    public IExampleSet mutateExamples(IExampleSet mutableExamples, ActionRuleSet regActionRules, IExampleSet trainSet) throws OperatorException {
//        IExampleSet result = materializer.apply(mutableExamples);
//
//        if (result.getAttributes().getPredictedLabel() != null) {
//            result.getAttributes().remove(result.getAttributes().getPredictedLabel());
//        }
//        int failedCount = 0;
//        for (Example current : result) {
//
//            List<ActionRule> applicableRules = regActionRules.getRules()
//                    .stream()
//                    .map(RegressionActionRule.class::cast)
//                    .filter(x -> x.getPremise().evaluate(current))
//                    .collect(Collectors.toList());
//
//            if (applicableRules.size() < 1) {
//                failedCount++;
//                continue;
//            }
//
//            applicableRules.sort(Comparator.comparingDouble(Rule::getWeight));
//            ActionRule toApply = applicableRules.get(0);
//
//            mutateExampleForRegression(trainSet, result, current, toApply);
//            current.setValue(current.getAttributes().get("mutated_rule_id"), regActionRules.getRules().indexOf(toApply) + 1);
//        }
//        System.out.println("Failed to mutate " + failedCount + " examples");
//        return result;
//    }

//    private void mutateExampleForRegression(IExampleSet trainSet, IExampleSet result, Example current, ActionRule toApply) {
//        for (ConditionBase cond : toApply.getPremise().getSubconditions()) {
//            Action action = (Action) cond;
//
//            IAttribute attributeToMutate = result.getAttributes().get(action.getAttribute());
//            if (attributeToMutate.isNominal()) {
//                mutateNominalAttribute(current, attributeToMutate, action);
//            } else {
//                mutateNumericalAttributeInRegression(current, attributeToMutate, action, trainSet);
//            }
//        }
//    }
}
