package dk.codeunited.kulturarv.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.data.DataSource.DataSourceType;
import dk.codeunited.kulturarv.data.DataSource.DisplayMode;

/**
 * @author Kostas Rutkauskas
 */
public class DataSourceStorage {

	private static final String DATA_SOURCES = "DATA_SOURCES";
	private static final String PREFS_VISIBILITY_POSTFIX = "_VISIBLE";
	private static final String PREFS_MAX_OBJECTS_POSTFIX = "_MAX_OBJECTS";

	private static final int DS_ID_KULTURARV = 0;
	private static final int DS_ID_FLICKR = 1;
	private static final int DS_ID_WIKIPEDIA = 2;
	private static final int DS_ID_TWITTER = 3;
	private static final int DS_ID_OSM = 4;
	private static final int DS_ID_INSTAGRAM = 5;

	private static List<DataSource> dataSources;

	static {
		refreshDataSources();
	}

	public static void refreshDataSources() {
		Context ctx = KulturarvApplication.getAppContext();
		dataSources = new ArrayList<DataSource>();

		dataSources.add(new DataSource(DS_ID_KULTURARV, ctx
				.getString(R.string.kulturarv_name), ctx
				.getString(R.string.kulturarv_url), DataSourceType.KULTURARV,
				DisplayMode.CIRCLE_MARKER, getMaxObjects(DS_ID_KULTURARV,
						getDefaultMaxObjects(DS_ID_KULTURARV)),
				R.drawable.kulturarv, Color.rgb(0, 255, 0), false, true));

		dataSources.add(new DataSource(DS_ID_INSTAGRAM, ctx
				.getString(R.string.instagram_name), ctx
				.getString(R.string.instagram_url), DataSourceType.INSTAGRAM,
				DisplayMode.IMAGE_MARKER, getMaxObjects(DS_ID_INSTAGRAM,
						getDefaultMaxObjects(DS_ID_INSTAGRAM)), 0,
				Color.YELLOW, true, false));

		dataSources.add(new DataSource(DS_ID_FLICKR, ctx
				.getString(R.string.flickr_name), ctx
				.getString(R.string.flickr_url), DataSourceType.FLICKR,
				DisplayMode.IMAGE_MARKER, getMaxObjects(DS_ID_FLICKR,
						getDefaultMaxObjects(DS_ID_FLICKR)), 0, Color.WHITE,
				true, false));

		dataSources.add(new DataSource(DS_ID_WIKIPEDIA, ctx
				.getString(R.string.wikipedia_name), ctx
				.getString(R.string.wikipedia_url), DataSourceType.WIKIPEDIA,
				DisplayMode.CIRCLE_MARKER, getMaxObjects(DS_ID_WIKIPEDIA,
						getDefaultMaxObjects(DS_ID_WIKIPEDIA)), 0, Color.GRAY,
				true, true));

		dataSources.add(new DataSource(DS_ID_TWITTER, ctx
				.getString(R.string.twitter_name), ctx
				.getString(R.string.twitter_url), DataSourceType.TWITTER,
				DisplayMode.CIRCLE_MARKER, getMaxObjects(DS_ID_TWITTER,
						getDefaultMaxObjects(DS_ID_TWITTER)), 0, Color.rgb(154,
						228, 232), true, true));
	}

	public static List<DataSource> getDataSources() {
		return dataSources;
	}

	private static String getVisibilityKeyForDataSource(int dataSourceId) {
		return dataSourceId + PREFS_VISIBILITY_POSTFIX;
	}

	private static String getMaxObjectsKeyForDataSource(int dataSourceId) {
		return dataSourceId + PREFS_MAX_OBJECTS_POSTFIX;
	}

	private static SharedPreferences getPreferences() {
		return KulturarvApplication.getAppContext().getSharedPreferences(
				DATA_SOURCES, Context.MODE_PRIVATE);
	}

	public static List<DataSource> getAllEnabledDataSources() {
		List<DataSource> enabledDataSources = new ArrayList<DataSource>();
		for (DataSource dataSource : dataSources) {
			if (dataSource.isEnabled()) {
				enabledDataSources.add(dataSource);
			}
		}
		return enabledDataSources;
	}

	public static boolean isDataSourceVisible(int dataSourceId,
			boolean defaultValue) {
		return getPreferences().getBoolean(
				getVisibilityKeyForDataSource(dataSourceId),
				getDefaultVisibilityForDataSource(dataSourceId));
	}

	private static boolean getDefaultVisibilityForDataSource(int dataSourceId) {
		boolean isVisible = false;

		switch (dataSourceId) {
		case DS_ID_KULTURARV:
		case DS_ID_FLICKR:
		case DS_ID_WIKIPEDIA:
			isVisible = true;
			break;
		}

		return isVisible;
	}

	public static void setDataSourceVisibile(int dataSourceId, boolean visible) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putBoolean(getVisibilityKeyForDataSource(dataSourceId), visible);
		editor.commit();
	}

	public static int getDefaultMaxObjects(int dataSourceId) {
		int defaultMaxObjects = 10;
		switch (dataSourceId) {
		case DS_ID_KULTURARV:
			defaultMaxObjects = 50;
			break;
		case DS_ID_INSTAGRAM:
			defaultMaxObjects = 10;
			break;
		case DS_ID_FLICKR:
			defaultMaxObjects = 10;
			break;
		case DS_ID_WIKIPEDIA:
			defaultMaxObjects = 15;
			break;
		case DS_ID_TWITTER:
			defaultMaxObjects = 5;
			break;
		case DS_ID_OSM:
			defaultMaxObjects = 10;
			break;
		}
		return defaultMaxObjects;
	}

	private static int getMaxObjects(int dataSourceId, int defaultValue) {
		return getPreferences().getInt(
				getMaxObjectsKeyForDataSource(dataSourceId), defaultValue);
	}

	public static void setMaxObjects(int dataSourceId, int maxObjects) {
		SharedPreferences.Editor editor = getPreferences().edit();
		editor.putInt(getMaxObjectsKeyForDataSource(dataSourceId), maxObjects);
		editor.commit();
	}
}