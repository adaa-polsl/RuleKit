package adaa.analytics.rules.rm.example.table;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.AttributeRole;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.operator.OperatorException;
import adaa.analytics.rules.rm.tools.att.AttributeSet;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public interface IExampleTable extends Serializable {
    int size();

    IDataRowReader getDataRowReader();

    DataRow getDataRow(int var1);

    IAttribute[] getAttributes();

    IAttribute getAttribute(int var1);

    IAttribute findAttribute(String var1) throws OperatorException;

    void addAttributes(Collection<IAttribute> var1);

    int addAttribute(IAttribute var1);

    void removeAttribute(IAttribute var1);

    void removeAttribute(int var1);

    int getNumberOfAttributes();

    int getAttributeCount();

    IExampleSet createExampleSet(IAttribute var1);

    IExampleSet createExampleSet(Iterator<AttributeRole> var1);

    IExampleSet createExampleSet(IAttribute var1, IAttribute var2, IAttribute var3);

    IExampleSet createExampleSet(AttributeSet var1);

    IExampleSet createExampleSet(Map<IAttribute, String> var1);

    IExampleSet createExampleSet();

    String toString();

    String toDataString();
}
