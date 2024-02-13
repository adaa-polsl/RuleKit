package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.AttributeRole;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.tools.Ontology;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

// TODO throw new UserError
public class ExampleSetUtilities {
    public static final Comparator<AttributeRole> SPECIAL_ATTRIBUTES_ROLE_COMPARATOR = new Comparator<AttributeRole>() {
        private List<String> priorityList = Arrays.asList("id", "label", "prediction", "confidence", "cluster", "weight");

        public int compare(AttributeRole a1, AttributeRole a2) {
            int priorityAttribute1 = a1.isSpecial() ? 1000 : 2000;
            int priorityAttribute2 = a2.isSpecial() ? 1000 : 2000;
            if (a1.isSpecial() && this.priorityList.contains(a1.getSpecialName())) {
                priorityAttribute1 = this.priorityList.indexOf(a1.getSpecialName());
            }

            if (a2.isSpecial() && this.priorityList.contains(a2.getSpecialName())) {
                priorityAttribute2 = this.priorityList.indexOf(a2.getSpecialName());
            }

            if (a1.isSpecial() && a1.getSpecialName().startsWith("confidence_")) {
                priorityAttribute1 = this.priorityList.indexOf("confidence");
            }

            if (a2.isSpecial() && a2.getSpecialName().startsWith("confidence_")) {
                priorityAttribute2 = this.priorityList.indexOf("confidence");
            }

            return priorityAttribute1 - priorityAttribute2;
        }
    };

    public ExampleSetUtilities() {
    }

    public static void checkAttributesMatching(
//            Operator op,
            IAttributes originalAttributes,
            IAttributes comparedAttributes,
            ExampleSetUtilities.SetsCompareOption compareSets,
            ExampleSetUtilities.TypesCompareOption compareTypes)
//            throws UserError
    {
        Iterator var5 = originalAttributes.iterator();

        IAttribute comparedAttribute;
        while(var5.hasNext()) {
            comparedAttribute = (IAttribute)var5.next();
            if (comparedAttributes.contains(comparedAttribute)) {
                String comparedAttributeName = comparedAttribute.getName();
                IAttribute comparedAttribute2 = comparedAttributes.get(comparedAttributeName);
                int originalValueType = comparedAttribute2.getValueType();
                int comparedValueType = comparedAttribute2.getValueType();
                if (originalValueType != comparedValueType) {
                    Ontology valueTypes = Ontology.ATTRIBUTE_VALUE_TYPE;
                    if (compareTypes == ExampleSetUtilities.TypesCompareOption.ALLOW_SUBTYPES) {
                        if (!valueTypes.isA(comparedValueType, originalValueType)) {
//                            throw new UserError(op, 964, new Object[]{comparedAttribute.getName(), Ontology.VALUE_TYPE_NAMES[comparedValueType], Ontology.VALUE_TYPE_NAMES[originalValueType]});
                        }
                    } else if (compareTypes == ExampleSetUtilities.TypesCompareOption.ALLOW_SUPERTYPES) {
                        if (!valueTypes.isA(originalValueType, comparedValueType)) {
//                            throw new UserError((Operator)null, 965, new Object[]{comparedAttribute.getName(), Ontology.VALUE_TYPE_NAMES[comparedValueType], Ontology.VALUE_TYPE_NAMES[originalValueType]});
                        }
                    } else if (compareTypes == ExampleSetUtilities.TypesCompareOption.ALLOW_SAME_PARENTS) {
                        int parentOriginal = valueTypes.getParent(originalValueType);
                        parentOriginal = parentOriginal <= 0 ? originalValueType : parentOriginal;
                        int parentCompared = valueTypes.getParent(comparedValueType);
                        parentCompared = parentCompared <= 0 ? comparedValueType : parentCompared;
                        if (!valueTypes.isA(parentCompared, parentOriginal)) {
//                            throw new UserError(op, 965, new Object[]{comparedAttribute.getName(), Ontology.VALUE_TYPE_NAMES[comparedValueType], Ontology.VALUE_TYPE_NAMES[originalValueType]});
                        }
                    } else if (compareTypes == ExampleSetUtilities.TypesCompareOption.EQUAL) {
//                        throw new UserError(op, 963, new Object[]{comparedAttribute.getName(), Ontology.VALUE_TYPE_NAMES[originalValueType], Ontology.VALUE_TYPE_NAMES[comparedValueType]});
                    }
                }
            } else {
                if (compareSets == ExampleSetUtilities.SetsCompareOption.EQUAL) {
//                    throw new UserError(op, 960, new Object[]{comparedAttribute.getName()});
                }

                if (compareSets == ExampleSetUtilities.SetsCompareOption.ALLOW_SUPERSET) {
//                    throw new UserError(op, 962, new Object[]{comparedAttribute.getName()});
                }
            }
        }

        var5 = comparedAttributes.iterator();

        while(var5.hasNext()) {
            comparedAttribute = (IAttribute)var5.next();
            if (!originalAttributes.contains(comparedAttribute)) {
                if (compareSets == ExampleSetUtilities.SetsCompareOption.EQUAL) {
//                    throw new UserError(op, 960, new Object[]{comparedAttribute.getName()});
                }

                if (compareSets == ExampleSetUtilities.SetsCompareOption.ALLOW_SUBSET) {
//                    throw new UserError(op, 961, new Object[]{comparedAttribute.getName()});
                }
            }
        }

    }

    public static enum TypesCompareOption {
        EQUAL,
        ALLOW_SUBTYPES,
        ALLOW_SUPERTYPES,
        ALLOW_SAME_PARENTS,
        DONT_CARE;

        private TypesCompareOption() {
        }
    }

    public static enum SetsCompareOption {
        EQUAL,
        ALLOW_SUBSET,
        ALLOW_SUPERSET,
        USE_INTERSECTION;

        private SetsCompareOption() {
        }
    }
}
