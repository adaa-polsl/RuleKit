package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.NominalStatistics;
import adaa.analytics.rules.rm.example.UnknownStatistics;
import adaa.analytics.rules.rm.tools.Tools;

public abstract class NominalAttribute extends AbstractAttribute {
    private static final long serialVersionUID = -3830980883541763869L;

    protected NominalAttribute(String name, int valueType) {
        super(name, valueType);
        this.registerStatistics(new NominalStatistics());
        this.registerStatistics(new UnknownStatistics());
    }

    protected NominalAttribute(NominalAttribute other) {
        super(other);
    }

    public boolean isNominal() {
        return true;
    }

    public boolean isNumerical() {
        return false;
    }

    public void setValue(DataRow row, double value) {
        if (value < 0.0) {
            value = Double.NaN;
        }

        super.setValue(row, value);
    }

    public String getAsString(double value, int digits, boolean quoteNominal) {
        if (Double.isNaN(value)) {
            return "?";
        } else {
            try {
                String result = this.getMapping().mapIndex((int)value);
                if (quoteNominal) {
                    result = Tools.escape(result);
                    result = "\"" + result + "\"";
                }

                return result;
            } catch (Throwable var6) {
                return "?";
            }
        }
    }
}
