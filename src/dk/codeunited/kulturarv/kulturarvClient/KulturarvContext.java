package dk.codeunited.kulturarv.kulturarvClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Context that is used in the kulturarv service to share information between
 * the fetchers.
 * 
 * @author Maksim Sorokin
 */
public class KulturarvContext {

	/**
	 * Invalid building id indicates that we cannot get/search/query information
	 * for a building with this id. There can be multiple reasons for that, for
	 * example kulturarv service does not return valid information for this
	 * particular building.
	 * 
	 * <br />
	 * At the moment, we treat all buildings without name or architectural
	 * evaluation as invalid buildings, though we get valid information out from
	 * Kulturarv service.
	 * 
	 * <p>
	 * By knowing invalidIds, we can eliminate possible duplicated requests to
	 * the Kulturarv service.
	 */
	private List<Long> invalidBuildingsIds = new ArrayList<Long>();

	public void addInvalidBuildingsIds(List<Long> ids) {
		invalidBuildingsIds.addAll(ids);
	}

	public List<Long> getInvalidBuildingsIds() {
		return new ArrayList<Long>(invalidBuildingsIds);
	}
}