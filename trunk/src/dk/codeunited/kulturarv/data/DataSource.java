package dk.codeunited.kulturarv.data;

import dk.codeunited.kulturarv.data.convert.DataConvertor;

/**
 * @author Kostas Rutkauskas
 */
public class DataSource {

	private int id;
	private String name;
	private String url;
	private int maxObjects;
	private int icon;
	private int color;
	private boolean canBeDisabled;
	private boolean maxObjectsEditable;

	public enum DataSourceType {
		WIKIPEDIA, BUZZ, TWITTER, OSM, MIXARE, ARENA, KULTURARV, FLICKR, INSTAGRAM
	}

	public enum DisplayMode {
		CIRCLE_MARKER, NAVIGATION_MARKER, IMAGE_MARKER
	}

	private DataSourceType type;
	private DisplayMode display;

	public DataSource() {

	}

	public DataSource(int id, String name, String url, DataSourceType type,
			DisplayMode display, int maxObjects, int icon, int color,
			boolean canBeDisabled, boolean maxObjectsEditable) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.type = type;
		this.display = display;
		this.maxObjects = maxObjects;
		this.icon = icon;
		this.color = color;
		this.canBeDisabled = canBeDisabled;
		this.setMaxObjectsEditable(maxObjectsEditable);
	}

	public String createRequestParams(double lat, double lon, double alt,
			float radius, String locale) {
		String ret = "";
		if (!ret.startsWith("file://")) {
			switch (this.type) {

			case WIKIPEDIA:
				float geoNamesRadius = radius > 20 ? 20 : radius; // Free
				// service
				// limited
				// to 20km
				ret += "?lat=" + lat + "&lng=" + lon + "&radius="
						+ geoNamesRadius + "&maxRows=50" + "&lang=" + locale
						+ "&username=mixare";
				break;

			case BUZZ:
				ret += "&lat=" + lat + "&lon=" + lon + "&radius=" + radius
						* 1000;
				break;

			case TWITTER:
				ret += "?geocode=" + lat + "%2C" + lon + "%2C"
						+ Math.min(radius, 1.0) + "km";
				break;

			case MIXARE:
				ret += "?latitude=" + Double.toString(lat) + "&longitude="
						+ Double.toString(lon) + "&altitude="
						+ Double.toString(alt) + "&radius="
						+ Double.toString(radius);
				break;

			case ARENA:
				ret += "&lat=" + Double.toString(lat) + "&lng="
						+ Double.toString(lon);
				break;

			case OSM:
				ret += DataConvertor.getOSMBoundingBox(lat, lon, radius);
				break;
			}

		}

		return ret;
	}

	public DisplayMode getDisplay() {
		return this.display;
	}

	public DataSourceType getType() {
		return this.type;
	}

	public boolean isEnabled() {
		return DataSourceStorage.isDataSourceVisible(this.id, true);
	}

	public String getName() {
		return this.name;
	}

	public String getUrl() {
		return this.url;
	}

	public void setEnabled(boolean isChecked) {
		DataSourceStorage.setDataSourceVisibile(this.id, isChecked);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMaxObjects() {
		return maxObjects;
	}

	public void setMaxObjects(int maxObjects) {
		this.maxObjects = maxObjects;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public boolean getCanBeDisabled() {
		return canBeDisabled;
	}

	public void setCanBeDisabled(boolean canBeDisabled) {
		this.canBeDisabled = canBeDisabled;
	}

	public boolean isMaxObjectsEditable() {
		return maxObjectsEditable;
	}

	public void setMaxObjectsEditable(boolean maxObjectsEditable) {
		this.maxObjectsEditable = maxObjectsEditable;
	}

	@Override
	public String toString() {
		return "DataSource [name=" + name + ", url=" + url + ", enabled="
				+ ", type=" + type + ", display=" + display + "]";
	}

	/**
	 * Check the minimum required data
	 * 
	 * @return boolean
	 */
	public boolean isWellFormed() {
		if (type == DataSourceType.KULTURARV) {
			return true;
		}

		boolean out = false;
		if (isUrlWellFormed() || getName() != null || "".equals(getName())) {
			out = true;
		}
		return out;
	}

	public boolean isUrlWellFormed() {
		return getUrl() != null || !"".equals(getUrl())
				|| "http://".equalsIgnoreCase(getUrl());
	}

	@Override
	public int hashCode() {
		// TODO: fix
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (obj.getClass() != getClass())
			return false;

		DataSource ds = (DataSource) obj;
		return this.id == ds.id;
	}
}