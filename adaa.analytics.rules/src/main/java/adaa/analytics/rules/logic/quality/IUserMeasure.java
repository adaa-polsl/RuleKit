package adaa.analytics.rules.logic.quality;

public interface IUserMeasure {

    void setValues(double p, double n, double P, double N);
    double getResult();
}
