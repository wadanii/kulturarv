package dk.codeunited.kulturarv.mgr.downloader;

import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * @author Kostas Rutkauskas
 */
public class GeoLocationBasedDownloadRequest extends DownloadRequest {

	private GeoLocation geoLocation;

	private double radius;

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public void setGeoLocation(GeoLocation geoLocation) {
		this.geoLocation = geoLocation;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public GeoLocationBasedDownloadRequest(DataSource source,
			GeoLocation geoLocation, double radius) {
		super(source);
		this.setGeoLocation(geoLocation);
		this.radius = radius;
	}
}