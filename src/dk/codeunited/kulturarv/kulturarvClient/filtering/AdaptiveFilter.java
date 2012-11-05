package dk.codeunited.kulturarv.kulturarvClient.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dk.codeunited.kulturarv.kulturarvClient.util.CoordinateSquareCalculator;
import dk.codeunited.kulturarv.model.Building;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * @author Maksim Sorokin
 */
public class AdaptiveFilter {

	private final static int MINIMUM_NUMBER_OF_SLICES = 30;

	private final static int OBSERVER_DEGREES = 360;

	/**
	 * Adaptively filters buildings for better user experience.
	 * 
	 * <p>
	 * 360 degrees of the view area is sliced into separate parts and only one
	 * building is left as a representative of a slice. In this way, the view
	 * will be not cluttered.
	 * 
	 * <p>
	 * In the current implementation, Kulturarv service determines buildings for
	 * area in a square, not in the circle. This have to be kept in mind as in
	 * certain situations, buildings may be filtered, when buildings are located
	 * in the corners of the square.
	 * 
	 * @param center
	 *            center from which the buildings are viewed from
	 * @param radius
	 *            radius from center, where all the buildings belong to. It is
	 *            assumed, that all the buildings lie within the radius from the
	 *            center.
	 * @param buildings
	 *            buildings to apply adaptive filtering on
	 * @param ratings
	 *            ratings to respect in this adaptive filtering
	 * @return filtered buildings
	 */
	public List<Building> filter(GeoLocation center, int radius,
			List<Building> buildings, Map<Long, Integer> ratings) {
		if (buildings.isEmpty()) {
			return new ArrayList<Building>();
		}

		int numberOfSlices = getNumberOfSlices(buildings.size());
		List<Slice> slices = sliceObserversView(center, radius, numberOfSlices);

		Map<Slice, List<Building>> sliceBuildings = distributeBulidingsInSlices(
				slices, buildings);
		List<Building> filtered = pickOneRepresentativePerSlice(sliceBuildings,
				ratings);

		return filtered;
	}

	/**
	 * Always return at least {@value #MINIMUM_NUMBER_OF_SLICES} slices.
	 * Otherwise, markers can be filtered out.
	 * 
	 * @param buildingsAmount
	 *            amount of buildings to put into the slices
	 * @return number of slices
	 */
	private int getNumberOfSlices(int buildingsAmount) {
		return buildingsAmount < MINIMUM_NUMBER_OF_SLICES ? MINIMUM_NUMBER_OF_SLICES
				: buildingsAmount;
	}

	private List<Slice> sliceObserversView(GeoLocation center, int radius,
			int numberOfSlices) {
		List<Slice> slices = new ArrayList<Slice>();

		double sliceDegrees = OBSERVER_DEGREES / (double) numberOfSlices;

		for (int i = 0; i < numberOfSlices; i++) {
			double firstRayDegree = i * sliceDegrees;
			GeoLocation firstBoundPoint = CoordinateSquareCalculator
					.getTargetCoordinate(center, firstRayDegree, radius);
			double secondRayDegree = (i + 1) * sliceDegrees;
			GeoLocation secondBoundPoint = CoordinateSquareCalculator
					.getTargetCoordinate(center, secondRayDegree, radius);
			Slice slice = new Slice(center, firstBoundPoint, secondBoundPoint);
			slices.add(slice);
		}

		return slices;
	}

	private Map<Slice, List<Building>> distributeBulidingsInSlices(
			List<Slice> slices, List<Building> buildings) {
		Map<Slice, List<Building>> map = new HashMap<Slice, List<Building>>();

		for (Building building : buildings) {
			Slice slice = findBuildingSlice(slices, building);
			if ((slice != null) && map.containsKey(slice)) {
				map.get(slice).add(building);
			} else if ((slice != null)) {
				List<Building> sliceBuildings = new ArrayList<Building>();
				sliceBuildings.add(building);
				map.put(slice, sliceBuildings);
			}
		}

		return map;
	}

	/**
	 * Finds slice, within whih building is located. Note, that this method may
	 * return {@code null}, as buildings are found on a square in Kulturarv,
	 * whereas in this context we deal with a circle. Therefore, buildings,
	 * lying in the square corners will be ommited.
	 * 
	 * @param slices
	 *            slices to search
	 * @param building
	 *            buildings to search
	 * @return slice which building belong to
	 */
	private Slice findBuildingSlice(List<Slice> slices, Building building) {
		for (Slice slice : slices) {
			if (slice.doesCoodinateLiesWithin(building.getGeoLocation())) {
				return slice;
			}
		}

		return null;
	}

	private List<Building> pickOneRepresentativePerSlice(
			Map<Slice, List<Building>> sliceBuildings,
			Map<Long, Integer> ratings) {
		List<Building> slicesRepresentatives = new ArrayList<Building>();

		for (Slice slice : sliceBuildings.keySet()) {
			if (!sliceBuildings.isEmpty()) {
				Building sliceRepresentative = pickSliceRepresentative(
						sliceBuildings.get(slice), ratings);
				slicesRepresentatives.add(sliceRepresentative);
			}
		}

		return slicesRepresentatives;
	}

	private Building pickSliceRepresentative(List<Building> buildings,
			Map<Long, Integer> ratings) {
		int bestMetRating = 999;
		List<Building> bestBuildings = new ArrayList<Building>();
		for (Building building : buildings) {
			if (ratings.containsKey(building.getId())
					&& (ratings.get(building.getId()) < bestMetRating)) {
				bestMetRating = ratings.get(building.getId());
				bestBuildings = new ArrayList<Building>();
				bestBuildings.add(building);
			} else if (ratings.containsKey(building.getId())
					&& (ratings.get(building.getId()) == bestMetRating)) {
				bestBuildings.add(building);
			}
		}

		// we sure that best buildings size is not empty
		int random = new Random().nextInt(bestBuildings.size());
		return bestBuildings.get(random);
	}
}