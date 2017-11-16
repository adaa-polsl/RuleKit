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
