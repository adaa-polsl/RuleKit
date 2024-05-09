package adaa.analytics.rules.data.metadata;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.columns.Column;

import java.io.Serializable;

public enum EColumnType implements Serializable {
    OTHER,
    NOMINAL,
    NUMERICAL,
    DATE;

    public static EColumnType fromTsColumn(Column<?> column) {

        if(column.type() == ColumnType.DOUBLE ||
                column.type() == ColumnType.FLOAT ||
                column.type() == ColumnType.LONG ||
                column.type() == ColumnType.INTEGER ||
                column.type() == ColumnType.BOOLEAN ||
                column.type() == ColumnType.SHORT) {

            return EColumnType.NUMERICAL;
        }
        else if(column.type() == ColumnType.TEXT ||
                column.type() == ColumnType.STRING) {

            return EColumnType.NOMINAL;
        }
        else if(column.type() == ColumnType.LOCAL_DATE_TIME ||
                column.type() == ColumnType.LOCAL_DATE ||
                column.type() == ColumnType.LOCAL_TIME) {

            return EColumnType.DATE;
        }

        return EColumnType.OTHER;
    }

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
