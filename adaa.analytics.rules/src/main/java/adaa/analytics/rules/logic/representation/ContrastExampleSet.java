package adaa.analytics.rules.logic.representation;

import adaa.analytics.rules.data.DataColumnDoubleAdapter;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.DataTableAnnotations;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ContrastExampleSet implements IExampleSet {

    private IExampleSet delegateExampleSet;

    protected IAttribute contrastAttribute;

    public IAttribute getContrastAttribute() { return contrastAttribute; }

    public ContrastExampleSet(IExampleSet exampleSet) {
        this.delegateExampleSet = exampleSet;

        contrastAttribute = (exampleSet.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE) == null)
                ? exampleSet.getAttributes().getLabel()
                : exampleSet.getAttributes().getColumnByRole(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);
    }

    @Override
    public DataTableAnnotations getAnnotations() {
        return delegateExampleSet.getAnnotations();
    }

    @Override
    public Object clone() {
        return delegateExampleSet.clone();
    }

    @Override
    public boolean equals(Object var1) {
        return delegateExampleSet.equals(var1);
    }

    @Override
    public int hashCode() {
        return delegateExampleSet.hashCode();
    }

    @Override
    public IAttributes getAttributes() {
        return delegateExampleSet.getAttributes();
    }

    @Override
    public int size() {
        return delegateExampleSet.size();
    }


    @Override
    public Example getExample(int var1) {
        return delegateExampleSet.getExample(var1);
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        return delegateExampleSet.filter(cnd);
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        return delegateExampleSet.filterWithOr(cndList);
    }

    @Override
    public IExampleSet updateMapping(IExampleSet mappingSource) {
        return delegateExampleSet.updateMapping(mappingSource);
    }


    @Override
    public Object[] getValues(String colName) {
        return delegateExampleSet.getValues(colName);
    }

    @NotNull
    @Override
    public Iterator<Example> iterator() {
        return delegateExampleSet.iterator();
    }

    @Override
    public void forEach(Consumer<? super Example> action) {
        delegateExampleSet.forEach(action);
    }

    @Override
    public Spliterator<Example> spliterator() {
        return delegateExampleSet.spliterator();
    }

    @Override
    public void sortBy(String columnName, EColumnSortDirections sortDir) {
        delegateExampleSet.sortBy(columnName, sortDir);
    }

    @Override
    public int addAttribute(IAttribute var1) {
        return delegateExampleSet.addAttribute(var1);
    }

    @Override
    public DataColumnDoubleAdapter getDataColumnDoubleAdapter(IAttribute attr, double defaultValue) {
        return delegateExampleSet.getDataColumnDoubleAdapter(attr, defaultValue);
    }

}




