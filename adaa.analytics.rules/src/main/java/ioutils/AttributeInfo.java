package ioutils;

import adaa.analytics.rules.data.metadata.EColumnType;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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

        List<String> tokens = getTokens(arffAttributeLine, ' ');
        tokens.removeIf(str -> str.trim().isEmpty());

        if (tokens.get(0).equalsIgnoreCase("@attribute")) {

            name = tokens.get(1);
            if(type == null) {
                type = tokens.get(2).trim();
            }

            if (type.equalsIgnoreCase("numeric") ||
                    type.equalsIgnoreCase("real") ||
                    type.equalsIgnoreCase("integer")) {
                colType = EColumnType.NUMERICAL;
            } else if ((type.startsWith("{") && type.endsWith("}")) ||
                    type.equalsIgnoreCase("string")) {
                type = type.substring(1, type.length() - 1);
                values = getTokens(type, ',');
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

    private List<String> getTokens(String arffAttributeLine, char delimiterChar) {
        return getTokens(arffAttributeLine, delimiterChar, "'\"");
    }

    private List<String> getTokens(String arffAttributeLine, char delimiterChar, String quoteChars) {
        ArrayList<String> tokensList = new ArrayList<>();
        Character currQuoteChar = null;

        Scanner scanner = new Scanner(arffAttributeLine);
        scanner.useDelimiter(String.valueOf(delimiterChar));
        while (scanner.hasNext()) {
            String token = scanner.next();
            if (startsWithOneOfChars(token, quoteChars)) {
                currQuoteChar = token.charAt(0);
                if(token.endsWith(String.valueOf(currQuoteChar))) {
                    tokensList.add(token.substring(1, token.length()-1).trim());
                }
                else {
                    StringBuilder quotedToken = new StringBuilder(token.substring(1));
                    while (scanner.hasNext()) {
                        token = scanner.next();
                        quotedToken.append(delimiterChar);
                        if (token.endsWith(String.valueOf(currQuoteChar))) {
                            quotedToken.append(token, 0, token.length()-1);
                            break;
                        }
                        quotedToken.append(token);
                    }
                    tokensList.add(quotedToken.toString().trim());
                }
                currQuoteChar = null;
            } else {
                tokensList.add(token.trim());
            }
        }
        scanner.close();

        return tokensList;
    }

    private boolean startsWithOneOfChars(String text, String chars) {
        for(char c : chars.toCharArray()) {
            if(text.startsWith(String.valueOf(c)))
                return true;
        }
        return false;
    }
}