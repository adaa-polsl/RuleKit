/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package adaa.analytics.rules.operator;

import adaa.analytics.rules.example.ExampleTableAdapter;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.parameter.*;
import com.rapidminer.tools.Ontology;
import org.renjin.eval.Context;
import org.renjin.primitives.Types;
import org.renjin.primitives.io.serialization.RDataReader;
import org.renjin.sexp.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Class representing example source to be utilized by R engine.
 */
public class RDataExampleSource extends AbstractExampleSource {

    public static final String PARAMETER_DATA_FILE = "data_file";
    public static final String PARAMETER_DATE_ATTRIBUTES = "date_attributes";
    public static final String PARAMETER_DATE_FORMAT = "date_format";

    public RDataExampleSource(OperatorDescription description) {
        super(description);
    }

    @Override
    public ExampleSet createExampleSet() throws OperatorException {

        String[] dataAttributes = ParameterTypeEnumeration
                .transformString2Enumeration(getParameterAsString(PARAMETER_DATE_ATTRIBUTES));
        return loadRData(getParameterAsString(PARAMETER_DATA_FILE),
                dataAttributes,
                getParameterAsString(PARAMETER_DATE_FORMAT));
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = new LinkedList<ParameterType>();
        types.add(new ParameterTypeString(PARAMETER_DATA_FILE, "Path to .RData file.", false));
        types.add(new ParameterTypeEnumeration(PARAMETER_DATE_ATTRIBUTES, "Name of date attributes in data frame.",
                new ParameterTypeString("date_attributes","",false)));
        types.add(new ParameterTypeString(PARAMETER_DATE_FORMAT, "Pattern for date attributes.", true));
        //types.add(new ParameterTypeList())
        types.addAll(super.getParameterTypes());
        return types;
    }

    public static ExampleSet loadRData(String directory, String[] dateAttributes, String dateFormat) throws UserError {
        try {
            return toRapidMinerExampleSet(getDataFrame(directory), dateAttributes, dateFormat);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ListVector getDataFrame(String directory) throws IOException {
        Context context = Context.newTopLevelContext();
        FileInputStream fio = new FileInputStream(directory);
        GZIPInputStream zis = new GZIPInputStream(fio);
        try (RDataReader rdreader = new RDataReader(context, zis);) {
        	PairList pairList = (PairList) rdreader.readFile();
        	return pairList.getElementAsSEXP(0);
        }
    }

    public static ExampleSet toRapidMinerExampleSet(ListVector dataFrame,
                                                    String[] dateAttNames, String dateFormat) throws UserError {

        ArrayList<Attribute> attributes = new ArrayList<Attribute>();


        /*Sprawdzenie czy ramka danych istnieje*/
        AttributeMap attributeMap = null;
        try {
            attributeMap = dataFrame.getAttributes();
        } catch (Exception e) {
            if (e instanceof NullPointerException) {
                throw new UserError(null, e, "data.01");
            }
        }

        /*Tworzenie listy nazw atrybutów typu data/czas*/
        ArrayList<String> dateAttNamesList = new ArrayList<String>();
        for (int i = 0; i < dateAttNames.length; i++) {
            dateAttNamesList.add(dateAttNames[i]);
        }

        //Tworzenie listy atrybutów na podstawie ich nazw oraz typów.
        StringVector attNames = attributeMap.getNames();

        /* Tworzenie listy atrybutów na podstawie kolejnych kolumn ramki danych */
        for (String attName : attNames) {
            Attribute attribute = null;
            SEXP vector = dataFrame.get(attName);
            attribute = getNewAttribute(vector, dateAttNamesList, attName);
            attributes.add(attribute);
            if (dateAttNamesList.contains(attName)) {
                checkDateFormat(dateFormat, vector);
            }
        }
        ExampleTableAdapter exampleTable = new ExampleTableAdapter(dataFrame,
                attributes, dateFormat);
        return exampleTable.createExampleSet();
    }

    private static void checkDateFormat(String dateFormat, SEXP vector) throws UserError {

        try {
            SimpleDateFormat dateformat = new SimpleDateFormat(dateFormat);
            dateformat.parse(((StringArrayVector) vector).getElementAsString(0));
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw new UserError(null, e, "format.02");
            } else {
                throw new UserError(null, e, "format.01");
            }

        }
    }

    private static Attribute getNewAttribute(SEXP vector, ArrayList<String> dateAttNamesList, String name) {

        /* Kolumny bedące wektorem stringow lub datą.*/

        if (Types.isCharacter(vector)) {
            if (dateAttNamesList.contains(name)) {
                return AttributeFactory.createAttribute(name,
                        Ontology.DATE_TIME);
            } else {
                return AttributeFactory.createAttribute(name,
                        Ontology.NOMINAL);
            }
        } else

            /*Kolumny bedące factorami.*/

            if (Types.isFactor(vector)) {
                Attribute attribute = AttributeFactory.createAttribute(name,
                        Ontology.NOMINAL);
                StringArrayVector levels = (StringArrayVector) vector.getAttributes()
                        .get(Symbols.LEVELS);
                Iterator<String> iterator = levels.iterator();
                while (iterator.hasNext()) {
                    attribute.getMapping().mapString(iterator.next());
                }
                return attribute;
            } else

                /*Kolumny zawierajace double*/

                if (Types.isDouble(vector)) {
                    return AttributeFactory.createAttribute(name,
                            Ontology.REAL);
                } else if (Types.isInteger(vector)) {
                    return AttributeFactory.createAttribute(name,
                            Ontology.INTEGER);
                } else if (Types.isLogical(vector)) {
                    return AttributeFactory.createAttribute(name,
                            Ontology.BINOMINAL);
                }
        return AttributeFactory.createAttribute(name, Ontology.NUMERICAL);
    }
}
