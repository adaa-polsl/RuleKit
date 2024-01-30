package adaa.analytics.rules.rm.example;

import java.io.Serializable;

public interface AttributeTransformation extends Serializable, Cloneable {
    Object clone();

    double transform(IAttribute var1, double var2);

    double inverseTransform(IAttribute var1, double var2);

    boolean isReversable();
}