package dk.codeunited.kulturarv.instagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * Provides nearby photos information from Instagram.
 * 
 * @author Maksim Sorokin
 */
public class InstagramPhotosProvider {

	private final static String INSTAGRAM_MEDIA_SEARCH_URL = "https://api.instagram.com/v1/media/search";

	/**
	 * Get instagram photos for the provided location.
	 * 
	 * @param center
	 *            center location
	 * @param radius
	 *            radius to get search instagram photos for
	 * @return list of instagram photos for the provided area
	 * @throws Exception
	 *             exception that may be thrown during accessing the information
	 *             from Instagram
	 */
	public List<InstagramPhoto> getInstagramPhotosForArea(GeoLocation center,
			int radius) throws Exception {
		List<InstagramPhoto> instagramPhotos = new ArrayList<InstagramPhoto>();

		String mediaSearchUrl = buildMediaSearchUrl(center, radius);
		String mediaSearchResponse = makeGetRequest(mediaSearchUrl);

		JSONObject mediaJSON = new JSONObject(mediaSearchResponse);

		JSONArray mediaJSONArray = mediaJSON.getJSONArray("data");
		for (int i = 0; i < mediaJSONArray.length(); i++) {
			try {
				JSONObject mediaItemJSON = mediaJSONArray.getJSONObject(i);

				String id = mediaItemJSON.getString("id");

				JSONObject locationJSON = mediaItemJSON
						.getJSONObject("location");
				double latitude = locationJSON.getDouble("latitude");
				double longitude = locationJSON.getDouble("longitude");
				String name = null;
				try {
					name = locationJSON.getString("name");
				} catch (JSONException jsone) {
					// name might not exist, so this is ok.
				}

				JSONObject imagesJSON = mediaItemJSON.getJSONObject("images");
				JSONObject thumbnailJSON = imagesJSON
						.getJSONObject("thumbnail");
				String thumbnailUrl = thumbnailJSON.getString("url");
				JSONObject standardResolutionJSON = imagesJSON
						.getJSONObject("standard_resolution");
				String standardResolutionUrl = standardResolutionJSON
						.getString("url");

				InstagramPhoto instagramPhoto = new InstagramPhoto(id,
						latitude, longitude, name, thumbnailUrl,
						standardResolutionUrl);
				instagramPhotos.add(instagramPhoto);
			} catch (Exception e) {
				// we do not want to crash if we cannot parse a particular item
				LogBridge.error("Failed parsing Instagram location", e);
			}
		}

		return instagramPhotos;
	}

	private String buildMediaSearchUrl(GeoLocation center, int radius) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.appendQueryParameter("lat", center.getLatitudeInDegrees()
				+ "");
		uriBuilder.appendQueryParameter("lng", center.getLongitudeInDegrees()
				+ "");
		uriBuilder.appendQueryParameter("distance", radius + "");
		uriBuilder.appendQueryParameter("client_id", KulturarvApplication
				.getAppContext().getString(R.string.instagram_client_id));
		Uri uri = uriBuilder.build();

		return INSTAGRAM_MEDIA_SEARCH_URL + uri.toString();
	}

	private String makeGetRequest(String url) throws Exception {
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet request = new HttpGet();
		request.setURI(new URI(url));

		HttpResponse httpResponse = httpClient.execute(request);
		HttpEntity entity = httpResponse.getEntity();
		InputStream is = entity.getContent();

		String response = inputStreamToString(is);

		try {
			is.close();
		} catch (Exception e) {
			// swallow on purpose
		}

		return response;
	}

	private String inputStreamToString(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder total = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			total.append(line);
		}

		return total.toString();
	}
}