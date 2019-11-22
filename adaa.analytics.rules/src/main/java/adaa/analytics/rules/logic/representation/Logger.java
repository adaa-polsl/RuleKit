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
package adaa.analytics.rules.logic.representation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Auxiliary singleton class for logging. 
 * @author Adam Gudys
 *
 */
public class Logger {
	
	/**
	 * Wrapper for PrintStream Java class which encapsulates logging level.
	 * @author Adam Gudys
	 *
	 */
	private class LogStream {
		public PrintStream stream;
		public Level level;
		
		public LogStream (PrintStream stream, Level level) {
			this.stream = stream;
			this.level = level;
		}
	}
	
	/** Singleton instance */
	private static Logger instance = new Logger();
	
	/** Collection of logging streams */
	private List<LogStream> streams = new ArrayList<LogStream>();
	
	/** Gets {@link #instance}. */ 
	public static Logger getInstance() { return instance; }
	
	/**
	 * Adds a new print stream.
	 * @param ps Print stream.
	 * @param lvl Logging level.
	 */
	public void addStream(PrintStream ps, Level lvl) { streams.add(new LogStream(ps, lvl)); }
	
	/**
	 * Private singleton constructor.
	 */
	private Logger() {}
	
	/**
	 * Singleton method which calls {@link #run(String, Level)} on the singleton instance.
	 * @param msg Message.
	 * @param lvl Importance level of a message.
	 */
	public static void log(String msg, Level lvl) {
		instance.run(msg, lvl);
	}
	
	/**
	 * Logs a message on all streams with logging level smaller than the message level.
	 * @param msg Message.
	 * @param lvl Importance level of message.
	 */
	public void run(String msg, Level lvl) {
			for (LogStream s : streams) {
				if (lvl.intValue() >= s.level.intValue()) {
				s.stream.print(msg);
			}
		}
	}
}
