package utils;

import adaa.analytics.rules.data.DataTable;
import adaa.analytics.rules.rm.example.IExampleSet;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArffFileLoader extends TableSawLoader {

    @Deprecated
    @Override
    protected IExampleSet loadExampleSet(String path, String labelParameterName, String survivalTimeParameter) {

        List<AttributeInfo> attributesInfo = new ArrayList<>();

        CsvReadOptions.Builder builder = createArffLoadBuilder(path, attributesInfo);

        return loadExampleSet(builder, labelParameterName, survivalTimeParameter, attributesInfo);
    }

    @Override
    protected DataTable loadDataTable(String path, String labelParameterName, String survivalTimeParameter) {

        List<AttributeInfo> attributesInfo = new ArrayList<>();

        CsvReadOptions.Builder builder = createArffLoadBuilder(path, attributesInfo);

        return loadDataTable(builder, labelParameterName, survivalTimeParameter, attributesInfo);
    }

    private CsvReadOptions.Builder createArffLoadBuilder(String path, List<AttributeInfo> attributesInfo) {

        File file = new File(path);
        long dataPosition = findDataPosition(file, attributesInfo);

        BufferedReader bufferReader = null;
        if (dataPosition >= 0) {
            try {
                RandomAccessFile raf = new RandomAccessFile(file, "r");
                raf.seek(dataPosition);
                bufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD())));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Nie znaleziono znacznika '@data'.");
        }

        return CsvReadOptions.builder(bufferReader).header(false);
    }

    private long findDataPosition(File file, List<AttributeInfo> attributesInfo) {

        long position = -1;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            String line;
            while ((line = raf.readLine()) != null) {

                if(line.toLowerCase().trim().startsWith("@attribute")) {

                    attributesInfo.add(new AttributeInfo(line));
                }
                else if (line.toLowerCase().trim().startsWith("@data")) {

                    position = raf.getFilePointer();
                    break;
                }
            }
        } catch (IOException e) {

            e.printStackTrace();
        }
        return position;
    }
}
