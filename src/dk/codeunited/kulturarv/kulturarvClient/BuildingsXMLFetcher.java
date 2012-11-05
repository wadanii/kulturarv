package dk.codeunited.kulturarv.kulturarvClient;

import java.util.ArrayList;
import java.util.List;

import dk.codeunited.kulturarv.kulturarvClient.cache.BuildingsCache;
import dk.codeunited.kulturarv.kulturarvClient.parsers.BuildingXMLParser;
import dk.codeunited.kulturarv.kulturarvClient.soap.KulturarvClient;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.model.Building;

/**
 * @author Maksim Sorokin
 */
public class BuildingsXMLFetcher {

	private final static int MAX_BUILDING_IDS_PORTIONS_TO_QUERY = 25;

	private BuildingsCache buildingsCache = new BuildingsCache();

	private KulturarvClient kulturarvClient;

	private KulturarvContext kulturarvContext;

	public BuildingsXMLFetcher(KulturarvClient kulturarvClient,
			KulturarvContext kulturarvContext) {
		this.kulturarvClient = kulturarvClient;
		this.kulturarvContext = kulturarvContext;
	}

	public List<Building> getBuildingsForArea(List<Long> buildingsIds)
			throws KulturarvUnavailableException {
		LogBridge.debug(String.format(
				"Getting buildings information for %d buildings",
				buildingsIds.size()));
		List<Building> buildingsForArea = new ArrayList<Building>();

		List<Building> buildingsFromCache = buildingsCache
				.getCachedValues(buildingsIds);
		buildingsForArea.addAll(buildingsFromCache);
		LogBridge.debug(String.format("Found %d buildings in cache",
				buildingsFromCache.size()));

		buildingsIds.removeAll(buildingsCache.getCachedKeys());

		List<Building> buildingsFromKulturarv = getBuildingsXmlsFromKulturavAndParse(
				buildingsIds, true);
		buildingsForArea.addAll(buildingsFromKulturarv);
		buildingsCache.add(buildingsFromKulturarv);
		addInvalidBuildingsIdsToTheContext(buildingsIds, buildingsFromKulturarv);

		return buildingsForArea;
	}

	private List<Building> getBuildingsXmlsFromKulturavAndParse(
			List<Long> buildingsIds, boolean treatUninterestingAsInvalid)
			throws KulturarvUnavailableException {
		List<Building> buildings = new ArrayList<Building>();

		if (buildingsIds.size() > 0) {
			for (List<Long> buildingsIdsPortion : splitBuildingsIdsIntoPortions(
					buildingsIds, MAX_BUILDING_IDS_PORTIONS_TO_QUERY)) {
				LogBridge
						.debug(String
								.format("Attempting to get full information for %d buildings portion",
										buildingsIdsPortion.size()));
				try {
					List<String> buildingsXmls = attemptToGetFullBuildingsXmls(buildingsIdsPortion);
					for (String buildingXml : buildingsXmls) {
						Building building = BuildingXMLParser
								.parse(buildingXml);
						if ((building != null)
								&& (building.isInteresting() || !treatUninterestingAsInvalid)) {
							buildings.add(building);
						}
					}
				} catch (Exception e) {
					// one request may fail, but we want to try to continue with
					// the others
					LogBridge
							.warning("Error getting buildings xmls for buildings ids portion: "
									+ e.getMessage());
				}
			}
		}

		return buildings;
	}

	private List<String> attemptToGetFullBuildingsXmls(
			List<Long> buildingsIdsPortion)
			throws KulturarvUnavailableException {
		Exception lastException = null;
		for (int i = 0; i < KulturarvService.KULTURARV_RETRY_ATTEMPTS; i++) {
			LogBridge
					.debug(String
							.format("Attempt %d to get full xmls for building portion with %d buildings",
									i + 1, buildingsIdsPortion.size()));
			try {
				return kulturarvClient
						.getFullBuildingsXmls(buildingsIdsPortion);
			} catch (Exception e) {
				LogBridge
						.warning("Failed getting full buildings xmls for a give ids portion"
								+ e.getMessage());
				lastException = e;
			}
		}
		throw new KulturarvUnavailableException(lastException);
	}

	private List<List<Long>> splitBuildingsIdsIntoPortions(
			List<Long> buildingsIds, int portionSize) {
		List<List<Long>> portions = new ArrayList<List<Long>>();

		if ((buildingsIds.size() > 0) && (buildingsIds.size() < portionSize)) {
			portions.add(buildingsIds);
		} else if (buildingsIds.size() > 0) {
			portions.add(buildingsIds.subList(0, portionSize));
			if (buildingsIds.size() > portionSize) {
				portions.addAll(splitBuildingsIdsIntoPortions(
						buildingsIds.subList(portionSize, buildingsIds.size()),
						portionSize));
			}
		}

		return portions;
	}

	/**
	 * If building id was asked, but is not among the {@code parsedBuildings},
	 * it is considered invalid.
	 * 
	 * @param buildingsIdsToAsk
	 *            buildings ids that were queried
	 * @param parsedBuildings
	 *            actual parsed buildings
	 */
	private void addInvalidBuildingsIdsToTheContext(
			List<Long> buildingsIdsToAsk, List<Building> parsedBuildings) {
		int originalToAskAmount = buildingsIdsToAsk.size();
		List<Long> invalidBuildingsIds = new ArrayList<Long>(buildingsIdsToAsk);
		for (Building building : parsedBuildings) {
			if (invalidBuildingsIds.contains(building.getId())) {
				invalidBuildingsIds.remove(building.getId());
			}
		}
		LogBridge.debug(String.format("Marking %d out of %d ids as invalid",
				invalidBuildingsIds.size(), originalToAskAmount));
		kulturarvContext.addInvalidBuildingsIds(invalidBuildingsIds);
	}
}