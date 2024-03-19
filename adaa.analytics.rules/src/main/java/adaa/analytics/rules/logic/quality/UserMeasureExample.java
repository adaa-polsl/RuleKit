package adaa.analytics.rules.logic.quality;

public class UserMeasureExample implements IUserMeasure {

    public double getResult(double p, double n, double P, double N) {
        double result = 2 * p / n;
        return result;
    }
}
