package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.MinMaxStatistics;
import adaa.analytics.rules.rm.example.UnknownStatistics;
import adaa.analytics.rules.rm.tools.Tools;

import java.util.Date;

public class DateAttribute extends AbstractAttribute {
    private static final long serialVersionUID = -685655991653799960L;

    protected DateAttribute(String name) {
        this(name, 10);
    }

    protected DateAttribute(String name, int valueType) {
        super(name, valueType);
        this.registerStatistics(new MinMaxStatistics());
        this.registerStatistics(new UnknownStatistics());
    }

    private DateAttribute(DateAttribute a) {
        super(a);
    }

    public Object clone() {
        return new DateAttribute(this);
    }

    public String getAsString(double value, int digits, boolean quoteNominal) {
        if (Double.isNaN(value)) {
            return "?";
        } else {
            long milliseconds = (long)value;
            String result = null;
            if (this.getValueType() == 10) {
                result = Tools.formatDate(new Date(milliseconds));
            } else if (this.getValueType() == 11) {
                result = Tools.formatTime(new Date(milliseconds));
            } else if (this.getValueType() == 9) {
                result = Tools.formatDateTime(new Date(milliseconds), "dd/MM/yyyy HH:mm:ss aa zzz");
            }

            if (quoteNominal) {
                result = "\"" + result + "\"";
            }

            return result;
        }
    }

    public INominalMapping getMapping() {
        throw new UnsupportedOperationException("The method getNominalMapping() is not supported by date attributes! You probably tried to execute an operator on a date or time data which is only able to handle nominal values. You could use one of the Date to Nominal operator before this application.");
    }

    public boolean isNominal() {
        return false;
    }

    public boolean isNumerical() {
        return false;
    }

    public void setMapping(INominalMapping nominalMapping) {
    }

    public boolean isDateTime() {
        return true;
    }
}
