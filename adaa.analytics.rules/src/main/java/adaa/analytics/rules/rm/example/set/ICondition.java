package adaa.analytics.rules.rm.example.set;

import adaa.analytics.rules.rm.example.Example;

import java.io.Serializable;

public interface ICondition extends Serializable {
    boolean conditionOk(Example var1) throws ArithmeticException;

    /** @deprecated */
    @Deprecated
    ICondition duplicate();
}