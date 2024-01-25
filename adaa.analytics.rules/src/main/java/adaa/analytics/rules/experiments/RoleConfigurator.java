package adaa.analytics.rules.experiments;

import adaa.analytics.rules.logic.representation.ContrastRule;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;

import java.util.List;

public class RoleConfigurator {


    private String labelValue;

    private String contrastValue;

    private List<String[]> roles;


    private static final String REGULAR_NAME = "regular";

    public RoleConfigurator(String labelValue) {
        this.labelValue = labelValue;
    }

    public void configureContrast( String contrastValue)
    {
        this.contrastValue = contrastValue;
    }

    public void configureRoles( List<String[]> roles)
    {
        this.roles = roles;
    }

    public void apply(ExampleSet exampleSet) throws OperatorException {

        setRole(exampleSet, labelValue, "label", "attribute_name");


        if (roles!=null)
        {
            for (String[] pairs : roles) {
                setRole(exampleSet, pairs[0], pairs[1], "set_additional_roles");
            }
        }
        if (contrastValue!=null)
        {
            exampleSet.getAnnotations().put(ContrastRule.CONTRAST_ATTRIBUTE_ROLE, contrastValue);
        }

    }

    private void setRole(ExampleSet exampleSet, String name, String newRole, String paramKey) {
        Attribute attribute = exampleSet.getAttributes().get(name);

        if (attribute == null) {
            throw new IllegalArgumentException("Params not found: "+ paramKey+" "+name);
        }

        exampleSet.getAttributes().remove(attribute);
        if (newRole == null || newRole.trim().length() == 0) {
            throw new IllegalArgumentException("Undefined parameter: set_additional_roles");
        }
        if (newRole.equals(REGULAR_NAME)) {
            exampleSet.getAttributes().addRegular(attribute);
        } else {
            Attribute oldOne = exampleSet.getAttributes().getSpecial(newRole);
            if (oldOne != null) {
                exampleSet.getAttributes().remove(oldOne);
                exampleSet.getAttributes().addRegular(oldOne);
            }
            exampleSet.getAttributes().setSpecialAttribute(attribute, newRole);
        }
    }
}
