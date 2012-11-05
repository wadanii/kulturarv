package dk.codeunited.kulturarv.kulturarvClient.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

/**
 * Basic abstraction over a hashmap.
 * 
 * @author Maksim Sorokin
 * 
 * @param <K>
 *            key type
 * @param <V>
 *            value type
 */
public class AbstractCache<K, V> {

	@SuppressLint("UseSparseArrays")
	private Map<K, V> cache = new HashMap<K, V>();

	public void add(K key, V value) {
		if ((key != null) && (value != null)) {
			cache.put(key, value);
		}
	}

	/**
	 * Add objects to the cache.
	 * 
	 * @param objects
	 *            objects to cache
	 */
	public void add(Map<K, V> objects) {
		for (Map.Entry<K, V> objectEntry : objects.entrySet()) {
			add(objectEntry.getKey(), objectEntry.getValue());
		}
	}

	/**
	 * Will return cached objects for given keys. If no key is found, no object
	 * is added to the return.
	 * 
	 * @param keys
	 *            keys to search in the cache
	 * @return cached objects for the given known keys
	 */
	public List<V> getCachedValues(List<K> keys) {
		List<V> values = new ArrayList<V>();

		for (K key : keys) {
			if (cache.containsKey(key)) {
				values.add(cache.get(key));
			}
		}

		return values;
	}

	/**
	 * Will return cache map for given keys. If no key is found, no object is
	 * added to the return.
	 * 
	 * @param keys
	 *            keys to search in the cache
	 * @return cached objects for the given known keys
	 */
	public Map<K, V> getCached(List<K> keys) {
		Map<K, V> toReturn = new HashMap<K, V>();

		for (K key : keys) {
			if (cache.containsKey(key)) {
				toReturn.put(key, cache.get(key));
			}
		}

		return toReturn;
	}

	/**
	 * Returns a list of all cached keys.
	 * 
	 * @return a list of all cached keys
	 */
	public Collection<K> getCachedKeys() {
		return cache.keySet();
	}
}