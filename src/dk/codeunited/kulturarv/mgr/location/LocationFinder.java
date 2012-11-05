package dk.codeunited.kulturarv.mgr.location;

import dk.codeunited.kulturarv.mgr.location.LocationMgrImpl.SimpleLocationListener;
import android.hardware.GeomagneticField;
import android.location.Location;

/**
 * This class is repsonsible for finding the location, and sending it back to
 * the mixcontext.
 * 
 * @author mixare
 * @author Kostas Rutkauskas
 */
public interface LocationFinder {

	/**
	 * Returns the current location.
	 */
	Location getCurrentLocation();

	/**
	 * Request to active the service
	 */
	void switchOn();

	/**
	 * Request to deactive the service
	 */
	void switchOff();

	/**
	 * 
	 * @return GeomagneticField
	 */
	GeomagneticField getGeomagneticField();

	void addSimpleListener(SimpleLocationListener listener);

	void removeSimpleListener(SimpleLocationListener listener);
}