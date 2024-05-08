package adaa.analytics.rules.data.metadata;

import adaa.analytics.rules.utils.Ontology;

public class Converter {

    public static int EColumnTypeToRmOntology(EColumnType eColType) {

        if(eColType == EColumnType.DATE) {
            return Ontology.STRING;
        }
        if(eColType == EColumnType.NOMINAL) {
            return Ontology.NOMINAL;
        }
        if(eColType == EColumnType.NUMERICAL) {
            return Ontology.NUMERICAL;
        }

        return Ontology.ATTRIBUTE_VALUE;
    }

    public static EColumnType RmOntologyToEColumnType(int type) {

        if(type == Ontology.NUMERICAL || type == Ontology.REAL) {

            return EColumnType.NUMERICAL;
        }
        else if(type == Ontology.NOMINAL || type == Ontology.STRING) {

            return EColumnType.NOMINAL;
        }
//        else if(type == Ontology.STRING) {
//
//            return EColumnType.DATE;
//        }

        return EColumnType.OTHER;
    }
}
