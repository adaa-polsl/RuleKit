package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IAttributes;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

public class ColumnMetadataMapTest {

    private ColumnMetadataMap prepareColumnMetadataMap()
    {
        ColumnMetadataMap cmm = new ColumnMetadataMap();
        cmm.add(new ColumnMetaData("columnName1",EColumnType.NOMINAL,EColumnRole.regular,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("columnName2",EColumnType.NOMINAL,EColumnRole.regular,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("columnName3",EColumnType.NOMINAL,EColumnRole.regular,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("labelColumn",EColumnType.NOMINAL,EColumnRole.label,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("weightColumn",EColumnType.NUMERICAL,EColumnRole.weight,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("costColumn",EColumnType.NUMERICAL,EColumnRole.cost,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("predictionColumn",EColumnType.NUMERICAL,EColumnRole.prediction,new ArrayList<>(), null));
        cmm.add(new ColumnMetaData("confidenceColumn",EColumnType.NUMERICAL,EColumnRole.confidence.toString()+"_class",new ArrayList<>(), null));

        return cmm;
    }


    @Test
    public void testGetColumnNames() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Set<String> namesSet =  cmm.getColumnNames();
        Assert.assertTrue(namesSet.contains("columnName1"));
        Assert.assertTrue(namesSet.contains("columnName2"));
        Assert.assertTrue(namesSet.contains("columnName3"));
    }


    @Test
    public void testGet() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();

        Assert.assertNotNull(cmm.get("columnName1"));

        Assert.assertNull(cmm.get("abcd"));


        Assert.assertNotNull(cmm.get("labelColumn"));

        Assert.assertNotNull(cmm.get("label"));
        try {
            cmm.get("regular");
            Assert.fail("Should be fail (too many columns)");
        }catch(Exception ex)
        {}
    }

    @Test
    public void testGetRegular() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();

        Assert.assertNotNull(cmm.getRegular("columnName1"));
        Assert.assertNull(cmm.getRegular("weightLabel"));
    }

    @Test
    public void testRegularSize() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();

        Assert.assertEquals(3,cmm.regularSize());
    }

    @Test
    public void testGetWeight() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getWeight());
    }

    @Test
    public void testGetCost() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getCost());
    }

    @Test
    public void testGetConfidence() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getConfidence("class"));
    }


    @Test
    public void testGetPredictedLabel() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getPredictedLabel());
    }

    @Test
    public void testGetColumnByRole() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getColumnByRole("weight"));
        try {
            cmm.getColumnByRole("regular");
            Assert.fail("Should be fail (too many columns)");
        }catch(Exception ex)
        {}
    }

    @Test
    public void testGetLabel() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getLabel());
        cmm = new ColumnMetadataMap();
        Assert.assertNull(cmm.getLabel());

    }

    @Test
    public void testSetGetRegular() {
        ColumnMetadataMap cmm = new ColumnMetadataMap();
        cmm.add(new ColumnMetaData("columnName1",EColumnType.NOMINAL,EColumnRole.not_defined,new ArrayList<>(), null));
        Assert.assertNull(cmm.getRegular("columnName1"));
        cmm.setRegularRole(cmm.get("columnName1"));
        Assert.assertNotNull(cmm.getRegular("columnName1"));
        cmm.removeRegularRole(cmm.getRegular("columnName1"));
        Assert.assertNull(cmm.getRegular("columnName1"));
    }

    @Test
    public void testSetGetLabel() {
        ColumnMetadataMap cmm = new ColumnMetadataMap();
        cmm.add(new ColumnMetaData("columnName1",EColumnType.NOMINAL,EColumnRole.not_defined,new ArrayList<>(), null));
        Assert.assertNull(cmm.getLabel());
        cmm.setLabel(cmm.get("columnName1"));
        Assert.assertNotNull(cmm.getLabel());
    }

    @Test
    public void testSetGetPredicted() {
        ColumnMetadataMap cmm = new ColumnMetadataMap();
        cmm.add(new ColumnMetaData("columnName1",EColumnType.NOMINAL,EColumnRole.not_defined,new ArrayList<>(), null));
        Assert.assertNull(cmm.getPredictedLabel());
        cmm.setPredictedLabel(cmm.get("columnName1"));
        Assert.assertNotNull(cmm.getPredictedLabel());
    }

    @Test
    public void testGetColumnByRoleUnsafe() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        Assert.assertNotNull(cmm.getColumnByRoleUnsafe("weight"));
        Assert.assertNotNull(cmm.getColumnByRoleUnsafe("regular"));
        Assert.assertNotNull(cmm.getLabelUnsafe());
        cmm.setColumnRole("labelColumn",EColumnRole.not_defined.toString());
        Assert.assertNull(cmm.getLabelUnsafe());
    }

    @Test
    public void testUpdateMapping() {
        ColumnMetadataMap cmm = prepareColumnMetadataMap();
        ColumnMetadataMap cmm2 = new ColumnMetadataMap();
        cmm2.add(new ColumnMetaData("newColumn",EColumnType.NOMINAL,EColumnRole.not_defined,new ArrayList<>(), null));

        cmm.updateMapping(cmm2,null);
        IAttribute nc = cmm.get("newColumn");
        Assert.assertNotNull(nc);

        Assert.assertFalse(nc==cmm2.get("newColumn"));
    }

}