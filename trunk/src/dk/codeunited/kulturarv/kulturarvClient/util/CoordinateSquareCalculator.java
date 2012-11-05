package dk.codeunited.kulturarv.kulturarvClient.util;

import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * Finds top left (A) and bottom right (B) square points from specific center
 * point (C).
 * 
 * <pre>
 * A
 *  |-------------|
 *  |      |      |
 *  |      |      |
 *  |-------------|
 *  |      |C     |
 *  |      |      |
 *  |-------------|
 *                 B
 * </pre>
 * 
 * @author Maksim Sorokin
 */
public class CoordinateSquareCalculator {

	/**
	 * the length of one minute of latitude in meters, i.e. one nautical mile in
	 * meters.
	 */
	private static final double MINUTES_TO_METERS = 1852d;
	/**
	 * the amount of minutes in one degree.
	 */
	private static final double DEGREE_TO_MINUTES = 60d;

	public static GeoLocation getTopLeft(GeoLocation c, double radius) {
		return getTargetCoordinate(c, 315, radius);
	}

	public static GeoLocation getBottomRight(GeoLocation c, double radius) {
		return getTargetCoordinate(c, 135, radius);
	}

	/**
	 * Calculates the endpoint of movement from a starting point coordinate with
	 * a given length and using a given course.
	 * 
	 * @param c
	 *            starting point coordinate
	 * @param course
	 *            the course to be used for extrapolation in degrees
	 * @param distance
	 *            the distance to be extrapolated in meters
	 * 
	 * @return the extrapolated point.
	 */
	public static GeoLocation getTargetCoordinate(GeoLocation c, double course,
			double distance) {
		double startPointLat = c.getLatitudeInDegrees();
		double startPointLon = c.getLongitudeInDegrees();

		double crs = Math.toRadians(course);
		double d12 = Math.toRadians(distance / MINUTES_TO_METERS
				/ DEGREE_TO_MINUTES);

		double lat1 = Math.toRadians(startPointLat);
		double lon1 = Math.toRadians(startPointLon);

		double lat = Math.asin(Math.sin(lat1) * Math.cos(d12) + Math.cos(lat1)
				* Math.sin(d12) * Math.cos(crs));
		double dlon = Math.atan2(
				Math.sin(crs) * Math.sin(d12) * Math.cos(lat1), Math.cos(d12)
						- Math.sin(lat1) * Math.sin(lat));
		double lon = (lon1 + dlon + Math.PI) % (2 * Math.PI) - Math.PI;

		return GeoLocation.fromDegrees(Math.toDegrees(lat),
				Math.toDegrees(lon), c.getAltitudeInDegrees());
	}
}