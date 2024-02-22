package utils;

import org.apache.commons.lang3.text.StrTokenizer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeInfo {

    private String name;
    private EColType cellType;
    private String[] values;

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
                cellType = EColType.NUMERIC;
            } else if (type.startsWith("{") && type.endsWith("}")) {
                type = type.substring(1, type.length() - 1);
                StrTokenizer typeTokenizer = new StrTokenizer(type);
                typeTokenizer.setDelimiterChar(',');
                typeTokenizer.setQuoteChar('\'');
                values = typeTokenizer.getTokenArray();
                cellType = EColType.TEXT;
            } else {
                cellType = EColType.UNKNOWN;
            }
        }
    }

    public AttributeInfo(String name, EColType cellType, List<?> values) {

        this.name = name;
        this.cellType = cellType;
        this.values = null;
        if(values != null && this.cellType == EColType.TEXT) {
            this.values = new String[values.size()];
            for(int i=0 ; i<values.size() ; i++) {
                this.values[i] = values.get(i).toString();
            }
        }
    }

    public String getName() {
        return name;
    }

    public EColType getCellType() {
        return cellType;
    }

    public String[] getValues() {
        return values;
    }
}