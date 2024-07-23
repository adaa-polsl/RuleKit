package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.*;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.logic.representation.rule.ContrastRule;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.DoubleColumn;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ContrastExampleSet extends ExampleSetWrapper {

    protected IAttribute contrastAttribute;

    public IAttribute getContrastAttribute() { return contrastAttribute; }

    public ContrastExampleSet(IExampleSet exampleSet) {
       super(exampleSet);

        contrastAttribute = (exampleSet.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
    }
}




