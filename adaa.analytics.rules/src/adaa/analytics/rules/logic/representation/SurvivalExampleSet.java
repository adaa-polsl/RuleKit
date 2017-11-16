package adaa.analytics.rules.logic.representation;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.Annotations;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.ports.ProcessingStep;
import com.rapidminer.tools.LoggingHandler;

public class SurvivalExampleSet implements ExampleSet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3309577729252456020L;
	
	protected ExampleSet wrapped;
	
	protected KaplanMeierEstimator estimators[];

	protected KaplanMeierEstimator trainingEstimator;
	
	public KaplanMeierEstimator getTrainingEstimator() { return trainingEstimator; }

	public KaplanMeierEstimator[] getEstimators() { return estimators; }
	
	public SurvivalExampleSet(ExampleSet ref, KaplanMeierEstimator trainingEstimator) {
		this.wrapped = ref;
		this.estimators = new KaplanMeierEstimator[ref.size()];
		this.trainingEstimator = trainingEstimator;
	}

	@Override
	public String getName() { return wrapped.getName(); }

	@Override
	public String toResultString() { return wrapped.toResultString(); }

	@Override
	public Icon getResultIcon() { return wrapped.getResultIcon(); }

	@Override
	public List getActions() { return wrapped.getActions(); }

	@Override
	public void setSource(String sourceName) { wrapped.setSource(sourceName); }

	@Override
	public String getSource(){ return wrapped.getSource(); }

	@Override
	public void appendOperatorToHistory(Operator operator, OutputPort port) { wrapped.appendOperatorToHistory(operator, port); }

	@Override
	public List<ProcessingStep> getProcessingHistory() { return wrapped.getProcessingHistory(); }

	@Override
	public IOObject copy() { return (IOObject)this.clone(); }
	
	public ExampleSet clone() {
		SurvivalExampleSet s = new SurvivalExampleSet(this.wrapped, this.trainingEstimator);
		return s;
	}

	@Override
	public void write(OutputStream out) throws IOException { wrapped.write(out); }

	@Override
	public LoggingHandler getLog() { return wrapped.getLog(); }

	@Override
	public void setLoggingHandler(LoggingHandler loggingHandler) { wrapped.setLoggingHandler(loggingHandler); }

	@Override
	public Annotations getAnnotations() { return wrapped.getAnnotations(); }

	@Override
	public Iterator<Example> iterator() { return wrapped.iterator(); }

	@Override
	public Attributes getAttributes() { return wrapped.getAttributes(); }

	@Override
	public int size() { return wrapped.size(); }

	@Override
	public ExampleTable getExampleTable() { return wrapped.getExampleTable(); }

	@Override
	public Example getExampleFromId(double value) { return wrapped.getExampleFromId(value); }

	@Override
	public int[] getExampleIndicesFromId(double value) { return wrapped.getExampleIndicesFromId(value); }

	@Override
	public Example getExample(int index) { return wrapped.getExample(index); }

	@Override
	public void remapIds() { wrapped.remapIds(); }

	@Override
	public void writeDataFile(File dataFile, int fractionDigits,
			boolean quoteNominal, boolean zipped, boolean append,
			Charset encoding) throws IOException {
		wrapped.writeDataFile(dataFile, fractionDigits, quoteNominal, zipped, append, encoding);
	}

	@Override
	public void writeAttributeFile(File attFile, File dataFile, Charset encoding)
			throws IOException {
		wrapped.writeAttributeFile(attFile, dataFile, encoding);
	}

	@Override
	public void writeSparseDataFile(File dataFile, int format,
			int fractionDigits, boolean quoteNominal, boolean zipped,
			boolean append, Charset encoding) throws IOException {
		wrapped.writeSparseDataFile(dataFile, format, fractionDigits, quoteNominal, zipped, append, encoding);
	}

	@Override
	public void writeSparseAttributeFile(File attFile, File dataFile, int format, Charset encoding) throws IOException {
		wrapped.writeSparseAttributeFile(attFile, dataFile, format, encoding);
	}

	@Override
	public void recalculateAllAttributeStatistics() {
		wrapped.recalculateAllAttributeStatistics();
	}

	@Override
	public void recalculateAttributeStatistics(Attribute attribute) {
		wrapped.recalculateAttributeStatistics(attribute);
	}

	@Override
	public double getStatistics(Attribute attribute, String statisticsName) {
		return wrapped.getStatistics(attribute, statisticsName);
	}

	@Override
	public double getStatistics(Attribute attribute, String statisticsName, String statisticsParameter) {
		return wrapped.getStatistics(attribute, statisticsName, statisticsParameter);
	}

}
