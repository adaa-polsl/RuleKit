/**
 * Copyright (C) 2001-2019 by RapidMiner and the contributors
 * 
 * Complete list of developers available at our web site:
 * 
 * http://rapidminer.com
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see http://www.gnu.org/licenses/.
*/
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
