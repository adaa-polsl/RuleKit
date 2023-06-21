package utils;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;

public class PrettyExamplePrinter {

    public static String format(Example example) {
        StringBuilder builder = new StringBuilder();
        Attributes atr = example.getAttributes();
        builder.append('(');
        for (Attribute a : atr){
            builder.append(a.getName());
            builder.append("=");

            builder.append(a.isNominal() ? example.getNominalValue(a) : example.getNumericalValue(a));
            builder.append(", ");
        }
        if (example.getAttributes().getLabel().isNominal()) {
            builder.append("class = ").append(example.getNominalValue(example.getAttributes().getLabel()));
        } else {
            builder.append("class = ").append(example.getNumericalValue(example.getAttributes().getLabel()));
        }
        if (example.getAttributes().getPredictedLabel() != null) {
            builder.append(" predicted(class) = ").append(
                    example.getAttributes().getLabel().isNominal()  ?
                    example.getNominalValue(example.getAttributes().getPredictedLabel())
                    :
                    example.getNumericalValue(example.getAttributes().getPredictedLabel()));
        }
        builder.append(')');
        return builder.toString();
    }

}
