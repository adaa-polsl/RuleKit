/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2014 by RapidMiner and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapidminer.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer5.operator.io;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.AbstractDataReader;
import com.rapidminer.operator.nio.file.FileInputPortHandler;
import com.rapidminer.operator.ports.InputPort;
import com.rapidminer.operator.ports.Port;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.PortProvider;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.StrictDecimalFormat;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.io.Encoding;

import java.io.*;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;


/**
 * <p>This operator can read ARFF files known from the machine learning library Weka. 
 * An ARFF (Attribute-Relation File Format) file is an ASCII text file that describes 
 * a list of instances sharing a set of attributes. ARFF files were developed by the 
 * Machine Learning Project at the Department of Computer Science of The University 
 * of Waikato for use with the Weka machine learning software.</p>
 * 
 * <p>ARFF files have two distinct sections. The first section is the Header information, 
 * which is followed the Data information. The Header of the ARFF file contains the name 
 * of the relation (@RELATION, ignored by RapidMiner) and a list of the attributes, each of which
 * is defined by a starting @ATTRIBUTE followed by its name and its type.</p>
 * 
 * <p>Attribute declarations take the form of an orderd sequence of @ATTRIBUTE statements. 
 * Each attribute in the data set has its own @ATTRIBUTE statement which uniquely defines 
 * the name of that attribute and it's data type. The order the attributes are declared 
 * indicates the column position in the data section of the file. For example, if an 
 * attribute is the third one declared all that attributes values will be found in the third 
 * comma delimited column.</p>
 * 
 * <p>The possible attribute types are:</p>
 * <ul>
 * <li><code>numeric</code></li>
 * <li><code>integer</code></li>
 * <li><code>real</code></li>
 * <li><code>{nominalValue1,nominalValue2,...}</code> for nominal attributes</li>
 * <li><code>string</code> for nominal attributes without distinct nominal values (it is 
 * however recommended to use the nominal definition above as often as possible)</li>
 * <li><code>date [date-format]</code> (currently not supported by RapidMiner)</li>
 * </ul>
 * 
 * <p>Valid examples for attribute definitions are <br>
 * <code>@ATTRIBUTE petalwidth REAL</code> <br>
 * <code>@ATTRIBUTE class {Iris-setosa,Iris-versicolor,Iris-virginica}</code>
 * </p>
 * 
 * <p>The ARFF Data section of the file contains the data declaration line @DATA followed
 * by the actual example data lines. Each example is represented on a single line, with 
 * carriage returns denoting the end of the example. Attribute values for each example 
 * are delimited by commas. They must appear in the order that they were declared in the 
 * header section (i.e. the data corresponding to the n-th @ATTRIBUTE declaration is 
 * always the n-th field of the example line). Missing values are represented by a single 
 * question mark, as in:<br>
 * <code>4.4,?,1.5,?,Iris-setosa</code></p>
 * 
 * <p>A percent sign (%) introduces a comment and will be ignored during reading. Attribute
 * names or example values containing spaces must be quoted with single quotes ('). Please
 * note that the sparse ARFF format is currently only supported for numerical attributes. 
 * Please use one of the other options for sparse data files provided by RapidMiner if you also 
 * need sparse data files for nominal attributes.</p>
 * 
 * <p>Please have a look at the Iris example ARFF file provided in the data subdirectory 
 * of the sample directory of RapidMiner to get an idea of the described data format.</p>
 *
 * @author Ingo Mierswa, Tobias Malbrecht
 */
//public class ArffExampleSource extends AbstractExampleSource {
public class ArffExampleSource extends AbstractDataReader {

	/** The parameter name for &quot;The path to the data file.&quot; */
	public static final String PARAMETER_DATA_FILE = "data_file";
	
	private InputPort fileInputPort = getInputPorts().createPort("file");
	private FileInputPortHandler filePortHandler = new FileInputPortHandler(this, fileInputPort, PARAMETER_DATA_FILE);

	static {
		registerReaderDescription(new ReaderDescription("arff", ArffExampleSource.class, PARAMETER_DATA_FILE));
	}

	public ArffExampleSource(OperatorDescription description) {
		super(description);
		/*
		 * There is no need to guess value types for Arff, as arff already contains the metadata
		 * about attributes
		 */
		this.skipGuessingValueTypes = true;
	}
	
	@Override
	protected DataSet getDataSet() throws OperatorException, IOException {
		return new DataSet() {
			private InputStream inputStream = null;
			
			private BufferedReader in = null;
			
			private StreamTokenizer tokenizer = null;
			
			private NumberFormat numberFormat = StrictDecimalFormat.getInstance(ArffExampleSource.this);
			
			private DateFormat   dateFormat   = new SimpleDateFormat();
			
			private String[] tokens = null;
			
			private HashMap<Integer,String> datePattern = new HashMap<Integer, String>();
			
			{	
				inputStream = filePortHandler.openSelectedFile();
				in = new BufferedReader(new InputStreamReader(inputStream, Encoding.getEncoding(ArffExampleSource.this)));
				tokenizer = createTokenizer(in);

				// read file

				Tools.getFirstToken(tokenizer);
				if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
					throw new UserError(ArffExampleSource.this, 302, filePortHandler.getSelectedFileDescription(), "file is empty");
				}

				if ("@relation".equalsIgnoreCase(tokenizer.sval)) {
					Tools.getNextToken(tokenizer);
					Tools.getLastToken(tokenizer, false);
				} else {
					throw new IOException("expected the keyword @relation in line " + tokenizer.lineno());
				}

				// attributes
				Tools.getFirstToken(tokenizer);
				if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
					throw new IOException("unexpected end of file in line " + tokenizer.lineno() + ", attribute description expected...");
				}

				LinkedList<String> attributeNamesList = new LinkedList<String>();
				LinkedList<Integer> valueTypesList = new LinkedList<Integer>();
				LinkedList<Set<String>> valueSets = new LinkedList<Set<String>>();
				LinkedHashSet<String> valueSet = null;
				while ("@attribute".equalsIgnoreCase(tokenizer.sval)) {
					// name
					Tools.getNextToken(tokenizer);
					String attributeName = tokenizer.sval;
					attributeNamesList.add(attributeName);

					// determine value type
					Tools.getNextToken(tokenizer);
					int valueType = Ontology.ATTRIBUTE_VALUE;
					if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
						// numerical or string value type
						if (tokenizer.sval.equalsIgnoreCase("real")) {
							valueType = Ontology.REAL;
						} else if (tokenizer.sval.equalsIgnoreCase("integer")) {
							valueType = Ontology.INTEGER;
						} else if (tokenizer.sval.equalsIgnoreCase("numeric")) {
							valueType = Ontology.NUMERICAL;
						} else if (tokenizer.sval.equalsIgnoreCase("string")) {
							valueType = Ontology.STRING;
						} else if (tokenizer.sval.equalsIgnoreCase("date")) {
							valueType = Ontology.DATE_TIME;
							Tools.getNextToken(tokenizer);
							datePattern.put(attributeNamesList.indexOf(attributeName), tokenizer.sval);
						}
						Tools.waitForEOL(tokenizer);
						valueSet = null;
					} else {
						// nominal attribute
						valueType = Ontology.NOMINAL;

						tokenizer.pushBack();
						valueSet = new LinkedHashSet<String>();

						// check if nominal value definition starts
						if (tokenizer.nextToken() != '{') {
							throw new IOException("{ expected at beginning of nominal values definition in line " + tokenizer.lineno());
						}

						// read all nominal values until the end of the definition
						while (tokenizer.nextToken() != '}') {
							if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
								throw new IOException("} expected at end of the nominal values definition in line " + tokenizer.lineno());
							} else {
								valueSet.add(tokenizer.sval);
							}
						}

						if (valueSet.size() == 0) {
							throw new IOException("empty definition of nominal values is not suggested in line " + tokenizer.lineno());
						}
					}
					valueTypesList.add(valueType);
					valueSets.add(valueSet);
					Tools.getLastToken(tokenizer, false);
					Tools.getFirstToken(tokenizer);

					if (tokenizer.ttype == StreamTokenizer.TT_EOF)
						throw new IOException("unexpected end of file before data section in line " + tokenizer.lineno());
				}
				String[] attributeNames = new String[attributeNamesList.size()];
				attributeNames = attributeNamesList.toArray(attributeNames);
				setAttributeNames(attributeNames);

				setValueTypes(valueTypesList);

				// expect data declaration
				if (!"@data".equalsIgnoreCase(tokenizer.sval)) {
					throw new IOException("expected keyword '@data' in line " + tokenizer.lineno());
				}

				// check attribute number
				if (attributeNamesList.size() == 0) {
					throw new IOException("no attributes were declared in the ARFF file, please declare attributes with the '@attribute' keyword.");
				}
			}

			@Override
			public boolean next() {
				try {
					Tools.getFirstToken(tokenizer);
				} catch (IOException e) {
					return false;
				}
				if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
					return false;
				}
				try {
					tokens = new String[getColumnCount()];
					if (tokenizer.ttype == '{') {
						for (int t = 0; t < tokens.length; t++) {
							tokens[t] = "0";
						}
						do {
							if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
								throw new IOException("unexpedted end of line " + tokenizer.lineno());
							}
							if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
								throw new IOException("unexpedted end of file in line " + tokenizer.lineno());
							} 
							if (tokenizer.ttype == '}') {
								break;
							}
							int index = Integer.valueOf(tokenizer.sval);
							Tools.getNextToken(tokenizer);
							if  (tokenizer.ttype == '?') {
								tokens[index] = null;
							} else {
								if (tokenizer.ttype != StreamTokenizer.TT_WORD) {
									throw new IOException("not a valid value '" + tokenizer.sval + "' in line " + tokenizer.lineno());
								}
								tokens[index] = tokenizer.sval;
							}
						} while (true);
						Tools.getLastToken(tokenizer, true);
					} else {
						for (int i = 0; i < getColumnCount(); i++) {
							if (i > 0) {
								try {
									Tools.getNextToken(tokenizer);
								} catch (IOException e) {
									// this exception indicates a malformed .arff file, log it
									try {
										LogService.getRoot().log(Level.WARNING, 
												"ArffExampleSource.unexpected_end_of_file",
												filePortHandler.getSelectedFile().getName());
										throw e; // needs to be rethrown for the global catch below
									} catch (OperatorException e1) {}
								}
							}
							if (tokenizer.ttype == '?') {
								tokens[i] = null;
							} else {
								if (tokenizer.ttype != StreamTokenizer.TT_WORD) {
									throw new IOException("not a valid value '" + tokenizer.sval + "' in line " + tokenizer.lineno());
								}
								tokens[i] = tokenizer.sval;
							}
						}
						Tools.getLastToken(tokenizer, true);
					}
					return true;
				} catch (IOException e) {
					return false;
				}
			}

			@Override
			public void close() {
				try {
					in.close();
				} catch (IOException e) {
					
				}
			}

			@Override
			public int getNumberOfColumnsInCurrentRow() {
				return tokens.length;
			}

			@Override
			public boolean isMissing(int columnIndex) {
				return tokens[columnIndex] == null || tokens[columnIndex].isEmpty();
			}

			@Override
			public Number getNumber(int columnIndex) {
				try {
					return numberFormat.parse(tokens[columnIndex]);
				} catch (ParseException e) {
				}
				return null; 
			}

			@Override
			public String getString(int columnIndex) {
				return tokens[columnIndex];
			}

			@Override
			public Date getDate(int columnIndex) {
				String pattern = this.datePattern.get(columnIndex);
				try {
					if(pattern != null){
						SimpleDateFormat format = new SimpleDateFormat(pattern);
						return format.parse(tokens[columnIndex]);
					}
					return dateFormat.parse(tokens[columnIndex]);					
				} catch (ParseException e) {
				}
				return null;
			}
		};			
	}

	/** Creates a StreamTokenizer for reading ARFF files. */
	private StreamTokenizer createTokenizer(Reader in){
		StreamTokenizer tokenizer = new StreamTokenizer(in);
		tokenizer.resetSyntax();         
		tokenizer.whitespaceChars(0, ' ');    
		tokenizer.wordChars(' '+1,'\u00FF');
		tokenizer.whitespaceChars(',',',');
		tokenizer.commentChar('%');
		tokenizer.quoteChar('"');
		tokenizer.quoteChar('\'');
		tokenizer.ordinaryChar('{');
		tokenizer.ordinaryChar('}');
		tokenizer.eolIsSignificant(true);
		return tokenizer;
	}

	@Override
	protected boolean supportsEncoding() {
		return true;
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = new LinkedList<ParameterType>();
		types.add(FileInputPortHandler.makeFileParameterType(this, PARAMETER_DATA_FILE, "Name of the Arff file to read the data from.", "arff", new PortProvider() {
//			@Override
			public Port getPort() {			
				return fileInputPort;
			}
		}));
		types.addAll(super.getParameterTypes());
		types.addAll(StrictDecimalFormat.getParameterTypes(this));
		return types;
	}
}
