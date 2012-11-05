package dk.codeunited.kulturarv.mgr.location;

import dk.codeunited.kulturarv.mgr.context.MixContext;

/**
 * Factory Of LocationFinder
 * 
 * @author Alessandro Staniscia
 * @author mixare
 */
public class LocationFinderFactory {

	/**
	 * Hide implementation Of LocationFinder
	 * 
	 * @param mixContext
	 * @return LocationFinder
	 */
	public static LocationFinder makeLocationFinder(MixContext mixContext) {
		return new LocationMgrImpl(mixContext);
	}
}