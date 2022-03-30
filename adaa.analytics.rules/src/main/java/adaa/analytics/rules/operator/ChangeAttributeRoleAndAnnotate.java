package adaa.analytics.rules.operator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;

public class ChangeAttributeRoleAndAnnotate extends ChangeAttributeRole {

    public static String PARAMETER_ANNOTATION_NAME = "annotation_name";

    public static String PARAMETER_ANNOTATION_VALUE = "annotation_value";

    public ChangeAttributeRoleAndAnnotate(OperatorDescription description) {
        super(description);
    }

    @Override
    public ExampleSet apply(ExampleSet exampleSet) throws OperatorException {
        ExampleSet outSet = super.apply(exampleSet);

        String annotationName = getParameterAsString(PARAMETER_ANNOTATION_NAME);
        String annotationValue = getParameterAsString(PARAMETER_ANNOTATION_VALUE);

        if (annotationName != null && annotationName.length() > 0) {
            outSet.getAnnotations().put(annotationName, annotationValue);
        }

        return outSet;
    }
}
