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
 * A data processor for custom urls or data, Responsible for converting raw data
 * (to json and then) to marker data.
 * 
 * @author A. Egal
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class MixareDataProcessor implements DataProcessor {

	private static final int MAX_JSON_OBJECTS = 1000;

	@Override
	public String[] getUrlMatch() {
		String[] str = new String[0]; // only use this data source if all the
										// others
										// don't match
		return str;
	}

	@Override
	public String[] getDataMatch() {
		String[] str = new String[0]; // only use this data source if all the
										// others
										// don't match
		return str;
	}

	@Override
	public boolean matchesRequiredType(String type) {
		return true; // this datasources has no required type, it will always
						// match.
	}

	@Override
	public List<Marker> load(String rawData, int taskId, int colour,
			DataSource dataSource) throws JSONException {
		List<Marker> markers = new ArrayList<Marker>();
		JSONObject root = convertToJSON(rawData);
		JSONArray dataArray = root.getJSONArray("results");
		int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

		for (int i = 0; i < top; i++) {
			JSONObject jo = dataArray.getJSONObject(i);

			Marker ma = null;
			if (jo.has("title") && jo.has("lat") && jo.has("lng")
					&& jo.has("elevation")) {

				String id = "";
				if (jo.has("id"))
					id = jo.getString("id");

				LogBridge.debug("Processing Mixare JSON object");
				String link = null;

				if (jo.has("has_detail_page")
						&& jo.getInt("has_detail_page") != 0
						&& jo.has("webpage"))
					link = jo.getString("webpage");

				ma = new POIMarker(id, HtmlUnescape.unescapeHTML(
						jo.getString("title"), 0), jo.getDouble("lat"),
						jo.getDouble("lng"), jo.getDouble("elevation"), link,
						taskId, colour, dataSource);
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