package dk.codeunited.kulturarv.mgr.context;

import static android.view.KeyEvent.KEYCODE_CAMERA;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.KeyEvent.KEYCODE_DPAD_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_LEFT;
import static android.view.KeyEvent.KEYCODE_DPAD_RIGHT;
import static android.view.KeyEvent.KEYCODE_DPAD_UP;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.Camera;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.data.DataSourceStorage;
import dk.codeunited.kulturarv.gui.RadarPoints;
import dk.codeunited.kulturarv.kulturarvClient.KulturarvUnavailableException;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.marker.ImageMarker;
import dk.codeunited.kulturarv.marker.LocalMarker;
import dk.codeunited.kulturarv.mgr.context.task.MarkerDownloaderTask;
import dk.codeunited.kulturarv.mgr.context.task.MarkerDownloaderTask.OnMarkersDownloadedListener;
import dk.codeunited.kulturarv.mgr.downloader.DownloadRequestFactory;
import dk.codeunited.kulturarv.mgr.downloader.GeoLocationBasedDownloadRequest;
import dk.codeunited.kulturarv.mgr.location.LocationMgrImpl.SimpleLocationListener;
import dk.codeunited.kulturarv.toast.ErrorToast;

/**
 * This class is able to update the markers and the radar. It also handles some
 * user events
 * 
 * @author daniele
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class DataView implements OnMarkersDownloadedListener,
		SimpleLocationListener {

	/** current context */
	private MixContext mixContext;
	/** is the view Inited? */
	private boolean isInit;

	/** width and height of the view */
	private int width, height;

	/**
	 * _NOT_ the android camera, the class that takes care of the transformation
	 */
	private Camera cam;

	private MixState state = new MixState();

	/** The view can be "frozen" for debug purposes */
	private boolean frozen;

	private static final String GENERAL_PREFERENCES = "GENERAL_PREFERENCES";
	public static final String PREF_FLOAT_CURRENT_RADIUS = "PREF_FLOAT_CURRENT_RADIUS";

	private static float LOCATION_TO_RADIUS_RATION_THRESHOLD_FOR_REFRESH = 0.2f;
	private static int REFRESH_TRIGGER_TIME_THRESHOLD_SECONDS = 3 * 60;

	public static final float default_radius = 0.2f;
	public static final float max_radius = 1;

	/** timer to refresh the browser */
	private Timer refresh = null;

	private boolean isLauncherStarted;

	private ArrayList<UIEvent> uiEvents = new ArrayList<UIEvent>();

	private RadarPoints radarPoints = new RadarPoints();
	private ScreenLine lrl = new ScreenLine();
	private ScreenLine rrl = new ScreenLine();
	private float rx = 10, ry = 20;
	private float addX = 0, addY = 0;

	private List<Marker> markers = new ArrayList<Marker>();
	private List<MarkerDownloaderTask> downloaderTasks = new ArrayList<MarkerDownloaderTask>();
	private Handler delayedDownloadTasksHandler = new Handler();
	private List<Runnable> delayedDownloadTasks = new ArrayList<Runnable>();

	private Location lastRefreshLocation;
	private Date lastRefreshTriggerTime;

	/**
	 * Constructor
	 */
	public DataView(MixContext ctx) {
		this.mixContext = ctx;
	}

	public MixContext getContext() {
		return mixContext;
	}

	public boolean isLauncherStarted() {
		return isLauncherStarted;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public static float getRadius() {
		SharedPreferences prefs = KulturarvApplication.getAppContext()
				.getSharedPreferences(GENERAL_PREFERENCES, 0);
		return prefs.getFloat(PREF_FLOAT_CURRENT_RADIUS, default_radius);
	}

	public static void setRadius(float radius) {
		SharedPreferences prefs = KulturarvApplication.getAppContext()
				.getSharedPreferences(GENERAL_PREFERENCES, 0);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(PREF_FLOAT_CURRENT_RADIUS, radius);
		editor.commit();
	}

	public void start() {
		mixContext.getLocationFinder().addSimpleListener(this);
	}

	public void stop() {
		if (refresh != null) {
			refresh.cancel();
		}
		mixContext.getLocationFinder().removeSimpleListener(this);
		cancelAllDelayedDownloaderTasks();
		cancelAllRunningDownloaderTasks();
	}

	public boolean isInited() {
		return isInit;
	}

	public void init(int widthInit, int heightInit) {
		try {
			width = widthInit;
			height = heightInit;

			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);

			lrl.set(0, -RadarPoints.RADIUS);
			lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
			lrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
			rrl.set(0, -RadarPoints.RADIUS);
			rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
			rrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frozen = false;
		isInit = true;
	}

	public void draw(PaintScreen dw) {
		mixContext.getRM(cam.transform);

		state.calcPitchBearing(cam.transform);

		List<Marker> markers = getMarkers();
		for (int i = markers.size() - 1; i >= 0; i--) {
			Marker ma = markers.get(i);
			if (ma.isActive() && (ma.getDistance() / 1000f < getRadius())) {
				// To increase performance don't recalculate position vector
				// for every marker on every draw call, instead do this only
				// after onLocationChanged and after downloading new marker
				if (!frozen) {
					ma.calcPaint(cam, addX, addY);
				}
				ma.draw(dw);
			}
		}

		// Draw Radar
		drawRadar(dw);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(KulturarvApplication
						.getAppContext());

		if (prefs.getBoolean(
				mixContext.getResources()
						.getString(R.string.pref_show_info_box), true)) {
			drawLocationInfoBox(dw);
		}

		// Get next event
		UIEvent evt = null;
		synchronized (uiEvents) {
			if (uiEvents.size() > 0) {
				evt = uiEvents.get(0);
				uiEvents.remove(0);
			}
		}
		if (evt != null) {
			switch (evt.type) {
			case UIEvent.KEY:
				handleKeyEvent((KeyEvent) evt);
				break;
			case UIEvent.CLICK:
				handleClickEvent((ClickEvent) evt);
				break;
			}
		}
	}

	private void showNotification(final String notification) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(KulturarvApplication
						.getAppContext());

		if (prefs.getBoolean(
				mixContext.getResources().getString(
						R.string.pref_show_dataset_updates), true)) {
			mixContext.getActualMixView().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(mixContext, notification, Toast.LENGTH_SHORT)
							.show();
				}
			});
		}
	}

	private void showErrorNotification(final String notification) {
		mixContext.getActualMixView().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				ErrorToast t = new ErrorToast(mixContext.getActualMixView());
				t.setErrorText(notification);
				t.show();
			}
		});
	}

	/**
	 * Handles drawing radar and direction.
	 * 
	 * @param PaintScreen
	 *            screen that radar will be drawn to
	 */
	private void drawRadar(PaintScreen dw) {
		String dirTxt = "";
		int bearing = (int) state.getCurBearing();
		int range = (int) (state.getCurBearing() / (360f / 16f));
		// TODO: get strings from the values xml file
		if (range == 15 || range == 0)
			dirTxt = getContext().getString(R.string.N);
		else if (range == 1 || range == 2)
			dirTxt = getContext().getString(R.string.NE);
		else if (range == 3 || range == 4)
			dirTxt = getContext().getString(R.string.E);
		else if (range == 5 || range == 6)
			dirTxt = getContext().getString(R.string.SE);
		else if (range == 7 || range == 8)
			dirTxt = getContext().getString(R.string.S);
		else if (range == 9 || range == 10)
			dirTxt = getContext().getString(R.string.SW);
		else if (range == 11 || range == 12)
			dirTxt = getContext().getString(R.string.W);
		else if (range == 13 || range == 14)
			dirTxt = getContext().getString(R.string.NW);

		radarPoints.view = this;
		dw.paintObj(radarPoints, rx, ry, -state.getCurBearing(), 1);
		dw.setFill(false);
		dw.setColor(Color.argb(150, 0, 0, 220));
		dw.setStrokeWidth(0);
		dw.paintLine(lrl.x, lrl.y, rx + RadarPoints.RADIUS, ry
				+ RadarPoints.RADIUS);
		dw.paintLine(rrl.x, rrl.y, rx + RadarPoints.RADIUS, ry
				+ RadarPoints.RADIUS);
		dw.setColor(Color.rgb(255, 255, 255));
		dw.setFontSize(12);

		radarText(dw, MixUtils.formatDist(getRadius() * 1000), rx
				+ RadarPoints.RADIUS, ry + RadarPoints.RADIUS * 2 - 10, false);
		radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx
				+ RadarPoints.RADIUS, ry - 5, true);
	}

	private void drawLocationInfoBox(PaintScreen dw) {
		dw.setFill(true);
		dw.setColor(Color.argb(130, 223, 133, 217));

		int infoBoxWidth = 105;
		int infoBothHeight = 80;
		int infoBoxX = this.width - infoBoxWidth;
		int infoBoxY = 0;
		dw.paintRect(infoBoxX, infoBoxY, infoBoxWidth, infoBothHeight);

		int textOffsetX = 5;
		int textHeight = 17;
		dw.setColor(Color.rgb(255, 255, 255));
		dw.setFill(false);
		dw.setStrokeWidth(0);

		Location currentGPSInfo = this.mixContext.getLocationFinder()
				.getCurrentLocation();

		if (currentGPSInfo != null) {
			DecimalFormat coordinateFormat = new DecimalFormat("#.#####");
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			dw.paintText(
					infoBoxX + textOffsetX,
					textHeight,
					getContext().getString(R.string.lat)
							+ ": "
							+ coordinateFormat.format(currentGPSInfo
									.getLatitude()), false);
			dw.paintText(
					infoBoxX + textOffsetX,
					textHeight * 2,
					getContext().getString(R.string.lon)
							+ ": "
							+ coordinateFormat.format(currentGPSInfo
									.getLongitude()), false);
			dw.paintText(
					infoBoxX + textOffsetX,
					textHeight * 3,
					getContext().getString(R.string.last_fix) + ": "
							+ sdf.format(new Date(currentGPSInfo.getTime())),
					false);
			dw.paintText(infoBoxX + textOffsetX, textHeight * 4,
					getContext().getString(R.string.provider) + ": "
							+ currentGPSInfo.getProvider(), false);
		} else {
			dw.paintText(infoBoxX + textOffsetX, textHeight, getContext()
					.getString(R.string.location_unknown), false);
		}
	}

	private void handleKeyEvent(KeyEvent evt) {
		/** Adjust marker position with keypad */
		final float CONST = 10f;
		switch (evt.keyCode) {
		case KEYCODE_DPAD_LEFT:
			addX -= CONST;
			break;
		case KEYCODE_DPAD_RIGHT:
			addX += CONST;
			break;
		case KEYCODE_DPAD_DOWN:
			addY += CONST;
			break;
		case KEYCODE_DPAD_UP:
			addY -= CONST;
			break;
		case KEYCODE_DPAD_CENTER:
			frozen = !frozen;
			break;
		case KEYCODE_CAMERA:
			frozen = !frozen;
			break; // freeze the overlay with the camera button
		default: // if key is set, then ignore event
			break;
		}
	}

	boolean handleClickEvent(ClickEvent evt) {
		boolean evtHandled = false;

		List<Marker> markers = getMarkers();
		// Handle event
		// the following will traverse the markers in ascending order (by
		// distance) the first marker that
		// matches triggers the event.
		// TODO handle collection of markers. (what if user wants the one at
		// the back)
		for (int i = 0; i < markers.size() && !evtHandled; i++) {
			Marker pm = markers.get(i);
			if (pm.isClickValid(evt.x, evt.y)) {
				String webpage = MixUtils.parseAction(pm.getURL());
				Intent in = new Intent(Intent.ACTION_VIEW);
				in.setData(Uri.parse(webpage));
				mixContext.getActualMixView().startActivity(in);
			}
		}
		return evtHandled;
	}

	private void radarText(PaintScreen dw, String txt, float x, float y,
			boolean bg) {
		float padw = 4, padh = 2;
		float w = dw.getTextWidth(txt) + padw * 2;
		float h = dw.getTextAsc() + dw.getTextDesc() + padh * 2;
		if (bg) {
			dw.setColor(Color.rgb(0, 0, 0));
			dw.setFill(true);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
			dw.setColor(Color.rgb(255, 255, 255));
			dw.setFill(false);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
		}
		dw.paintText(padw + x - w / 2, padh + dw.getTextAsc() + y - h / 2, txt,
				false);
	}

	public void clickEvent(float x, float y) {
		synchronized (uiEvents) {
			uiEvents.add(new ClickEvent(x, y));
		}
	}

	public void keyEvent(int keyCode) {
		synchronized (uiEvents) {
			uiEvents.add(new KeyEvent(keyCode));
		}
	}

	public void clearEvents() {
		synchronized (uiEvents) {
			uiEvents.clear();
		}
	}

	/**
	 * Re-downloads the markers, and draw them on the map.
	 */
	public void refresh(Location l) {
		LogBridge.debug("DataView.refresh()");

		if (l == null) {
			return;
		}

		lastRefreshLocation = l;
		lastRefreshTriggerTime = new Date();

		removeMarkersOfDisabledDataSources();
		refreshDownloaderTasks(l);
	}

	private void removeMarkersOfDisabledDataSources() {
		List<DataSource> enabledDataSources = DataSourceStorage
				.getAllEnabledDataSources();
		Iterator<Marker> iter = getMarkers().iterator();
		while (iter.hasNext()) {
			LocalMarker localMarker = (LocalMarker) iter.next();
			if (!enabledDataSources.contains(localMarker.getDataSource())) {
				iter.remove();
				purgeMarker(localMarker);
			}
		}
	}

	private void refreshDownloaderTasks(Location l) {
		LogBridge.debug("DataView.refreshDownloaderTasks()");

		if (mixContext.getLocationFinder().getCurrentLocation() == null) {
			showNotification(mixContext.getString(R.string.acquiring_location));
		} else {
			cancelAllDelayedDownloaderTasks();
			cancelAllRunningDownloaderTasks();
			startDownloaderTasksForEnabledSources(l);
		}
	}

	private void cancelAllDelayedDownloaderTasks() {
		for (Runnable delayedTask : delayedDownloadTasks) {
			delayedDownloadTasksHandler.removeCallbacks(delayedTask);
		}
	}

	private void cancelAllRunningDownloaderTasks() {
		LogBridge.debug("DataView.cancelAllRunningDownloaderTasks()");
		for (MarkerDownloaderTask task : downloaderTasks) {
			if (task.getStatus() == Status.RUNNING) {
				task.cancel(true);
			}
		}
	}

	private void startDownloaderTasksForEnabledSources(Location l) {
		LogBridge.debug("DataView.startDownloaderTasksForEnabledSources()");
		downloaderTasks.clear();

		Location curFix = mixContext.getLocationFinder().getCurrentLocation();

		for (DataSource dataSource : DataSourceStorage.getDataSources()) {
			if (dataSource.isEnabled()) {
				GeoLocationBasedDownloadRequest request = createDownloadRequest(
						curFix, dataSource);
				MarkerDownloaderTask task = new MarkerDownloaderTask(
						this.getContext(), this);
				task.execute(request);
				downloaderTasks.add(task);
			}
		}
	}

	private GeoLocationBasedDownloadRequest createDownloadRequest(Location l,
			DataSource source) {
		return DownloadRequestFactory.createDownloadRequest(source,
				l.getLatitude(), l.getLongitude(), l.getAltitude(),
				getRadius(), Locale.getDefault().getLanguage());
	}

	private void updateDistances(Location location) {
		for (Marker ma : getMarkers()) {
			float[] dist = new float[3];
			Location.distanceBetween(ma.getLatitude(), ma.getLongitude(),
					location.getLatitude(), location.getLongitude(), dist);
			ma.setDistance(dist[0]);
		}
	}

	public List<Marker> getMarkers() {
		synchronized (markers) {
			return markers;
		}
	}

	@Override
	public void onMarkersDownloaded(DataSource source, List<Marker> newMarkers) {
		Iterator<Marker> iter = getMarkers().iterator();
		while (iter.hasNext()) {
			LocalMarker localMarker = (LocalMarker) iter.next();
			if (localMarker.getDataSource().getId() == source.getId()) {
				iter.remove();
				purgeMarker(localMarker);
			}
		}

		getMarkers().addAll(newMarkers);
		updateMarkersActivationStatus();
		showNotification(String.format(
				getContext().getString(R.string.downloaded_markers),
				newMarkers.size(), source.getName()));
		// TODO: create a trigger on real location change
		onLocationAqcuired(mixContext.getLocationFinder().getCurrentLocation());
	}

	public void purgeMarker(Marker marker) {
		if (marker instanceof ImageMarker) {
			Bitmap image = ((ImageMarker) marker).getImage();
			image.recycle();
			image = null;
		}
	}

	private void updateMarkersActivationStatus() {

		Hashtable<DataSource, Integer> map = new Hashtable<DataSource, Integer>();

		for (Marker ma : getMarkers()) {
			LocalMarker localMarker = (LocalMarker) ma;
			map.put(localMarker.getDataSource(),
					(map.get(localMarker.getDataSource()) != null) ? map
							.get(localMarker.getDataSource()) + 1 : 1);

			boolean belowMax = (map.get(localMarker.getDataSource()) <= localMarker
					.getDataSource().getMaxObjects());

			ma.setActive(belowMax);
		}
	}

	@Override
	public void onDownloadFailed(final DataSource source, Throwable cause) {
		if (cause instanceof KulturarvUnavailableException) {
			showErrorNotification(getContext().getString(
					R.string.kulturarv_unavailable));
		} else {
			showNotification(getContext().getString(
					R.string.failed_downloading_markers_from)
					+ " " + source.getName());
		}

		Runnable delayedDownload = new Runnable() {
			@Override
			public void run() {
				GeoLocationBasedDownloadRequest request = createDownloadRequest(
						mixContext.getLocationFinder().getCurrentLocation(),
						source);
				MarkerDownloaderTask task = new MarkerDownloaderTask(
						DataView.this.getContext(), DataView.this);
				task.execute(request);
				downloaderTasks.add(task);
			}
		};
		delayedDownloadTasks.add(delayedDownload);
		// retry after 10 seconds
		delayedDownloadTasksHandler.postDelayed(delayedDownload, 10 * 1000);
	}

	private boolean locationOffsetRequiresRefresh(Location newLocation) {
		float locationOffsetToRadiusRatio = lastRefreshLocation
				.distanceTo(newLocation) / (DataView.getRadius() * 1000);

		if (locationOffsetToRadiusRatio >= LOCATION_TO_RADIUS_RATION_THRESHOLD_FOR_REFRESH) {
			LogBridge
					.debug("DataView identified a significant location offset.");
			return true;
		}

		if ((new Date().getTime() - lastRefreshTriggerTime.getTime()) / 1000 > REFRESH_TRIGGER_TIME_THRESHOLD_SECONDS) {
			LogBridge.debug("DataView triggers expired location");
			return true;
		}

		return false;
	}

	@Override
	public void onLocationAqcuired(Location location) {
		updateDistances(location);
		for (Marker ma : getMarkers()) {
			ma.update(location);
		}

		if (lastRefreshLocation == null || lastRefreshTriggerTime == null
				|| locationOffsetRequiresRefresh(location)) {
			refresh(location);
		}
	}

	@Override
	public void onNoProvidersFound() {
		// TODO Auto-generated method stub

	}
}

class UIEvent {
	public static final int CLICK = 0;
	public static final int KEY = 1;

	public int type;
}

class ClickEvent extends UIEvent {
	public float x, y;

	public ClickEvent(float x, float y) {
		this.type = CLICK;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}

class KeyEvent extends UIEvent {
	public int keyCode;

	public KeyEvent(int keyCode) {
		this.type = KEY;
		this.keyCode = keyCode;
	}

	@Override
	public String toString() {
		return "(" + keyCode + ")";
	}
}