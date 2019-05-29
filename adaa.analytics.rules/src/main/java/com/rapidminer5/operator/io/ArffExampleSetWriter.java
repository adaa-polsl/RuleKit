/*******************************************************************************
 * Copyright (C) 2019 RuleKit Development Team
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
 ******************************************************************************/
package com.rapidminer5.operator.io;

import com.rapidminer.operator.io.AbstractStreamWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDate;
import com.rapidminer.tools.io.Encoding;

public class ArffExampleSetWriter extends AbstractStreamWriter {

    /** The parameter name for &quot;File to save the example set to.&quot; */
    public static final String PARAMETER_EXAMPLE_SET_FILE = "example_set_file";

    public ArffExampleSetWriter(OperatorDescription description) {
        super(description);
    }

    @Override
    protected void writeStream(ExampleSet exampleSet, OutputStream outputStream) throws OperatorException {
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, Encoding.getEncoding(this)))) {
            writeArff(exampleSet, out);
            out.flush();
        }
    }

	/*
	    @Override
	    public ExampleSet write(ExampleSet exampleSet) throws OperatorException {
	        try {
	            File arffFile = getParameterAsFile(PARAMETER_EXAMPLE_SET_FILE, true);
	            PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(arffFile), Encoding.getEncoding(this)));
	            writeArff(exampleSet, out);
	            out.flush();
	            out.close();
	        } catch (IOException e) {
	            throw new UserError(this, e, 303, new Object[] { getParameterAsString(PARAMETER_EXAMPLE_SET_FILE), e.getMessage() });
	        }
	        return exampleSet;
	    }*/

    private static void writeArff(ExampleSet exampleSet, PrintWriter out, String linefeed) {
        if (linefeed == null) {
            linefeed = System.getProperty("line.separator");
        }

        // relation
        out.print("@RELATION RapidMinerData" + linefeed);
        out.print(linefeed);

        // attribute meta data
        Iterator<Attribute> a = exampleSet.getAttributes().allAttributes();
        while (a.hasNext()) {
            printAttributeData(a.next(), out);
        }

        // data
        out.print(linefeed);
        out.print("@DATA" + linefeed);

        for (Example example : exampleSet) {
            boolean first = true;
            a = exampleSet.getAttributes().allAttributes();
            while (a.hasNext()) {
                Attribute current = a.next();
                if (!first)
                    out.print(",");

                if (current.isNominal()) {
                    double value = example.getValue(current);
                    if (Double.isNaN(value))
                        out.print("?");
                    else
                        out.print("'" + example.getValueAsString(current) + "'");
                } else if (current.isDateTime()) {
                    Date dateValue = example.getDateValue(current);
                    out.print("\"" + ParameterTypeDate.DATE_FORMAT.get().format(dateValue)+ "\"");
                } else {
                    out.print(example.getValueAsString(current));
                }
                first = false;
            }
            out.print(linefeed);
        }
    }

    private static void writeArff(ExampleSet exampleSet, PrintWriter out) {
        writeArff(exampleSet, out, null);
    }

    private static void printAttributeData(Attribute attribute, PrintWriter out) {
        out.print("@ATTRIBUTE '" + attribute.getName() + "' ");
        if (attribute.isNominal()) {
            StringBuilder nominalValues = new StringBuilder("{");
            boolean first = true;
            for (String s : attribute.getMapping().getValues()) {
                if (!first)
                    nominalValues.append(",");
                nominalValues.append("'").append(s).append("'");
                first = false;
            }
            nominalValues.append("}");
            out.print(nominalValues.toString());
        } else if (attribute.isDateTime())
            out.print("DATE \"" + ParameterTypeDate.DATE_FORMAT.get().toPattern() + "\"");
        else {
            out.print("real");
        }
        out.println();
    }

    @Override
    protected boolean supportsEncoding() {
        return true;
    }

    @Override
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = new LinkedList<>();
        types.add(makeFileParameterType());
        //types.add(new ParameterTypeFile(PARAMETER_EXAMPLE_SET_FILE, "File to save the example set to.", "arff", false));
        types.addAll(super.getParameterTypes());
        return types;
    }

    @Override
    protected String[] getFileExtensions() {
        return new String[] { "arff" };
    }

    @Override
    protected String getFileParameterName() {
        return PARAMETER_EXAMPLE_SET_FILE;
    }

}
