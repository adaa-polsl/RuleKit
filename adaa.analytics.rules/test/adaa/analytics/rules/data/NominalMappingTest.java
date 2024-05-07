package adaa.analytics.rules.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class NominalMappingTest {
    private static NominalMapping nominalMapping;
    @BeforeClass
    public static void setUp() throws Exception {
        nominalMapping = new NominalMapping();
        nominalMapping.addValue("aaa");
        nominalMapping.addValue("bbb");
        nominalMapping.addValue("\"aaa\"");
        nominalMapping.addValue("\"ccc\"");
    }


    @Test
    public void testGetValue() {
        Assert.assertEquals("aaa",nominalMapping.getValue(0));
        Assert.assertEquals("ccc",nominalMapping.getValue(2));
        try {
             nominalMapping.getValue(3);
             Assert.fail("Should be exception");
        }catch(Exception ex){}
    }

    @Test
    public void testGetIndex() {
        Assert.assertEquals(2,nominalMapping.getIndex("ccc").intValue());
        Assert.assertEquals(2,nominalMapping.getIndex("\"ccc\"").intValue());
        Assert.assertNull(nominalMapping.getIndex("ddd"));
    }

    @Test
    public void testSize() {
        Assert.assertEquals(3,nominalMapping.size());
    }
}