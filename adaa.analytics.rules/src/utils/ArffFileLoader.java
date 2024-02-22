package utils;

import adaa.analytics.rules.rm.example.IExampleSet;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ArffFileLoader extends TableSawLoader {

    @Override
    protected IExampleSet loadExampleSet(String path, String labelParameterName, String survivalTimeParameter) {

        List<AttributeInfo> attributesNames = new ArrayList<>();

        File file = new File(path);
        long dataPosition = findDataPosition(file, attributesNames);

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

        CsvReadOptions.Builder builder = CsvReadOptions.builder(bufferReader)
                .header(false);

        return loadExampleSet(builder, labelParameterName, survivalTimeParameter, attributesNames);
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
