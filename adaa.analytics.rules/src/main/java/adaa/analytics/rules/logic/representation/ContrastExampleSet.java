package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.rm.comp.TsExampleSet;
import adaa.analytics.rules.rm.example.IAttribute;

public class ContrastExampleSet extends TsExampleSet {

    protected IAttribute contrastAttribute;

    public IAttribute getContrastAttribute() { return contrastAttribute; }

    public ContrastExampleSet(TsExampleSet exampleSet) {
        super(exampleSet);

        contrastAttribute = (exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
    }
}




