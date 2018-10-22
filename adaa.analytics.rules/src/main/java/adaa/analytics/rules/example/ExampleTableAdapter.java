package adaa.analytics.rules.example;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AbstractExampleTable;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.DoubleArrayDataRow;
import org.renjin.primitives.Types;
import org.renjin.sexp.ListVector;
import org.renjin.sexp.StringArrayVector;
import org.renjin.sexp.Symbols;
import org.renjin.sexp.Vector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private ArrayList<Attribute> attributes;
    private String dateFormat;

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
        /* Tworzenie szkieletu DataRow i pobranie formatu daty*/
        double[] data = new double[attributes.size()];
        SimpleDateFormat dateformat;
        if (dateFormat != null) {
            dateformat = new SimpleDateFormat(dateFormat);
        } else {
            dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            Vector vector = getDataFrame().getElementAsVector(
                    attribute.getName());
            data[i] = getDoubleValue(attribute, vector, dateformat, index);
        }
        return new DoubleArrayDataRow(data);
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
}
