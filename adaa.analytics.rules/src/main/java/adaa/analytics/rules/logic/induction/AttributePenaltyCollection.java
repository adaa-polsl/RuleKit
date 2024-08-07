package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.INominalMapping;

import java.util.Map;
import java.util.TreeMap;

public class AttributePenaltyCollection {

    private Map<Integer, AttributePenalty> penalties = new TreeMap<Integer, AttributePenalty>();

    private InductionParameters params;

    public AttributePenaltyCollection(InductionParameters params) {
        this.params = params;
    }

    public AttributePenalty get(int i) {
        return penalties.get(i);
    }

    public void init(IExampleSet trainSet) {
        penalties.clear();

        // each group requires its own penalty object
        final IAttribute outputAttr = (trainSet.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? trainSet.getAttributes().getLabel()
                : trainSet.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

        INominalMapping mapping = outputAttr.getMapping();

        for (int i = 0; i < mapping.size(); ++i) {
            // first covering - create penalty object
            penalties.put(i, new AttributePenalty(params.getPenaltyStrength(), params.getPenaltySaturation()));
        }
    }

    public void reset() {
        for (AttributePenalty ap : penalties.values()) {
            ap.reset();
        }
    }

    public void disableCompensation() {
        for (AttributePenalty ap : penalties.values()) {
            ap.disableCompensation();
        }
    }

}
