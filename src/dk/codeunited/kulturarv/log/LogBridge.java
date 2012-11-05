package dk.codeunited.kulturarv.log;

import java.util.List;

/**
 * @author Kostas Rutkauskas
 */
public class LogBridge {

	private enum LogLevel {
		FATAL(0), ERROR(1), WARNING(2), DEBUG(3);

		private final int index;

		LogLevel(int index) {
			this.index = index;
		}

		public int index() {
			return index;
		}
	}

	static final List<IApplicationLogWriter> applicationLogWriters = ApplicationLoggerFactory
			.getLogWriters();
	private static final LogLevel LOG_LEVEL = LogLevel.DEBUG;

	public static void debug(String message) {
		log(message, LogLevel.DEBUG, null);
	}

	public static void warning(String message) {
		log(message, LogLevel.WARNING, null);
	}

	public static void error(String message, Throwable exception) {
		log(message, LogLevel.ERROR, exception);
	}

	public static void fatal(String message, Throwable exception) {
		log(message, LogLevel.FATAL, exception);
	}

	private static void log(String message, LogLevel logLevel,
			Throwable exception) {
		if (LOG_LEVEL.index() >= logLevel.index()) {
			for (IApplicationLogWriter logWriter : ApplicationLoggerFactory
					.getLogWriters()) {
				try {
					switch (logLevel) {
					case FATAL:
						logWriter.fatal(message, exception);
						break;
					case ERROR:
						logWriter.error(message, exception);
						break;
					case WARNING:
						logWriter.warning(message);
						break;
					case DEBUG:
						logWriter.debug(message);
						break;
					}
				} catch (Exception e) {
					//
				}
			}
		}
	}
}