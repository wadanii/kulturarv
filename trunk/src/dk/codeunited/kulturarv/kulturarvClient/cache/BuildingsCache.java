package dk.codeunited.kulturarv.kulturarvClient.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.codeunited.kulturarv.model.Building;

/**
 * Cache for already acquired buildings. Building information is nearly static,
 * therefore it can be safely cached in the current application run. This cache
 * is intended to be used only in one application launch.
 * 
 * <p>
 * The way Kulturarv service works is that first ids of nearby (in specified
 * radius) buildings are acquired and then further information is acquired using
 * those ids. Therefore, we can cache hashmap of ids and the actual buildings.
 * 
 * @author Maksim Sorokin
 */
public class BuildingsCache extends AbstractCache<Long, Building> {

	/**
	 * Add buildings to cache.
	 * 
	 * @param buildingsFromKulturarv
	 *            buildings to cache
	 */
	public void add(List<Building> buildingsFromKulturarv) {
		Map<Long, Building> map = new HashMap<Long, Building>();
		for (Building building : buildingsFromKulturarv) {
			map.put(building.getId(), building);
		}
		super.add(map);
	}
}