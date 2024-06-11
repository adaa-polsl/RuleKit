package adaa.analytics.rules.logic.representation;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeValueFilterSingleCondition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.tools.ExpressionEvaluationException;

import java.util.ArrayList;
import java.util.List;

public class ContrastExampleSet extends SimpleExampleSet implements IContrastExampleSet {

    protected Attribute contrastAttribute;

    public Attribute getContrastAttribute() { return contrastAttribute; }

    public ContrastExampleSet(SimpleExampleSet exampleSet) {
        super(exampleSet);

        contrastAttribute = (exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getSpecial(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
    }

    public ContrastExampleSet(ContrastExampleSet rhs) {
        super(rhs);
        this.contrastAttribute = rhs.contrastAttribute;
    }
}




