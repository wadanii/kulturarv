package dk.codeunited.kulturarv.flickr;

import javax.xml.parsers.ParserConfigurationException;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.REST;

import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;

/**
 * A wrapper class for Flickr API.
 * 
 * @author Kostas Rutkauskas
 */
public final class FlickrHelper {

	private static FlickrHelper instance = null;
	private static final String API_KEY = KulturarvApplication.getAppContext()
			.getString(R.string.flickr_api_key);
	private static final String API_SEC = KulturarvApplication.getAppContext()
			.getString(R.string.flickr_api_secret);

	private FlickrHelper() {

	}

	public static FlickrHelper getInstance() {
		if (instance == null) {
			instance = new FlickrHelper();
		}

		return instance;
	}

	public Flickr getFlickr() {
		try {
			Flickr f = new Flickr(API_KEY, API_SEC, new REST());
			return f;
		} catch (ParserConfigurationException e) {
			return null;
		}
	}
}