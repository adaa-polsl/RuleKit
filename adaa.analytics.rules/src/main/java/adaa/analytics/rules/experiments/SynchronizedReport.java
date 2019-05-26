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
