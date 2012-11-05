package dk.codeunited.kulturarv.mgr.location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.maps.GeoPoint;

import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.activity.MixMap;
import dk.codeunited.kulturarv.mgr.context.MixContext;

/**
 * @author Kostas Rutkauskas
 */
public class LocationMgrImpl implements LocationFinder {

	static final String PREF_LAST_KNOWN_LOCATION = "PREF_LAST_KNOWN_LOCATION";
	static final int MIN_DURATION_BETWEEN_LOCATIONS = 15 * 1000;
	static final int MIN_DISTANCE_BETWEEN_LOCATIONS = 15;
	static final int MAX_SEARCH_BEST_PROVIDER_SECONDS = 30;
	static final int MAX_ACCEPTABLE_LOCATION_CACHE_IN_MINUTES = 5;
	static final int MIN_ACCEPTABLE_ACCURACY_IN_METERS = 10;

	public interface SimpleLocationListener {
		void onLocationAqcuired(Location l);

		void onNoProvidersFound();
	}

	private static LocationManager lm;
	private Location curLoc;
	private List<LocationListener> activeListeners = new ArrayList<LocationListener>();
	private List<LocationListener> enabledLocationProviderCheckers = new ArrayList<LocationListener>();
	private List<SimpleLocationListener> simpleListeners = new ArrayList<SimpleLocationListener>();
	private Date searchStarted;
	private String bestProvider;

	private static void refreshLm() {
		lm = null;
		lm = (LocationManager) KulturarvApplication.getAppContext()
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public static Location getLastKnownLocation() {
		refreshLm();

		Context ctx = KulturarvApplication.getAppContext();
		Location l;

		// 1. Try getting last known location by BEST provider from OS
		l = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
		if (l != null) {
			return l;
		}

		// 2. Try getting last known location by ANY provider from OS with fix
		// no older than 15min
		for (String provider : lm.getAllProviders()) {
			Location otherLoc = lm.getLastKnownLocation(provider);
			if (l == null
					|| otherLoc != null
					&& (new Date().getTime() - otherLoc.getTime()) / 60000 < MAX_ACCEPTABLE_LOCATION_CACHE_IN_MINUTES
					&& (l.getTime() < otherLoc.getTime())) {
				l = otherLoc;
			}
		}

		if (l != null) {
			return l;
		}

		// 3. Try getting last known location from Kulturarv app
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		if (prefs.contains(PREF_LAST_KNOWN_LOCATION)) {
			return deserialize(prefs.getString(PREF_LAST_KNOWN_LOCATION, null));
		}

		return null;
	}

	@Override
	public void addSimpleListener(SimpleLocationListener listener) {
		if (!simpleListeners.contains(listener)) {
			simpleListeners.add(listener);
		}
	}

	@Override
	public void removeSimpleListener(SimpleLocationListener listener) {
		if (simpleListeners.contains(listener)) {
			simpleListeners.remove(listener);
		}
	}

	private static void setLastKnownLocation(Location l) {
		Context ctx = KulturarvApplication.getAppContext();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PREF_LAST_KNOWN_LOCATION, serialize(l));
		editor.commit();
	}

	private static String serialize(Location l) {
		if (l == null)
			return null;
		return String.format("%f;%f;%f;%d", l.getLatitude(), l.getLongitude(),
				l.getAltitude(), l.getTime());
	}

	private static Location deserialize(String location) {
		if (location == null || location.split(";").length != 4) {
			return null;
		}

		Location l = new Location("LastKnown");
		l.setLatitude(Double.parseDouble(location.split(";")[0]));
		l.setLongitude(Double.parseDouble(location.split(";")[1]));
		l.setAltitude(Double.parseDouble(location.split(";")[2]));
		l.setTime(Long.parseLong(location.split(";")[3]));

		return l;
	}

	public void startListeningForLocations(long minTimeMilis,
			float minDistance, List<String> useProviders) {
		activeListeners.clear();

		for (String provider : useProviders) {
			if (!lm.isProviderEnabled(provider)) {
				continue;
			}

			lm.requestLocationUpdates(provider, minTimeMilis, minDistance,
					locationFinder);
			activeListeners.add(locationFinder);
		}
	}

	private void restart() {
		stopListeningForLocations();

		startMonitoringForEnabledProviders();

		determineBestProvider();
	}

	private void determineBestProvider() {
		// bestProvider = null;
		activeListeners.clear();
		searchStarted = new Date();

		for (String provider : lm.getAllProviders()) {
			if (!lm.isProviderEnabled(provider)) {
				continue;
			}

			lm.requestLocationUpdates(provider, 0, 0, bestProviderFinder);
			activeListeners.add(bestProviderFinder);
		}
	}

	final LocationListener bestProviderFinder = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			if (isNewLocationBetter(curLoc, location)) {
				setCurrentLocation(location);
				notifyListenersAboutNewLocation();
				bestProvider = location.getProvider();
			}

			if ((new Date().getTime() - searchStarted.getTime()) / 1000 > 30) {
				// stop looking for provider
				onBestProviderFound();
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	final LocationListener locationFinder = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			setCurrentLocation(location);
			notifyListenersAboutNewLocation();
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	private void startMonitoringForEnabledProviders() {
		stopMonitoringForEnabledProviders();
		LocationListener enabledLocationProvidersChecker = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				//
			}

			@Override
			public void onProviderDisabled(String provider) {
				if (bestProvider != null && bestProvider.equals(provider)) {
					restart();
				}
			}

			@Override
			public void onProviderEnabled(String provider) {
				restart();
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
				// TODO Auto-generated method stub

			}
		};

		enabledLocationProviderCheckers.clear();
		for (String provider : lm.getAllProviders()) {
			enabledLocationProviderCheckers
					.add(enabledLocationProvidersChecker);
			lm.requestLocationUpdates(provider, 15 * 1000, 0,
					enabledLocationProvidersChecker);
		}
	}

	private void onBestProviderFound() {
		stopListeningForLocations();

		if (bestProvider == null) {
			notifyListenersAboutUnavailableProviders();
		} else {
			List<String> useProviders = new ArrayList<String>();
			useProviders.add(bestProvider);
			startListeningForLocations(MIN_DURATION_BETWEEN_LOCATIONS,
					MIN_DISTANCE_BETWEEN_LOCATIONS, useProviders);
		}
	}

	private synchronized void setCurrentLocation(Location l) {
		curLoc = l;
		setLastKnownLocation(l);
		addWalkingPathPosition(curLoc);
	}

	private void notifyListenersAboutNewLocation() {
		for (SimpleLocationListener simpleLocationListener : simpleListeners) {
			if (simpleLocationListener != null) {
				simpleLocationListener.onLocationAqcuired(curLoc);
			}
		}
	}

	private void notifyListenersAboutUnavailableProviders() {
		for (SimpleLocationListener simpleLocationListener : simpleListeners) {
			if (simpleLocationListener != null) {
				simpleLocationListener.onNoProvidersFound();
			}
		}
	}

	private boolean isNewLocationBetter(Location oldLoc, Location newLoc) {
		return (newLoc != null)
				&& ((oldLoc == null || !oldLoc.hasAccuracy()
						&& newLoc.hasAccuracy() || newLoc.hasAccuracy()
						&& oldLoc.getAccuracy() > newLoc.getAccuracy()));
	}

	public void stopListeningForLocations() {
		for (LocationListener listener : activeListeners) {
			lm.removeUpdates(listener);
			listener = null;
		}
		refreshLm();
	}

	public void stopMonitoringForEnabledProviders() {
		for (LocationListener listener : enabledLocationProviderCheckers) {
			lm.removeUpdates(listener);
		}
	}

	public LocationMgrImpl(MixContext mixContext) {
		//
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.codeunited.kulturarv.mgr.location.LocationFinder#getCurrentLocation()
	 */
	@Override
	public Location getCurrentLocation() {
		if (curLoc == null) {
			return getLastKnownLocation();
		}
		synchronized (curLoc) {
			return curLoc;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * dk.codeunited.kulturarv.mgr.location.LocationFinder#getGeomagneticField()
	 */
	@Override
	public GeomagneticField getGeomagneticField() {
		Location location = getCurrentLocation();
		GeomagneticField gmf = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), System.currentTimeMillis());
		return gmf;
	}

	@Override
	public void switchOn() {
		restart();
	}

	@Override
	public void switchOff() {
		stopListeningForLocations();
		stopMonitoringForEnabledProviders();
	}

	private void addWalkingPathPosition(Location location) {
		MixMap.addWalkingPathPosition(new GeoPoint((int) (location
				.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
	}
}