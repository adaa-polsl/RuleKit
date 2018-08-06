package adaa.analytics.rules.experiments;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class SynchronizedReport {
	
	protected Writer writer;
	
	protected boolean empty = true;
	
	public SynchronizedReport(String name) throws UnsupportedEncodingException, FileNotFoundException {
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
