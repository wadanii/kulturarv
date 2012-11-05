package dk.codeunited.kulturarv.kulturarvClient.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * Represents a slice of a observer view. A slice is a triangle, with one point
 * being in observer center, and another two are located within certain radius
 * from the observer center.
 * 
 * <pre>
 *           |
 *           |     /
 *           |    /
 *           |   /
 *           |  /
 *           | /
 *           ./
 * 
 * 
 * </pre>
 * 
 * @author Maksim Sorokin
 */
public class Slice {

	private GeoLocation center;

	private GeoLocation firstBoundPoint;

	private GeoLocation secondBoundPoint;

	public Slice(GeoLocation center, GeoLocation firstBoundPoint,
			GeoLocation secondBoundPoint) {
		this.center = center;
		this.firstBoundPoint = firstBoundPoint;
		this.secondBoundPoint = secondBoundPoint;
	}

	public boolean doesCoodinateLiesWithin(GeoLocation target) {
		boolean inSlice = false;

		List<Entry<GeoLocation, GeoLocation>> triangleSides = getSliceTriangleSides();

		for (Entry<GeoLocation, GeoLocation> triangleSide : triangleSides) {
			GeoLocation point1 = triangleSide.getKey();
			GeoLocation point2 = triangleSide.getValue();

			double targetLat = target.getLatitudeInDegrees();
			double targetLon = target.getLongitudeInDegrees();

			double lat1 = point1.getLatitudeInDegrees();
			double lon1 = point1.getLongitudeInDegrees();
			double lat2 = point2.getLatitudeInDegrees();
			double lon2 = point2.getLongitudeInDegrees();

			if (((lon1 < targetLon) && (lon2 >= targetLon))
					|| ((lon2 < targetLon) && (lon1 >= targetLon))) {
				if (lat1 + (targetLon - lon1) / (lon2 - lon1) * (lat2 - lat1) < targetLat) {
					inSlice = !inSlice;
				}
			}
		}

		return inSlice;
	}

	private List<Entry<GeoLocation, GeoLocation>> getSliceTriangleSides() {
		List<Entry<GeoLocation, GeoLocation>> triangleSides = new ArrayList<Entry<GeoLocation, GeoLocation>>();
		triangleSides.add(constructMapEntry(center, firstBoundPoint));
		triangleSides.add(constructMapEntry(firstBoundPoint, secondBoundPoint));
		triangleSides.add(constructMapEntry(secondBoundPoint, center));

		return triangleSides;
	}

	private <K, V> Entry<K, V> constructMapEntry(K key, V value) {
		Map<K, V> map = new HashMap<K, V>();
		map.put(key, value);
		return map.entrySet().iterator().next();
	}
}