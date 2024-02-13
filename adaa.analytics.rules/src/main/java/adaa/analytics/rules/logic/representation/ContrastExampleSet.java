package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.set.SimpleExampleSet;

public class ContrastExampleSet extends SimpleExampleSet {

    protected IAttribute contrastAttribute;

    public IAttribute getContrastAttribute() { return contrastAttribute; }

    public ContrastExampleSet(SimpleExampleSet exampleSet) {
        super(exampleSet);

        contrastAttribute = (exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
    }
}




