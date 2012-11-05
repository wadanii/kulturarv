package dk.codeunited.kulturarv;

import android.app.Application;
import android.content.Context;

/**
 * @author Kostas Rutkauskas
 */
public class KulturarvApplication extends Application {

	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		acquireContext();
	}

	public static Context getAppContext() {
		return context;
	}

	private void acquireContext() {
		KulturarvApplication.context = getApplicationContext();
	}
}