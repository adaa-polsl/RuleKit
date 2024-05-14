package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.data.DataTableAnnotations;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ColumnMetaDataTest {

    @Test
    public void testGetAsString() {
        List<String> values = new ArrayList<>();
        values.add("aaa");
        values.add("bbb");

        ColumnMetaData cmd = new ColumnMetaData("column1", EColumnType.NOMINAL, EColumnRole.label, values, null);
        Assert.assertEquals("aaa",cmd.getAsString(0));
        Assert.assertEquals("bbb",cmd.getAsString(1));
        try {
             cmd.getAsString(2);
            Assert.fail("Should have been exception");
        }catch(Exception ex)
        {}
        cmd = new ColumnMetaData("column1", EColumnType.NUMERICAL, EColumnRole.regular, new ArrayList<>(), null);
        Assert.assertEquals("?",cmd.getAsString(Double.NaN));
        Assert.assertEquals("?",cmd.getAsString(1));
    }

    @Test
    public void testGetMapping() {
        List<String> values = new ArrayList<>();
        values.add("aaa");
        values.add("bbb");

        ColumnMetaData cmd = new ColumnMetaData("column1", EColumnType.NOMINAL, EColumnRole.label, values, null);
        Assert.assertEquals("aaa",cmd.getMapping().mapIndex(0));
        Assert.assertEquals("bbb",cmd.getMapping().mapIndex(1));
        try {
            cmd.getMapping().mapIndex(2);
            Assert.fail("Should have been exception");
        }catch(Exception ex)
        {}


        Assert.assertEquals(Integer.valueOf(0),cmd.getMapping().getIndex("aaa"));
        Assert.assertEquals(Integer.valueOf(1),cmd.getMapping().getIndex("bbb"));
        Assert.assertEquals(null,cmd.getMapping().getIndex("ccc"));
    }

    @Test
    public void cloneWithNewOwner() {
        ColumnMetaData cmd = new ColumnMetaData("column1", EColumnType.NOMINAL, EColumnRole.label, null, null);
        ColumnMetaData cloned = cmd.cloneWithNewOwner(new DataTable(null, null, null));
        Assert.assertNotNull(cloned.getOwner());
    }

}