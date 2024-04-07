package utils;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IExampleSet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ArffFileWriter {
    public static void write(IExampleSet exampleSet, String fileName) {

        List<IAttribute> attributes = new ArrayList<>();

        try {
            FileWriter fileWriter = new FileWriter(fileName);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            bufferedWriter.write("@relation ''");
            bufferedWriter.newLine();
            bufferedWriter.newLine();

            for (Iterator<IAttribute> it = exampleSet.getAttributes().allAttributes(); it.hasNext(); ) {

                IAttribute attribute = it.next();
                attributes.add(attribute);

                String line = "@attribute '" + attribute.getName() + "' ";
                if(attribute.isNumerical()) {

                    line += "numeric";
                }
                else if(attribute.isNominal()) {

                    List<String> values = attribute.getMapping().getValues();
                    StringJoiner joiner = new StringJoiner("','", "{'", "'}");
                    for(String value : values) {

                        joiner.add(value);
                    }
                    line += joiner.toString();
                }

                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            bufferedWriter.newLine();
            bufferedWriter.write("@data");
            bufferedWriter.newLine();

            for(int i=0 ; i<exampleSet.size() ; i++) {

                StringBuilder lineBuilder = new StringBuilder();

                for(IAttribute att : attributes) {

                    if(att.isNumerical()) {
                        double val = exampleSet.getExample(i).getNumericalValue(att);
                        lineBuilder.append(Double.toString(val)).append(",");
                    }
                    else if(att.isNominal()) {
                        String val = exampleSet.getExample(i).getNominalValue(att);
                        lineBuilder.append("'").append(val).append("',");
                    }
                }

                String line = lineBuilder.toString();
                line = line.substring(0, line.length()-1);

                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }

            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
