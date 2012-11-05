package dk.codeunited.kulturarv.flickr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mixare.lib.marker.Marker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.googlecode.flickrjandroid.Flickr;
import com.googlecode.flickrjandroid.photos.GeoData;
import com.googlecode.flickrjandroid.photos.Photo;
import com.googlecode.flickrjandroid.photos.PhotoList;
import com.googlecode.flickrjandroid.photos.SearchParameters;
import com.googlecode.flickrjandroid.photos.Size;

import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.marker.ImageMarker;
import dk.codeunited.kulturarv.model.GeoLocation;
import dk.codeunited.kulturarv.util.AltitudeAdjuster;

/**
 * Provides markers with Flickr images for specific location.
 * 
 * @author Kostas Rutkauskas
 */
public class FlickrMarkersProvider {

	public static List<Marker> getMarkers(GeoLocation geoLocation,
			double radius, DataSource dataSource) {
		LogBridge
				.debug("FlickrMarkersProvider.getMarkers() started for location "
						+ geoLocation.toString() + " with radius " + radius);

		List<Marker> markers = new ArrayList<Marker>();
		try {
			Flickr f = FlickrHelper.getInstance().getFlickr();

			GeoLocation[] boundingBox = geoLocation.boundingCoordinates(radius,
					GeoLocation.EARTH_RADIUS);

			double minLongitude = boundingBox[0].getLongitudeInDegrees() < boundingBox[1]
					.getLongitudeInDegrees() ? boundingBox[0]
					.getLongitudeInDegrees() : boundingBox[1]
					.getLongitudeInDegrees();
			double minLatitude = boundingBox[0].getLatitudeInDegrees() < boundingBox[1]
					.getLatitudeInDegrees() ? boundingBox[0]
					.getLatitudeInDegrees() : boundingBox[1]
					.getLatitudeInDegrees();
			double maxLongitude = boundingBox[0].getLongitudeInDegrees() > boundingBox[1]
					.getLongitudeInDegrees() ? boundingBox[0]
					.getLongitudeInDegrees() : boundingBox[1]
					.getLongitudeInDegrees();
			double maxLatitude = boundingBox[0].getLatitudeInDegrees() > boundingBox[1]
					.getLatitudeInDegrees() ? boundingBox[0]
					.getLatitudeInDegrees() : boundingBox[1]
					.getLatitudeInDegrees();

			SearchParameters params = new SearchParameters();
			params.setBBox(Double.toString(minLongitude),
					Double.toString(minLatitude),
					Double.toString(maxLongitude), Double.toString(maxLatitude));

			params.setMaxUploadDate(new Date());

			PhotoList photos = f.getPhotosInterface().search(params,
					dataSource.getMaxObjects(), 1);
			LogBridge.debug("Flickr: " + photos.size() + " photos");

			for (Photo photo : photos) {
				GeoData photoLocation = f.getGeoInterface().getLocation(
						photo.getId());

				double altitude = AltitudeAdjuster.getAltitude(geoLocation,
						photoLocation.getLatitude(),
						photoLocation.getLongitude());

				List<Size> sizes = new ArrayList<Size>(f.getPhotosInterface()
						.getSizes(photo.getId()));

				Bitmap image = BitmapFactory.decodeStream(f
						.getPhotosInterface().getImageAsStream(photo,
								sizes.size() > 1 ? 2 : 1));

				if (image != null) {
					markers.add(new ImageMarker(photo.getId(),
							photo.getTitle() == null ? "Flickr" : photo
									.getTitle(), photoLocation.getLatitude(),
							photoLocation.getLongitude(), altitude, photo
									.getUrl(), 0, dataSource.getColor(), image,
							dataSource));
				}

				if (markers.size() >= dataSource.getMaxObjects()) {
					break;
				}
			}
		} catch (Exception e) {
			LogBridge.error("Flickr markers provider failed", e);
		}

		return markers;
	}
}