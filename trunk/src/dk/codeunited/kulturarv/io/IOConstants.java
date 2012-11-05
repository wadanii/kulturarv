package dk.codeunited.kulturarv.io;

import java.io.File;

import dk.codeunited.kulturarv.KulturarvApplication;

/**
 * @author Kostas Rutkauskas
 */
public class IOConstants {

	public static final String APPLICATION_DATA_DIR = "/data/data/"
			+ KulturarvApplication.getAppContext().getPackageName();

	private static final String APPLICATION_WORK_DIR = String.format("%s%s%s",
			android.os.Environment.getExternalStorageDirectory(),
			android.os.Environment.getExternalStorageDirectory().getPath()
					.endsWith(File.separator) ? "" : File.separator,
			KulturarvApplication.getAppContext().getPackageName());

	public static final String APPLICATION_LOG_DIR = APPLICATION_WORK_DIR
			+ "/log/";
}