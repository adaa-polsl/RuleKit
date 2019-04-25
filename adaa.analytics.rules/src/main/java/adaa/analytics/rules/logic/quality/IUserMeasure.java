package adaa.analytics.rules.logic.quality;

import java.io.Serializable;

public interface IUserMeasure extends Serializable{

    void setValues(double p, double n, double P, double N);
    double getResult();
}
