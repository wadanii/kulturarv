package dk.codeunited.kulturarv.model;

/**
 * @author Kostas Rutkauskas
 */
public class Building {

	private long id;

	private GeoLocation geoLocation;

	private String name;

	private boolean interesting;

	public Building(long id, GeoLocation geoLocation, String name,
			boolean interesting) {
		this.id = id;
		this.geoLocation = geoLocation;
		this.name = name;
		this.interesting = interesting;
	}

	public long getId() {
		return id;
	}

	public GeoLocation getGeoLocation() {
		return geoLocation;
	}

	public String getName() {
		return name;
	}

	public boolean isInteresting() {
		return interesting;
	}

	@Override
	public String toString() {
		return String.format("[%d, %s, %s, %s, %b]", id,
				geoLocation.getLatitudeInDegrees() + "",
				geoLocation.getLongitudeInDegrees() + "", name, interesting);
	}
}