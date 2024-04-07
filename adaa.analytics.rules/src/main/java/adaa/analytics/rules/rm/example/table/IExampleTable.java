package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;

import java.io.Serializable;

public interface IExampleTable extends Serializable {

    int addAttribute(IAttribute var1);

    String toString();
}
