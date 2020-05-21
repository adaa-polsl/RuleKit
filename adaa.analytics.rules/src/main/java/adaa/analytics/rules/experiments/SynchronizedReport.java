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
package adaa.analytics.rules.experiments;

import java.io.*;

public class SynchronizedReport {
	
	protected Writer writer;
	
	protected boolean empty = true;
	
	protected String file;
	
	public String getFile() { return file; }
	
	public SynchronizedReport(String name) throws UnsupportedEncodingException, FileNotFoundException {
    	file = name;
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name), "utf-8"));
	}

	public SynchronizedReport(String name, String header) throws IOException {
		file = name;
		writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(name), "utf-8"));
		writer.write(header);
	}
	
	
	public synchronized void append(String text) throws IOException {
		writer.write(text);
		writer.flush();
	}
	
	public synchronized void add(String[] headers, String row) throws IOException {
		if (empty == true) {
			for (String h: headers) {
				writer.write(h + "\n");
			}
			empty = false;
		}
		
		writer.write(row + "\n");
		writer.flush();
	}
}
