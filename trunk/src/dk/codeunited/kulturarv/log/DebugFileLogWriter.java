package dk.codeunited.kulturarv.log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.Date;

import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;

/**
 * @author Kostas Rutkauskas
 */
public class DebugFileLogWriter implements IApplicationLogWriter {

	static final String LOG_FOLDER = dk.codeunited.kulturarv.io.IOConstants.APPLICATION_LOG_DIR;
	static final String LOG_FILE = KulturarvApplication.getAppContext()
			.getString(R.string.app_name) + ".log";
	static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	@Override
	public void debug(String message) {
		log("DEBUG", message, null);
	}

	@Override
	public void warning(String message) {
		log("WARNING", message, null);
	}

	@Override
	public void error(String message, Throwable exception) {
		log("ERROR", message, exception);
	}

	@Override
	public void fatal(String message, Throwable exception) {
		log("FATAL", message, exception);
	}

	private void log(String severityTag, String message, Throwable exception) {
		Thread t = new Thread(new DebugLogWriterThread(severityTag, message,
				exception));
		t.start();
	}

	private class DebugLogWriterThread implements Runnable {

		private final String severityTag;
		private final String message;
		private final Throwable exception;

		DebugLogWriterThread(String severityTag, String message,
				Throwable exception) {
			this.severityTag = severityTag;
			this.message = message;
			this.exception = exception;
		}

		@Override
		public void run() {

			if (message == null || message.length() == 0 || severityTag == null
					|| severityTag.length() == 0) {
				return;
			}

			try {
				File gpxFolder = new File(LOG_FOLDER);

				if (!gpxFolder.exists()) {
					gpxFolder.mkdirs();
				}

				File logFile = new File(gpxFolder.getPath(), LOG_FILE);

				if (!logFile.exists()) {
					logFile.createNewFile();
				}

				FileOutputStream logStream = new FileOutputStream(logFile, true);
				BufferedOutputStream logOutputStream = new BufferedOutputStream(
						logStream);
				FileLock lock = logStream.getChannel().lock();

				String dateString = dateFormat.format(new Date());
				StringBuilder logMessage = new StringBuilder();
				logMessage.append(String.format("[%s|%s] %s\n", dateString,
						severityTag, message));

				if (exception != null) {
					logMessage.append(String.format("%s\n",
							exception.getMessage()));
					for (StackTraceElement stackTraceElement : exception
							.getStackTrace()) {
						logMessage.append(String.format("\t%s\n",
								stackTraceElement.toString()));
					}
				}

				logOutputStream.write(logMessage.toString().getBytes());
				logOutputStream.flush();
				lock.release();
				logOutputStream.close();
			} catch (Throwable e) {
				// Nothing
			}
		}
	}
}