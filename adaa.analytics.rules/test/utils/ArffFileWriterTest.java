package utils;

import adaa.analytics.rules.rm.comp.TsExampleSet;
import adaa.analytics.rules.rm.example.IExampleSet;
import org.junit.Test;

import java.nio.file.Paths;

public class ArffFileWriterTest {

    @Test
    public void ArffFileWriteTest() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-result.arff").toString();

        Object[][] values = new Object[][]{
                {1.0, "val1", true, 5, 6},
                {2.0, "val2", false, 4, 3},
                {4.0, "val1", false, 7, 2},
                {10.0, "val1", true, 1, 6},
                {5.0, "val2", false, 2, 7}
        };

        String[] attsName = new String[]{ "d_att", "t_att", "b_att", "att_1", "att_2" };

        IExampleSet es = new TsExampleSet(values, attsName, "t_att", "d_att");

        ArffFileWriter.write(es, dataDir);
    }
}
