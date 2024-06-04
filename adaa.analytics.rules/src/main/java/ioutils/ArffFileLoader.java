package ioutils;

import adaa.analytics.rules.data.DataTable;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.*;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ArffFileLoader extends TableSawLoader {

    @Override
    public DataTable loadDataTable(String path, String labelParameterName, String survivalTimeParameter) throws IOException {

        List<AttributeInfo> attributesInfo = new ArrayList<>();

        CsvReadOptions.Builder builder = createArffLoadBuilder(path, attributesInfo);

        return loadDataTable(builder, labelParameterName, survivalTimeParameter, attributesInfo);
    }

    private CsvReadOptions.Builder createArffLoadBuilder(String path, List<AttributeInfo> attributesInfo) throws IOException {

        File file = new File(path);
        long dataPosition = findDataPosition(file, attributesInfo);

        BufferedReader bufferReader = null;
        if (dataPosition >= 0) {
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(dataPosition);
            BufferedReader inBufferReader = new BufferedReader(new InputStreamReader(new FileInputStream(raf.getFD())));
            bufferReader = filterFileData(inBufferReader);
        } else {
            System.out.println("Nie znaleziono znacznika '@data'.");
        }

        return CsvReadOptions.builder(bufferReader).header(false);
    }

    private long findDataPosition(File file, List<AttributeInfo> attributesInfo) throws IOException {

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
        }
        return position;
    }

    private BufferedReader filterFileData(BufferedReader inReader) throws IOException {
        List<String> filteredLines = new ArrayList<>();

        String inLine;
        while ((inLine = inReader.readLine()) != null) {
            if (!inLine.trim().startsWith("%")) {
                filteredLines.add(inLine);
            }
        }

        StringBuilder filteredData = new StringBuilder();
        for (String line : filteredLines) {
            filteredData.append(line).append("\n");
        }

        return new BufferedReader(new StringReader(filteredData.toString()));
    }
}
