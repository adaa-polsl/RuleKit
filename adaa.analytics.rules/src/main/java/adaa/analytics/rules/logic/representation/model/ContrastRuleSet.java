package adaa.analytics.rules.logic.representation.model;

import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.induction.SetHelper;
import adaa.analytics.rules.logic.representation.*;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.operator.OperatorException;

import java.util.*;

public class ContrastRuleSet extends ClassificationRuleSet {

    private static final long serialVersionUID = 4631066124418857645L;

    public static final String OTHER_CLASSES = "<others>";

    private Map<String, List<ContrastRule>> sets = new TreeMap<String, List<ContrastRule>>();

    private Map<String, Integer> numDuplicates = new TreeMap<>();

    public List<ContrastRule> getAllSets() {
        List<ContrastRule> out = new ArrayList<ContrastRule>();
        for (String key: sets.keySet()) {
            List<ContrastRule> cs = sets.get(key);
            for (ContrastRule r : cs) {
                out.add(r);
            }
        }

        return out;
    }

    public int getTotalDuplicates() {
        int total = 0;
        for (int v: numDuplicates.values()) {
            total += v;
        }
        return total;
    }

    /**
     * Invokes base class constructor.
     *
     * @param exampleSet Training set.
     * @param isVoting   Voting flag.
     * @param params     Induction parameters.
     * @param knowledge  User's knowledge.
     */
    public ContrastRuleSet(IExampleSet exampleSet, boolean isVoting, InductionParameters params, Knowledge knowledge) {
        super(exampleSet, isVoting, params, knowledge);
    }

    /**
     * Calculates contrast sets indicators averaged within contrasts and then between contrasts.
     * @return Contrast set indicators.
     */
    public ContrastIndicators calculateAvgContrastIndicators() {
        ContrastIndicators global = new ContrastIndicators();

        // accumulate indicators from contrasts
        for (List<ContrastRule> contrast : sets.values()) {
            ContrastIndicators local = new ContrastIndicators(contrast);
            for (String k: global.values.keySet()) {
                global.values.put(k, global.get(k) + local.get(k));
            }
        }

        // average indicators over contrasts
        for (String k : global.values.keySet()) {
            global.values.put(k, global.get(k) / sets.size());
        }

       return global;
    }


    public double[] calculateAttributeStats() {

        double rulesPerAttr = 0;
        double totalCost = 0;

        // iterate over different contrasts
        for (String key : sets.keySet()) {
            List<ContrastRule> contrast = sets.get(key);

            int rid = 0;
            int topCount = 0;

            MultiSet<String> attributes = new MultiSet<>();

            double contrastCost = numDuplicates.get(key);

            // iterate over query rules
            for (int query_id = 0; query_id < contrast.size(); ++query_id) {
                ContrastRule q = contrast.get(query_id);
                Set<String> queryAttrs = q.getPremise().getAttributes();

                for (String a : queryAttrs) {
                    attributes.add(a);
                }

                contrastCost += q.getRedundancy();
            }

            totalCost += contrastCost / contrast.size();
            rulesPerAttr += (double)attributes.multisize() / attributes.size() / contrast.size();
        }

        double [] out = {
                rulesPerAttr / sets.size(),
                totalCost / sets.size()
        };
        return out;
    }

    public void add(String positiveClass, String negativeClass, ContrastRule rule) {

        String key = positiveClass  + " vs " + negativeClass;
        if (!sets.containsKey(key)) {
            sets.put(key, new ArrayList<ContrastRule>());
            numDuplicates.put(key, 0);
        }

        // calculate redundancy measure w.r.t. other contrasts 
        List<ContrastRule> dest = sets.get(key);
        double maxCost = 0;

        Set<String> queryAttrs = rule.getPremise().getAttributes();
        for (Rule r : dest) {
            Set<String> refAttrs = r.getPremise().getAttributes();

            double intersection = SetHelper.intersectionSize(queryAttrs, refAttrs);
            double union = queryAttrs.size() + refAttrs.size() - intersection;
            double attributeJaccard = intersection / union;

            intersection = r.getCoveredPositives().calculateIntersectionSize(rule.getCoveredPositives());
            union = rule.weighted_p + r.weighted_p - intersection;
            double exampleJaccard = intersection / union;

            double cost = attributeJaccard * exampleJaccard;
            if (cost > maxCost) {
                maxCost = cost;
            }
        }

        // maxCost == 1 -> duplicate
       if (maxCost < 1) {
            rule.setRedundancy(maxCost);
            rules.add(rule);
            dest.add(rule);
        } else {
           numDuplicates.replace(key, numDuplicates.get(key) + 1);
       }
    }

    /**
     * Predicts class label for a given example. Sets output attributes describing voting results.
     * @param example Example to be examined.
     * @return Predicted class label.
     * @throws OperatorException
     */
    @Override
    public double predict(Example example) throws OperatorException {
        throw new OperatorException("ContrastRuleSet should not be applied on any data.");
    }


    @Override
    public IExampleSet apply(IExampleSet exampleSet) throws OperatorException {
        return exampleSet;
    }

    /**
     * Generates text representation of the rule set which contains:
     * <p><ul>
     * <li>induction parameters,
     * <li>user's knowledge (if defined),
     * <li>list of rules,
     * <li>information about coverage of the training set examples.
     * </ul>
     * @return Rule set in the text form.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (params != null) {
            sb.append("Params:\n");
            sb.append(params.toString());
            sb.append("\n");
        }

        sb.append("Contrast sets:\n");

        int rid = 1;
        for (String key: sets.keySet()) {
            List<ContrastRule> cs = sets.get(key);
            sb.append(key + "(");
            ContrastIndicators cind = new ContrastIndicators(cs);
            for (String k: cind.values.keySet()) {
                sb.append( k + "=" + DoubleFormatter.format(cind.get(k)) + ", ");
            }
            sb.append(")\n");

            for (ContrastRule r : cs) {
                String aux = "cs-" + rid;
                sb.append(aux + ": " + r.toString() + " " + r.printStats() + "\n");
                ++rid;
            }
            sb.append("\n");
        }
        
        sb.replace(sb.length()-1, sb.length(), "\n");

        /*
        sb.append("\nCoverage of training examples by contrast sets (1-based):\n");
        for (int eid = 0; eid < trainingSet.size(); ++eid){
            Example ex = trainingSet.getExample(eid);

            List<Integer> matchingRules = new ArrayList<Integer>();

            rid = 1;
            for (Rule r: rules) {
                if (r.getPremise().evaluate(ex)) {
                    matchingRules.add(rid);
                }

                ++rid;
            }

            if (matchingRules.isEmpty()) {
                sb.append("-,");
            } else {
                for (int ruleId : matchingRules) {
                    sb.append(ruleId);
                    sb.append(",");
                }
            }

            sb.replace(sb.length() - 1, sb.length(), ";");
            //sb.append((bestRuleId > 0 ? bestRuleId : "-") + ",");
        }
         */

        return sb.toString();
    }
}
