package dk.codeunited.kulturarv.instagram;

/**
 * @author Maksim Sorokin
 */
public class InstagramPhoto {

	private String id;

	private double latitude;

	private double longitude;

	private String name;

	private String thumbnailUrl;

	private String standardResolutionUrl;

	public InstagramPhoto(String id, double latitude, double longitude,
			String name, String thumbnailUrl, String standardResolutionUrl) {
		this.id = id;
		this.latitude = latitude;
		this.longitude = longitude;
		this.name = name;
		this.thumbnailUrl = thumbnailUrl;
		this.standardResolutionUrl = standardResolutionUrl;
	}

	public String getId() {
		return id;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getName() {
		return name;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public String getStandardResolutionUrl() {
		return standardResolutionUrl;
	}
}