package dk.codeunited.kulturarv.kulturarvClient.util;

import dk.codeunited.kulturarv.kulturarvClient.util.jhlabs.Point2D;
import dk.codeunited.kulturarv.kulturarvClient.util.jhlabs.Projection;
import dk.codeunited.kulturarv.kulturarvClient.util.jhlabs.ProjectionFactory;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * @author Maksim Sorokin
 */
public class CoordinatesProjectionsConverter {

	/**
	 * Converts from Google Maps latitude/longitude to EPSG:25832. Please note,
	 * that technically Google Maps is EPSG:4326.
	 * 
	 * @param googleMapsPoint
	 *            latitude/longitude of Google Maps in a point
	 * @return converted coordinate to EPSG:25832
	 */
	public static Point2D.Double fromGoogleMapsToEPSG25832(
			GeoLocation googleMapsCoord) {
		// Projection epsg25832 =
		// ProjectionFactory.getNamedPROJ4CoordinateSystem("epsg:25832");
		Projection epsg25832 = ProjectionFactory.getEPGS25832Projection();

		// for some reason library expects source coordinates to be swapped
		Point2D.Double sourceCoord = new Point2D.Double(
				googleMapsCoord.getLongitudeInDegrees(),
				googleMapsCoord.getLatitudeInDegrees());
		Point2D.Double targetCoord = new Point2D.Double(0, 0);
		epsg25832.transform(sourceCoord, targetCoord);

		return targetCoord;
	}

	/**
	 * Converts EPSG:25832 coordinate to Google Maps latitude/longitude
	 * (technically EPSG:4326).
	 * 
	 * @param epsg25832Coord
	 *            EPSG:25832 coordinate
	 * @return Google Maps latitude/longitude
	 */
	public static GeoLocation fromEPSG25832ToGoogleMaps(
			Point2D.Double epsg25832Coord) {
		// Projection epsg25832 =
		// ProjectionFactory.getNamedPROJ4CoordinateSystem("epsg:25832");
		Projection epsg25832 = ProjectionFactory.getEPGS25832Projection();

		Point2D.Double destCoord = new Point2D.Double(0, 0);
		epsg25832.inverseTransform(epsg25832Coord, destCoord);

		// for some reason library returns target coordinates swapped
		return GeoLocation.fromDegrees(destCoord.y, destCoord.x, 0);
	}
}
