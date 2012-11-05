package dk.codeunited.kulturarv.kulturarvClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.codeunited.kulturarv.kulturarvClient.filtering.AdaptiveFilter;
import dk.codeunited.kulturarv.kulturarvClient.soap.KulturarvClient;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.model.Building;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * This service should be used in order to acquire the information about the
 * buildings. No additional caching should be used as this service already takes
 * cares of it.
 * 
 * @author Maksim Sorokin
 */
public class KulturarvService {

	/**
	 * Kulturarv soap service frequently fails on the requests. Out strategy is
	 * simply to retry a couple of times before giving up.
	 */
	final static int KULTURARV_RETRY_ATTEMPTS = 3;

	private KulturarvClient kulturarvClient = new KulturarvClient();

	private KulturarvContext kulturarvContext = new KulturarvContext();

	private AdaptiveFilter adaptiveFilter = new AdaptiveFilter();

	private BuildingsIdsFetcher buildingsIdsFetcher = new BuildingsIdsFetcher(
			kulturarvClient, kulturarvContext);
	private BuildingsXMLFetcher buildingsXmlFetcher = new BuildingsXMLFetcher(
			kulturarvClient, kulturarvContext);

	/**
	 * Resolve nearby buildings by the center coordinate and provided radius.
	 * 
	 * @param center
	 *            center coordinate
	 * @param radius
	 *            radius in meters from the coordinate, where the buildings
	 *            should be looked up
	 * @param minValueToShow
	 *            minValueToShow filters out all the buildings that have
	 *            evaluation less than specified
	 * @param adaptiveFiltering
	 *            adaptive filtering is applied after applying
	 *            {@code minValueToShow}. When enabled, attempt is made group
	 *            several buildings into, based on their location.
	 * @param maxBuildings
	 *            max buildings to return. The service does not necessarily
	 *            return max buildings, even though there are plenty of known
	 *            buildings nearby.
	 * @return list of nearby buildings based on the center coordinates and
	 *         provided radius
	 * @throws KulturarvUnavailableException
	 *             exception that may occur while using Kulturarv SOAP service
	 */
	public List<Building> getBuildingsForArea(GeoLocation center, int radius,
			int minValueToShow, boolean adaptiveFiltering, int maxBuildings)
			throws KulturarvUnavailableException {
		List<Long> buildingsIds = buildingsIdsFetcher.getBuildingsIds(center,
				radius, minValueToShow, maxBuildings);

		List<Building> buildingsForArea = buildingsXmlFetcher
				.getBuildingsForArea(buildingsIds);

		if (adaptiveFiltering) {
			buildingsForArea = applyAdaptiveFiltering(center, radius,
					buildingsForArea);
		}

		return buildingsForArea;
	}

	private List<Building> applyAdaptiveFiltering(GeoLocation center,
			int radius, List<Building> buildings) {
		LogBridge.debug("Applying adaptive filtering");

		List<Long> buildingsIds = new ArrayList<Long>();
		for (Building building : buildings) {
			buildingsIds.add(building.getId());
		}

		Map<Long, Integer> ratings = buildingsIdsFetcher
				.getRatings(buildingsIds);
		return adaptiveFilter.filter(center, radius, buildings, ratings);
	}
}