package dk.codeunited.kulturarv.mgr.downloader;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.mixare.lib.marker.Marker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.data.DataSource.DataSourceType;
import dk.codeunited.kulturarv.data.convert.DataConvertor;
import dk.codeunited.kulturarv.flickr.FlickrMarkersProvider;
import dk.codeunited.kulturarv.instagram.InstagramPhoto;
import dk.codeunited.kulturarv.instagram.InstagramPhotosProvider;
import dk.codeunited.kulturarv.kulturarvClient.KulturarvService;
import dk.codeunited.kulturarv.kulturarvClient.KulturarvUnavailableException;
import dk.codeunited.kulturarv.marker.ImageMarker;
import dk.codeunited.kulturarv.marker.LocalMarker;
import dk.codeunited.kulturarv.marker.POIMarker;
import dk.codeunited.kulturarv.mgr.HttpTools;
import dk.codeunited.kulturarv.model.Building;
import dk.codeunited.kulturarv.model.GeoLocation;
import dk.codeunited.kulturarv.util.AltitudeAdjuster;

/**
 * @author Kostas Rutkauskas
 * @author Maksim Sorokin
 */
public class DownloadRequestProcessor {

	private static KulturarvService kulturarvService = new KulturarvService();

	private static InstagramPhotosProvider instagramPhotosProvider = new InstagramPhotosProvider();

	public static List<Marker> processDownloadRequest(Context ctx,
			GeoLocationBasedDownloadRequest downloadRequest) throws Exception {
		List<Marker> markers = null;

		if (downloadRequest.getSource().getType() == DataSourceType.KULTURARV) {
			markers = processKulturStyrelsenRequest(ctx, downloadRequest);
		} else if (downloadRequest.getSource().getType() == DataSourceType.FLICKR) {
			markers = processFlickrRequest(downloadRequest);
		} else if (downloadRequest.getSource().getType() == DataSourceType.INSTAGRAM) {
			markers = processInstagramRequest(downloadRequest);
		} else {
			markers = processRequest(ctx, downloadRequest);
		}

		return markers;
	}

	private static List<Marker> processRequest(Context ctx,
			GeoLocationBasedDownloadRequest downloadRequest) throws Exception {
		if (downloadRequest == null) {
			throw new Exception("Request is null");
		}

		if (!downloadRequest.getSource().isWellFormed()) {
			throw new Exception("Datasource in not WellFormed");
		}

		String pageContent = HttpTools.getPageContent(downloadRequest,
				ctx.getContentResolver());

		List<Marker> markers = null;

		if (pageContent != null) {
			markers = DataConvertor.getInstance().load(
					downloadRequest.getSource().getUrl(), pageContent,
					downloadRequest.getSource());

			for (Marker marker : markers) {
				double altitude = AltitudeAdjuster.getAltitude(
						downloadRequest.getGeoLocation(), marker.getLatitude(),
						marker.getLongitude());
				LocalMarker locMarker = (LocalMarker) marker;
				locMarker.setAltitude(altitude);
			}
		}

		return markers;
	}

	private static List<Marker> processKulturStyrelsenRequest(Context ctx,
			GeoLocationBasedDownloadRequest downloadRequest)
			throws KulturarvUnavailableException {
		List<Marker> markers = new ArrayList<Marker>();

		GeoLocation currentLocation = downloadRequest.getGeoLocation();
		int radius = (int) (downloadRequest.getRadius() * 1000);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(KulturarvApplication
						.getAppContext());

		int minValueToShow = prefs.getInt(
				ctx.getString(R.string.pref_minimum_value), 4);
		boolean adaptiveFiltering = prefs.getBoolean(
				ctx.getString(R.string.pref_adaptive_filter), true);

		int maxObjects = downloadRequest.getSource().getMaxObjects();
		List<Building> buildings = kulturarvService.getBuildingsForArea(
				currentLocation, radius, minValueToShow, adaptiveFiltering,
				maxObjects);

		for (Building b : buildings) {
			double altitude = AltitudeAdjuster.getAltitude(currentLocation,
					b.getGeoLocation());
			markers.add(new POIMarker(
					b.getId() + "",
					b.getName(),
					b.getGeoLocation().getLatitudeInDegrees(),
					b.getGeoLocation().getLongitudeInDegrees(),
					altitude,
					String.format(
							"https://www.kulturarv.dk/fbb/bygningvis.pub?bygning=%d",
							b.getId()), 0, downloadRequest.getSource()
							.getColor(), downloadRequest.getSource()));
		}

		return markers;
	}

	private static List<Marker> processInstagramRequest(
			GeoLocationBasedDownloadRequest downloadRequest) throws Exception {
		List<Marker> markers = new ArrayList<Marker>();

		GeoLocation currentLocation = downloadRequest.getGeoLocation();
		int radius = (int) (downloadRequest.getRadius() * 1000);
		List<InstagramPhoto> instagramPhotos = instagramPhotosProvider
				.getInstagramPhotosForArea(currentLocation, radius);

		int maxObjects = downloadRequest.getSource().getMaxObjects();
		if (instagramPhotos.size() > maxObjects) {
			instagramPhotos = instagramPhotos.subList(0, maxObjects);
		}

		for (InstagramPhoto instagramPhoto : instagramPhotos) {
			double altitude = AltitudeAdjuster
					.getAltitude(currentLocation, instagramPhoto.getLatitude(),
							instagramPhoto.getLongitude());
			URL url = new URL(instagramPhoto.getThumbnailUrl());
			Bitmap thumbnail = BitmapFactory.decodeStream((InputStream) url
					.getContent());

			markers.add(new ImageMarker(instagramPhoto.getId(),
					instagramPhoto.getName() == null ? "Instagram"
							: instagramPhoto.getName(), instagramPhoto
							.getLatitude(), instagramPhoto.getLongitude(),
					altitude, instagramPhoto.getStandardResolutionUrl(), 0,
					downloadRequest.getSource().getColor(), thumbnail,
					downloadRequest.getSource()));
		}

		return markers;
	}

	private static List<Marker> processFlickrRequest(
			GeoLocationBasedDownloadRequest downloadRequest) throws Exception {
		return FlickrMarkersProvider.getMarkers(
				downloadRequest.getGeoLocation(), downloadRequest.getRadius(),
				downloadRequest.getSource());
	}
}