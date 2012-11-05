package dk.codeunited.kulturarv.log;

import android.util.Log;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;

/**
 * @author Kostas Rutkauskas
 */
public class LogcatLogWriter implements IApplicationLogWriter {

	private static final String LOG_TAG = KulturarvApplication.getAppContext()
			.getString(R.string.app_tag);

	@Override
	public void debug(String message) {
		Log.d(LOG_TAG, message);
	}

	@Override
	public void warning(String message) {
		Log.w(LOG_TAG, message);
	}

	@Override
	public void error(String message, Throwable exception) {
		if (exception != null) {
			Log.e(LOG_TAG, message, exception);
		} else {
			Log.e(LOG_TAG, message);
		}
	}

	@Override
	public void fatal(String message, Throwable exception) {
		Log.wtf(LOG_TAG, message);
	}
}