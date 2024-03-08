package adaa.analytics.rules.rm.example.set;


import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IAttributes;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.example.table.IExampleTable;

import java.util.Iterator;


public class HeaderExampleSet extends AbstractExampleSet {
	private static final long serialVersionUID = -255270841843010670L;

	/** The parent example set. */
	private IAttributes attributes;

	public HeaderExampleSet(IExampleSet parent) {
//		cloneAnnotationsFrom(parent);
		this.attributes = (IAttributes) parent.getAttributes().clone();
	}

	public HeaderExampleSet(HeaderExampleSet other) {
//		cloneAnnotationsFrom(other);
		this.attributes = (IAttributes) other.attributes.clone();
	}

	@Override
	public IAttributes getAttributes() {
		return attributes;
	}

	@Override
	public Example getExample(int index) {
		return null;
	}

	@Override
	public Example getExampleFromId(double value) {
		throw new UnsupportedOperationException(
				"The method getExampleFromId(double) is not supported by the header example set.");
	}

	@Override
	public IExampleTable getExampleTable() {
		throw new UnsupportedOperationException("The method getExampleTable() is not supported by the header example set.");
	}

	@Override
	public void remapIds() {
		throw new UnsupportedOperationException("The method remapIds() is not supported by the header example set.");
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public Iterator<Example> iterator() {
		throw new UnsupportedOperationException("The method iterator() is not supported by the header example set.");
	}
}
