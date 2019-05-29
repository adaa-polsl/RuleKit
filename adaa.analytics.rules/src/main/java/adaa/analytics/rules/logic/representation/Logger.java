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

public class Logger {
	
	private class LogStream {
		public PrintStream stream;
		public Level level;
		
		public LogStream (PrintStream stream, Level level) {
			this.stream = stream;
			this.level = level;
		}
	}
	
	private static Logger instance = new Logger();
	
	private List<LogStream> streams = new ArrayList<LogStream>();
	
	public static Logger getInstance() { return instance; }
	
	public void addStream(PrintStream ps, Level lvl) { streams.add(new LogStream(ps, lvl)); }
	
	private Logger() {}
	
	public static void log(String msg, Level lvl) {
		instance.run(msg, lvl);
	}
	
	public void run(String msg, Level lvl) {
			for (LogStream s : streams) {
				if (lvl.intValue() >= s.level.intValue()) {
				s.stream.print(msg);
			}
		}
	}
}
