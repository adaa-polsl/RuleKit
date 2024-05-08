package ioutils;

import adaa.analytics.rules.data.metadata.EColumnType;
import org.apache.commons.lang3.text.StrTokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AttributeInfo {

    private String name;
    private EColumnType colType;
    private List<String> values;

    public AttributeInfo(String arffAttributeLine) {

        Pattern pattern = Pattern.compile("\\{(.*?)\\}");
        Matcher matcher = pattern.matcher(arffAttributeLine);

        String type = null;

        if (matcher.find()) {
            type = matcher.group(0);
            arffAttributeLine = matcher.replaceAll("").trim();
        }

        StrTokenizer globalTokenizer = new StrTokenizer(arffAttributeLine);
        globalTokenizer.setDelimiterChar(' ');
        globalTokenizer.setQuoteChar('\'');
        String[] tokens = globalTokenizer.getTokenArray();

        if (tokens[0].equalsIgnoreCase("@attribute")) {

            name = tokens[1];
            if(type == null) {
                type = tokens[2].trim();
            }

            if (type.equalsIgnoreCase("numeric") ||
                    type.equalsIgnoreCase("real")) {
                colType = EColumnType.NUMERICAL;
            } else if (type.startsWith("{") && type.endsWith("}")) {
                type = type.substring(1, type.length() - 1);
                StrTokenizer typeTokenizer = new StrTokenizer(type);
                typeTokenizer.setDelimiterChar(',');
                typeTokenizer.setQuoteChar('\'');
                typeTokenizer.setTrimmerMatcher(StrTokenizer.getCSVInstance().getTrimmerMatcher());
                values = Arrays.asList(typeTokenizer.getTokenArray());
                colType = EColumnType.NOMINAL;
            } else {
                colType = EColumnType.OTHER;
            }
        }
    }

    public AttributeInfo(String name, EColumnType cellType, List<?> values) {
        this.name = name;
        this.colType = cellType;
        this.values = (values != null && cellType == EColumnType.NOMINAL) ?
                values.stream().map(Object::toString).collect(Collectors.toList()) : null;
    }


    public String getName() {
        return name;
    }

    public EColumnType getCellType() {
        return colType;
    }

    public List<String> getValues() {
        return values;
    }
}