package dk.codeunited.kulturarv.kulturarvClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.codeunited.kulturarv.kulturarvClient.cache.BuildingsIdsRatingsCache;
import dk.codeunited.kulturarv.kulturarvClient.parsers.BuildingRatingXMLParser;
import dk.codeunited.kulturarv.kulturarvClient.soap.KulturarvClient;
import dk.codeunited.kulturarv.kulturarvClient.util.CoordinateSquareCalculator;
import dk.codeunited.kulturarv.kulturarvClient.util.CoordinatesProjectionsConverter;
import dk.codeunited.kulturarv.kulturarvClient.util.jhlabs.Point2D;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * @author Maksim Sorokin
 */
public class BuildingsIdsFetcher {

	/**
	 * When we get buildings ids from the Kulturarv service, some of those are
	 * filtered out, because they are "not good enough", for example with value
	 * less then 4 (default value threshold). Therefore, we are a bit more
	 * pragmatic here and trying to get more building ids then it is actally
	 * needed. Information is cached, so it can be reused afterwards.
	 */
	private double BUILDING_TO_GET_RATIO = 5;

	private BuildingsIdsRatingsCache buildingIdsRatingsCache = new BuildingsIdsRatingsCache();

	private KulturarvClient kulturarvClient;

	private KulturarvContext kulturarvContext;

	public BuildingsIdsFetcher(KulturarvClient kulturarvClient,
			KulturarvContext kulturarvContext) {
		this.kulturarvClient = kulturarvClient;
		this.kulturarvContext = kulturarvContext;
	}

	public List<Long> getBuildingsIds(GeoLocation center, int radius,
			int minValueToShow, int maxBuildings)
			throws KulturarvUnavailableException {
		LogBridge
				.debug(String
						.format("Getting buildings ids for %s in radius %d with %d min value to show and %d buildings",
								center.toString(), radius, minValueToShow,
								maxBuildings));
		List<Long> allBuildingsIds = getAllBuildingsIdsForGivenAreaFromKulturarv(
				center, radius);
		LogBridge.debug(String.format("%d buildings nearby",
				allBuildingsIds.size()));

		allBuildingsIds = removeInvalidOnes(allBuildingsIds);
		LogBridge.debug(String.format("%d possibly valid buildings nearby",
				allBuildingsIds.size()));

		if (isEnoughBuildingsIdsInCache(allBuildingsIds, minValueToShow,
				maxBuildings)) {
			LogBridge
					.debug(String
							.format("Enough buildings ids in cache for min value to show %d (total %d), so returning that",
									minValueToShow,
									getBuildingsIdsFromCache(allBuildingsIds,
											minValueToShow).size()));
			return getBuildingsIdsFromCache(allBuildingsIds, minValueToShow,
					maxBuildings);
		}

		List<Long> filteredBuildingsIdsFromCache = getBuildingsIdsFromCache(
				allBuildingsIds, minValueToShow);
		List<Long> remainingFilteredBuildindingsRatings = getRemainingFilteredBuildingsRating(
				allBuildingsIds, filteredBuildingsIdsFromCache, minValueToShow,
				maxBuildings);

		List<Long> buildingsIds = new ArrayList<Long>();
		buildingsIds.addAll(filteredBuildingsIdsFromCache);
		buildingsIds.addAll(remainingFilteredBuildindingsRatings);

		if (buildingsIds.size() > maxBuildings) {
			return buildingsIds.subList(0, maxBuildings);
		}
		return buildingsIds;
	}

	/**
	 * Gets ratings for specified buildings ids.
	 * 
	 * @param buildingsIds
	 *            buidings ids to find rating for
	 * @return ratings for specified buildings ids
	 */
	public Map<Long, Integer> getRatings(List<Long> buildingsIds) {
		// if we know the rating, it has to be in cache
		return buildingIdsRatingsCache.getCached(buildingsIds);
	}

	private List<Long> removeInvalidOnes(List<Long> buildingsIds) {
		List<Long> valid = new ArrayList<Long>(buildingsIds);

		valid.removeAll(kulturarvContext.getInvalidBuildingsIds());
		LogBridge.debug(String.format("Removed %d invalid buildings out of %d",
				buildingsIds.size() - valid.size(), buildingsIds.size()));

		return valid;
	}

	private List<Long> getRemainingFilteredBuildingsRating(
			List<Long> allBuildingsIds, List<Long> buildingsIdsFromCache,
			int minValueToShow, int maxBuildings)
			throws KulturarvUnavailableException {
		List<Long> allNonCachedBuildingsIds = getAllNonCachedBuildingsIds(allBuildingsIds);
		if (allNonCachedBuildingsIds.isEmpty()) {
			return new ArrayList<Long>();
		}

		int missingAmount = maxBuildings - buildingsIdsFromCache.size();
		if (missingAmount <= 0) { // should never happen
			return new ArrayList<Long>();
		}
		LogBridge.debug(String.format("Missing %d buildings", missingAmount));
		Map<Long, Integer> remainingBuildingsRatings = getRemainingBuildingsRatings(
				allNonCachedBuildingsIds, missingAmount);
		List<Long> filteredRemainingBuildingsIds = filterIdsByMinValue(
				remainingBuildingsRatings, minValueToShow);
		return filteredRemainingBuildingsIds;
	}

	private List<Long> getAllBuildingsIdsForGivenAreaFromKulturarv(
			GeoLocation center, int radius)
			throws KulturarvUnavailableException {
		GeoLocation topLeftLocation = CoordinateSquareCalculator.getTopLeft(
				center, radius);
		GeoLocation bottomRightLocation = CoordinateSquareCalculator
				.getBottomRight(center, radius);

		Point2D.Double minEPSG25832 = CoordinatesProjectionsConverter
				.fromGoogleMapsToEPSG25832(topLeftLocation);
		Point2D.Double maxEPSG25832 = CoordinatesProjectionsConverter
				.fromGoogleMapsToEPSG25832(bottomRightLocation);

		Exception lastException = null;
		for (int i = 0; i < KulturarvService.KULTURARV_RETRY_ATTEMPTS; i++) {
			LogBridge
					.debug(String
							.format("Attempt %d to get all buildings ids for given area",
									i + 1));
			try {
				return kulturarvClient.getBuildingsIds(minEPSG25832,
						maxEPSG25832);
			} catch (Exception e) {
				LogBridge
						.warning("Failed getting all buildings ids for given area: "
								+ e.getMessage());
				lastException = e;
			}
		}
		throw new KulturarvUnavailableException(lastException);
	}

	private List<Long> getBuildingsIdsFromCache(List<Long> allBuildingsIds,
			int minValueToShow) {
		Map<Long, Integer> ratingsFromCache = buildingIdsRatingsCache
				.getCached(allBuildingsIds);

		List<Long> filtered = filterIdsByMinValue(ratingsFromCache,
				minValueToShow);
		return filtered;
	}

	private List<Long> getBuildingsIdsFromCache(List<Long> allBuildingsIds,
			int minValueToShow, int maxBuildings) {
		List<Long> buildingsIdsFromCache = getBuildingsIdsFromCache(
				allBuildingsIds, minValueToShow);

		if (buildingsIdsFromCache.size() >= maxBuildings) {
			return buildingsIdsFromCache.subList(0, maxBuildings);
		}

		return buildingsIdsFromCache;
	}

	private boolean isEnoughBuildingsIdsInCache(List<Long> allBuildingsIds,
			int minValueToShow, int maxBuildings) {
		List<Long> buildingsIdsFromCache = getBuildingsIdsFromCache(
				allBuildingsIds, minValueToShow);
		return buildingsIdsFromCache.size() >= maxBuildings;
	}

	private List<Long> getAllNonCachedBuildingsIds(List<Long> allBuildingsIds) {
		List<Long> ids = new ArrayList<Long>(allBuildingsIds);
		ids.removeAll(buildingIdsRatingsCache.getCachedKeys());
		return ids;
	}

	private Map<Long, Integer> getRemainingBuildingsRatings(
			List<Long> allNonCachedBuildingsIds, int missingAmount)
			throws KulturarvUnavailableException {
		int buildingsToGet = (int) (missingAmount * BUILDING_TO_GET_RATIO);
		List<Long> buildingsIdsToAskFor = new ArrayList<Long>(
				allNonCachedBuildingsIds);
		if (buildingsIdsToAskFor.size() > buildingsToGet) {
			Collections.shuffle(buildingsIdsToAskFor);
			buildingsIdsToAskFor = buildingsIdsToAskFor.subList(0,
					buildingsToGet);
		}
		LogBridge.debug(String.format(
				"Will attempt to get %d buildings ratings",
				buildingsIdsToAskFor.size()));

		Map<Long, Integer> remainingRatings = new HashMap<Long, Integer>();
		List<String> simpleBuildingsXmls = attemptToGetSimpleBuildingsXmls(buildingsIdsToAskFor);
		for (String simpleBuildingXml : simpleBuildingsXmls) {
			Map.Entry<Long, Integer> idRating = BuildingRatingXMLParser
					.parse(simpleBuildingXml);
			if (idRating != null) {
				remainingRatings.put(idRating.getKey(), idRating.getValue());
				buildingIdsRatingsCache.add(idRating.getKey(),
						idRating.getValue());
			}
		}
		addInvalidBuildingsIdsToTheContext(buildingsIdsToAskFor,
				remainingRatings.keySet());
		return remainingRatings;
	}

	private List<String> attemptToGetSimpleBuildingsXmls(
			List<Long> buildingsIdsToAskFor)
			throws KulturarvUnavailableException {
		Exception lastException = null;
		for (int i = 0; i < KulturarvService.KULTURARV_RETRY_ATTEMPTS; i++) {
			LogBridge.debug(String.format(
					"Attempt %d to get simple buildings xmls for %d ids",
					i + 1, buildingsIdsToAskFor.size()));
			try {
				return kulturarvClient
						.getSimpleBuildingXmls(buildingsIdsToAskFor);
			} catch (Exception e) {
				LogBridge.warning("Failed getting simple buildings xmls"
						+ e.getMessage());
				lastException = e;
			}
		}
		throw new KulturarvUnavailableException(lastException);
	}

	private void addInvalidBuildingsIdsToTheContext(Collection<Long> idsToAsk,
			Collection<Long> idsResolved) {
		List<Long> invalidIds = new ArrayList<Long>(idsToAsk);
		invalidIds.removeAll(idsResolved);
		LogBridge.debug(String.format("Marking %d buildings as invalid",
				invalidIds.size()));
		kulturarvContext.addInvalidBuildingsIds(invalidIds);
	}

	private Map<Long, Integer> filterRatingsByMinValue(
			Map<Long, Integer> ratings, int minValueToShow) {
		Map<Long, Integer> filteredRatings = new HashMap<Long, Integer>();
		for (Map.Entry<Long, Integer> rating : ratings.entrySet()) {
			if ((rating.getValue() > 0)
					&& (rating.getValue() <= minValueToShow)) {
				filteredRatings.put(rating.getKey(), rating.getValue());
			}
		}
		return filteredRatings;
	}

	private List<Long> filterIdsByMinValue(Map<Long, Integer> ratings,
			int minValueToShow) {
		return new ArrayList<Long>(filterRatingsByMinValue(ratings,
				minValueToShow).keySet());
	}
}