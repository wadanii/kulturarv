package dk.codeunited.kulturarv.util;

import java.util.Random;

import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * Knows how to magically adjust marker altitude based on the current observer
 * and target coordinates (latitude, longitude).
 * 
 * @author Maksim Sorokin
 */
public final class AltitudeAdjuster {

	/**
	 * Basically means that it is comfortable for the observer to see an object
	 * within 1km between 0 and 150 meters. The closer target object will be,
	 * less will be the comfortable altitude range.
	 */
	private final static double ALTITUDE_RATIO = 150 / (double) 1000;

	/**
	 * Based on the observer and target coordinates, returns random altitude
	 * that is comfortable to see for the observer.
	 * 
	 * @param observer
	 *            observer coordinate
	 * @param target
	 *            target coordinate
	 * @return altitude of the target coordinate, that is comfortable to see for
	 *         the observer
	 */
	public static double getAltitude(GeoLocation observer, GeoLocation target) {
		return observer.getAltitudeInDegrees()
				+ new Random()
						.nextInt((int) (observer.distanceTo(target) * ALTITUDE_RATIO));
	}

	/**
	 * @see #getAltitude(GeoLocation, GeoLocation)
	 * 
	 * @param observer
	 *            observer coordinate
	 * @param latitude
	 *            latitude of the target coordinate
	 * @param longitude
	 *            longitude of the target coordinate
	 * @return altitude of the target coordinate, that is comfortable to see for
	 *         the observer
	 */
	public static double getAltitude(GeoLocation observer, double latitude,
			double longitude) {
		GeoLocation target = GeoLocation.fromDegrees(latitude, longitude, 0);
		return getAltitude(observer, target);
	}
}