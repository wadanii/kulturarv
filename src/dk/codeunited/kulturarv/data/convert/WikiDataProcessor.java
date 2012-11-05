package dk.codeunited.kulturarv.data.convert;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;

import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.marker.POIMarker;

/**
 * A data processor for wikipedia urls or data, Responsible for converting raw
 * data (to json and then) to marker data.
 * 
 * @author A. Egal
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class WikiDataProcessor implements DataProcessor {

	private static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		String[] str = { "wiki" };
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = { "wiki" };
		return str;
	}

	@Override
	public boolean matchesRequiredType(String type) {
		if (type.equals(DataSource.DataSourceType.WIKIPEDIA.name())) {
			return true;
		}
		return false;
	}

	@Override
	public List<Marker> load(String rawData, int taskId, int colour,
			DataSource dataSource) throws JSONException {
		List<Marker> markers = new ArrayList<Marker>();
		JSONObject root = convertToJSON(rawData);
		JSONArray dataArray = root.getJSONArray("geonames");
		int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			JSONObject jo = dataArray.getJSONObject(i);

			Marker ma = null;
			if (jo.has("title") && jo.has("lat") && jo.has("lng")
					&& jo.has("elevation") && jo.has("wikipediaUrl")) {

				LogBridge.debug("processing Wikipedia JSON object");

				// no unique ID is provided by the web service according to
				// http://www.geonames.org/export/wikipedia-webservice.html
				ma = new POIMarker("", HtmlUnescape.unescapeHTML(
						jo.getString("title"), 0), jo.getDouble("lat"),
						jo.getDouble("lng"), jo.getDouble("elevation"),
						"http://" + jo.getString("wikipediaUrl"), taskId,
						colour, dataSource);
				markers.add(ma);
			}
		}
		return markers;
	}

	private JSONObject convertToJSON(String rawData) {
		try {
			return new JSONObject(rawData);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}
}