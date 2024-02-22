package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.MinMaxStatistics;
import adaa.analytics.rules.rm.example.NumericalStatistics;
import adaa.analytics.rules.rm.example.UnknownStatistics;
import adaa.analytics.rules.rm.example.WeightedNumericalStatistics;
import adaa.analytics.rules.rm.tools.Tools;

public class NumericalAttribute extends AbstractAttribute {
    private static final long serialVersionUID = -7425486508057529570L;
    public static final int DEFAULT_NUMBER_OF_DIGITS = -1;
    public static final int UNLIMITED_NUMBER_OF_DIGITS = -2;

    public NumericalAttribute(String name) {
        this(name, 2);
    }

    NumericalAttribute(String name, int valueType) {
        super(name, valueType);
        this.registerStatistics(new NumericalStatistics());
        this.registerStatistics(new WeightedNumericalStatistics());
        this.registerStatistics(new MinMaxStatistics());
        this.registerStatistics(new UnknownStatistics());
    }

    private NumericalAttribute(NumericalAttribute a) {
        super(a);
    }

    public Object clone() {
        return new NumericalAttribute(this);
    }

    public boolean isNominal() {
        return false;
    }

    public boolean isNumerical() {
        return true;
    }

    public INominalMapping getMapping() {
        throw new UnsupportedOperationException("The method getNominalMapping() is not supported by numerical attributes! You probably tried to execute an operator on a numerical data which is only able to handle nominal values. You could use one of the discretization operators before this application.");
    }

    public void setMapping(INominalMapping mapping) {
    }

    public String getAsString(double value, int numberOfDigits, boolean quoteNominal) {
        if (Double.isNaN(value)) {
            return "?";
        } else {
            switch (numberOfDigits) {
                case -2:
                    return Double.toString(value);
                case -1:
                    return Tools.formatIntegerIfPossible(value);
                default:
                    return Tools.formatIntegerIfPossible(value, numberOfDigits);
            }
        }
    }

    public boolean isDateTime() {
        return false;
    }
}
