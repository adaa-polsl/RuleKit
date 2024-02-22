package utils;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.set.SimpleExampleSet;
import adaa.analytics.rules.rm.example.table.*;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ArffFileWriterTest {

    @Test
    public void ArffFileWriteTest() throws Exception {

        String workingDir = System.getProperty("user.dir");
        String dataDir = Paths.get(workingDir, "/test/resources/data/unit-test-result.arff").toString();

        List<IAttribute> attributes = new ArrayList<>(3);

        IAttribute col_1 = new BinominalAttribute("Col 1");
        BinominalMapping biMapping = new BinominalMapping();
        biMapping.mapString("val_1");
        biMapping.mapString("val 2");
        col_1.setMapping(biMapping);

        IAttribute col_2 = new NumericalAttribute("Col_2");

        IAttribute col_3 = new BinominalAttribute("Col 3");
        PolynominalMapping polyMapping = new PolynominalMapping();
        polyMapping.mapString("val 1");
        polyMapping.mapString("val_2");
        polyMapping.mapString("v a l 3");
        col_3.setMapping(polyMapping);

        attributes.add(col_1);
        attributes.add(col_2);
        attributes.add(col_3);

        IAttribute[] attributeArray = attributes.toArray(new IAttribute[attributes.size()]);

        MemoryExampleTable meTable = new MemoryExampleTable(attributes);

        DataRowFactory dataRowFactory = new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY);
        String [] strRow = null;
        strRow = new String[] {"val_1", "1", "val 1"};
        meTable.addDataRow(dataRowFactory.create(strRow, attributeArray));
        strRow = new String[] {"val 2", "2.0", "val_2"};
        meTable.addDataRow(dataRowFactory.create(strRow, attributeArray));
        strRow = new String[] {"val 2", "3.3", "v a l 3"};
        meTable.addDataRow(dataRowFactory.create(strRow, attributeArray));

        SimpleExampleSet exampleSet = new SimpleExampleSet(meTable);

        ArffFileWriter.write(exampleSet, dataDir);
    }
}
