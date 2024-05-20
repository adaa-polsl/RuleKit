package adaa.analytics.rules.data.row;

import adaa.analytics.rules.data.*;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.*;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import tech.tablesaw.api.DoubleColumn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class ExampleTest {

    @Test
    public void testGetValue() {
        MockExampleDataAccess  exampleSet = new MockExampleDataAccess();
        Example e = new Example(10, exampleSet);
        double v = e.getValue(new ColumnMetaData( "col1", EColumnType.NUMERICAL, EColumnRole.not_defined, new ArrayList<>(),exampleSet));
        Assert.assertEquals(10.0,v, 0.0);
    }

    @Test
    public void testGetNominalValue() {
        MockExampleDataAccess  exampleSet = new MockExampleDataAccess();
        Example e = new Example(0, exampleSet);
        List<String> values = new ArrayList<>();
        values.add("aaa");
        String v = e.getNominalValue(new ColumnMetaData( "col1", EColumnType.NOMINAL, EColumnRole.not_defined, values,exampleSet));
        Assert.assertEquals("aaa",v);
    }

    @Test
    public void testSetValue() {
        MockExampleDataAccess  exampleSet = new MockExampleDataAccess();
        Example e = new Example(0, exampleSet);
        List<String> values = new ArrayList<>();
        values.add("aaa");
        e.setValue(new ColumnMetaData( "col1", EColumnType.NOMINAL, EColumnRole.not_defined, values,exampleSet),"aaa");
        Assert.assertTrue(exampleSet.setInvoked);
    }

    class MockExampleDataAccess implements IExampleSet{
        boolean setInvoked = false;
        @Override
        public IAttributes getAttributes() {
            return null;
        }

        @Override
        public int getColumnIndex(String attributeName) {
            return 1;
        }

        @Override
        public double getDoubleValue(int colIdx, int rowIndex) {
            return rowIndex;
        }

        @Override
        public void setDoubleValue(IAttribute att, int rowIndex, double value) {
            setInvoked = true;
        }

        @Override
        public Object clone() {
            return null;
        }

        @Override
        public DataTableAnnotations getAnnotations() {
            return null;
        }

        @Override
        public void recalculateStatistics(EStatisticType stateType, String colName) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int columnCount() {
            return 0;
        }

        @Override
        public void sortBy(String columnName, EColumnSortDirections sortDir) {

        }

        @Override
        public Example getExample(int var1) {
            return null;
        }

        @Override
        public IExampleSet filter(ICondition cnd) {
            return null;
        }

        @Override
        public IExampleSet filterWithOr(List<ICondition> cndList) {
            return null;
        }

        @Override
        public IExampleSet updateMapping(IExampleSet mappingSource) {
            return null;
        }


        @Override
        public void addNewColumn(IAttribute colMetaData) {

        }

        @Override
        public DoubleColumn getDoubleColumn(IAttribute attr) {
            return null;
        }

        @NotNull
        @Override
        public Iterator<Example> iterator() {
            return null;
        }
    }
}