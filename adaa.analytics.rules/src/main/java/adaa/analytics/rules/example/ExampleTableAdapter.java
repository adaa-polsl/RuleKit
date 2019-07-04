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
package adaa.analytics.rules.example;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.*;
import org.apache.commons.lang.ArrayUtils;
import org.renjin.primitives.Types;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class ExampleTableAdapter extends AbstractExampleTable {

    public ExampleTableAdapter(ListVector dataFrame, ArrayList<Attribute> attributes,
                               String dateFormat) {
        super(attributes);
        this.dataFrame = dataFrame;
        this.attributes = attributes;
        this.dateFormat = dateFormat;
    }

    private ListVector dataFrame;
    private MemoryExampleTable excessDataTable = null;
    private ArrayList<Attribute> attributes;
    private String dateFormat;
    private int dataRowIndex;

    @Override
    public int size() {
        try {
            Vector vector = getDataFrame().getElementAsVector(
                    attributes.get(0).getName());
            return vector.length();
        } catch (Exception e) {
            return 0;
        }
    }

    /* This works but it's not a real iterator.
     * Access to data through index.
     */
    @Override
    public DataRowReader getDataRowReader() {
        return new DataFrameRowReader(this);
    }

    @Override
    public DataRow getDataRow(int index) {
        dataRowIndex = index;
        double[] data = new double[dataFrame.length()];
        for (int i = 0; i < dataFrame.length(); i++) {
            Attribute attribute = attributes.get(i);
            data[i] = getSexpValue(attribute, index);
        }
        data = appendExcessData(data,index);
        return new DoubleArrayDataRowR(data,this);
    }

    private double[] appendExcessData(double[] data, int index) {
        int excessDataSize = attributes.size() - dataFrame.length();
        if (excessDataSize > 0) {
            if (excessDataTable.size() <= index) {
                double[] excessData = new double[excessDataSize];
                DataRow row = new DoubleArrayDataRow(excessData);
                excessDataTable.addDataRow(row);
                return ArrayUtils.addAll(data, excessData);
            } else {
                DataRow dataRow = excessDataTable.getDataRow(index);
                Attribute[] attributes = excessDataTable.getAttributes();
                double[] excessData = new double[attributes.length];
                for (int i=0 ;i<attributes.length;i++){
                    Attribute attribute = attributes[i];
                    excessData[i] = dataRow.get(attribute);
                }
                return ArrayUtils.addAll(data, excessData);
            }
        }
        return data;
    }

    public void setDataRowValue(int index, double value, double defaultValue){
        DataRow dataRow = excessDataTable.getDataRow(dataRowIndex);
        Attribute attribute = excessDataTable.getAttribute(index - dataFrame.length());
        dataRow.set(attribute,value);
    }

    private double getSexpValue(Attribute attribute, int index){
        SimpleDateFormat dateformat;
        if (dateFormat != null) {
            dateformat = new SimpleDateFormat(dateFormat);
        } else {
            dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        Vector vector = getDataFrame().getElementAsVector(attribute.getName());
        return getDoubleValue(attribute, vector, dateformat, index);
    }

    private double getDoubleValue(Attribute attribute, Vector vector, SimpleDateFormat dateformat, int index) {

        //Typy String oraz data/czas.
        if (Types.isCharacter(vector)) {
            if(attribute.isDateTime()) {
                Date date = null;
                try {
                    date = dateformat.parse(vector.getElementAsString(index));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date.getTime();
            } else {
                return attribute.getMapping().mapString(vector.getElementAsString(index));
            }
        } else

            //Dla kolumn bedacych faktorem.
            if (Types.isFactor(vector)) {
                StringArrayVector levels = (StringArrayVector) vector.getAttributes().get(Symbols.LEVELS);
                Double value = vector.getElementAsDouble(index);
                if (value.isNaN()) {
                    return Double.NaN;
                } else {
                    /* Value to wartosc zwracana przez R-owska strukture factor odpowiada ona indeksowi
                     * na którym w levels znajduje sie aktualni erozpatrywany string ze wzgledu na inny maping
                     * w Rapidmainerowskim atrybucie trzeba odjac od value 1 i dopiero odczytac mapping.
                     * Mozna rozwazyc jakies prostsze lepsze i bardziej odporne rozwiazanie.
                     */
                    String nominalValue = levels.getElementAsString((int) (value - 1));
                    return attribute.getMapping().getIndex(nominalValue);
                }
            } else

            if (Types.isDouble(vector)) {
                return vector.getElementAsDouble(index);
            } else

            if (Types.isInteger(vector)) {
                return vector.getElementAsInt(index);
            }

        return vector.getElementAsDouble(index);
    }

    public ListVector getDataFrame() {
        return dataFrame;
    }

    public int addAttribute(Attribute a) {
        int index = super.addAttribute(a);
        this.attributes = new ArrayList<Attribute>(Arrays.asList(super.getAttributes()));
        if (!isSexpAttribute(a)){
            Attribute copy = AttributeFactory.createAttribute(a);
            addToExcessTable(copy);
        }
        return index;
    }

    private boolean isSexpAttribute(Attribute a){
        if (dataFrame != null) {
            int length = dataFrame.length();
            if (length <= a.getTableIndex()) {
                return false;
            }
            return true;
        }
        return true;
    }

    private void addToExcessTable(Attribute a){
        if (excessDataTable == null){
            excessDataTable = new MemoryExampleTable(a);
        } else {
            excessDataTable.addAttribute(a);
        }
    }
}
