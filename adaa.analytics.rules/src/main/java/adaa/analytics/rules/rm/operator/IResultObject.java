package adaa.analytics.rules.rm.operator;

import java.io.Serializable;

//public interface IResultObject extends IOObject {
public interface IResultObject extends Serializable {
    String getName();

    Annotations getAnnotations();

//    String toResultString();

//    Icon getResultIcon();

    /** @deprecated */
//    @Deprecated
//    List<Action> getActions();
}
