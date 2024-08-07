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
package adaa.analytics.rules.consoles;

import java.io.*;

public class SynchronizedReport {

	private static VersionService versionService = new VersionService();

	protected Writer writer;
	
	protected boolean empty = true;
	
	protected String file;
	
	public String getFile() { return file; }


	public SynchronizedReport(String dirPath, String filePath) throws IOException {
		if (filePath!=null && !filePath.isEmpty()) {
			file = dirPath + "/" + filePath;
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			if (filePath.endsWith("csv"))
				writer.write(versionService.getSimpleHeader());
			else
				writer.write(versionService.getHeader());
		}
	}
	
	
	public synchronized void append(String text) throws IOException {
		if (writer!=null) {
			writer.write(text);
			writer.flush();
		}
	}
	
	public synchronized void add(String[] headers, String row) throws IOException {
		if (writer!=null) {
			if (empty == true) {
				for (String h : headers) {
					writer.write(h + "\n");
				}
				empty = false;
			}

			writer.write(row + "\n");
			writer.flush();
		}
	}
}
