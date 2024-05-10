package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.quality.IQualityModifier;
import adaa.analytics.rules.logic.representation.condition.ConditionBase;
import adaa.analytics.rules.logic.representation.condition.ElementaryCondition;
import adaa.analytics.rules.logic.representation.MultiSet;
import adaa.analytics.rules.logic.representation.rule.Rule;

public class AttributePenalty implements IQualityModifier, IFinderObserver {
    protected double penaltyStrength;

    protected double new2allSaturation;

    protected double compensationFactor;

    protected MultiSet<String> rulesetAttributes = new MultiSet<>();

    protected MultiSet<String> currentRuleAttributes = new MultiSet<>();

    protected double cumulativePenalty = 0.0;

    protected boolean pruningMode = false;

    public void disableCompensation() { new2allSaturation = 1; }

    public void reset() {
        rulesetAttributes.clear();
        currentRuleAttributes.clear();
        cumulativePenalty = 0.0;
        pruningMode = false;
    }

    public AttributePenalty(double penaltyStrength, double penaltySaturation) {
        this.penaltyStrength = penaltyStrength;
        this.new2allSaturation = penaltySaturation;

        if (new2allSaturation < 1) {
            this.compensationFactor = 1.0 / (1.0 - new2allSaturation);
        }
    }

    @Override
    public double modifyQuality(double quality, String newAttribute, double p, double new_p) {
        if (penaltyStrength == 0) {
            return quality;
        }

        double penalty = cumulativePenalty;

        // consider new attribute
        if (newAttribute != null) {
            if (pruningMode) {
                if (currentRuleAttributes.getCount(newAttribute) == 1) {
                    // pruning - discard penalty if the last remaining instance of the attribute is removed
                    penalty -= calculatePenalty(newAttribute);
                }

            } else {
                if (!currentRuleAttributes.contains(newAttribute)) {
                    // growing - consider penalty only when attribute used for the first time
                    penalty += calculatePenalty(newAttribute);
                }
            }
        }

        double penaltyMultiplier = Math.max(0.01, 1.0 - penaltyStrength * penalty);
        double compensation = calculateCompensation(penaltyMultiplier, p, new_p);

        return quality * penaltyMultiplier * compensation;
    }

    @Override
    public void conditionAdded(ConditionBase cnd) {
        String attr = ((ElementaryCondition)cnd).getAttribute();

        if (!currentRuleAttributes.contains(attr)) {
            cumulativePenalty += calculatePenalty(attr);
        }

        currentRuleAttributes.add(attr);
    }

    @Override
    public void conditionRemoved(ConditionBase cnd) {
        String attr = ((ElementaryCondition)cnd).getAttribute();

        if (currentRuleAttributes.getCount(attr) == 1) {
            cumulativePenalty -= calculatePenalty(attr);
        }

        currentRuleAttributes.remove(attr);
    }

    @Override
    public void growingFinished(Rule r) {
        pruningMode = true;
    }

    @Override
    public void growingStarted(Rule r) {
        pruningMode = false;
        cumulativePenalty = 0;
        currentRuleAttributes.clear();
    }

    @Override
    public void ruleReady(Rule r) {
        for (String attr : r.getPremise().getAttributes()) {
            rulesetAttributes.add(attr);
        }
    }

    protected double calculatePenalty(String attr) {
        int queryAttributeCount = rulesetAttributes.getCount(attr);
        int allAttributesCount = rulesetAttributes.multisize();

        if (queryAttributeCount == 0 || allAttributesCount == 0 || penaltyStrength == 0) {
            return 0;
        }

        double penalty = (double)queryAttributeCount / allAttributesCount;
        return penalty;
    }

    protected double calculateCompensation(double penaltyMultiplier, double p, double new_p) {
       if (new2allSaturation == 1) {
           return 1; // no compensation
       }

        double new2all = Math.max(new_p / p, new2allSaturation);
        double maxCompensation = 1.0 / penaltyMultiplier;
        double compensation = compensationFactor * (maxCompensation - 1.0) * (new2all - new2allSaturation) + 1.0;

        return compensation;
    }
}
