package dk.codeunited.kulturarv.log;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.preference.PreferenceManager;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;

/**
 * @author Kostas Rutkauskas
 */
class ApplicationLoggerFactory {

	public static List<IApplicationLogWriter> getLogWriters() {
		List<IApplicationLogWriter> applicationLogWriters = new ArrayList<IApplicationLogWriter>();

		applicationLogWriters.add(new LogcatLogWriter());

		Context ctx = KulturarvApplication.getAppContext();
		boolean debugToFile = PreferenceManager
				.getDefaultSharedPreferences(ctx).getBoolean(
						ctx.getString(R.string.pref_log_to_file), false);

		if (debugToFile) {
			applicationLogWriters.add(new DebugFileLogWriter());
		}

		return applicationLogWriters;
	}
}