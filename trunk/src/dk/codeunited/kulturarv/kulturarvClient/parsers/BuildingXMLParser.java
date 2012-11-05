package dk.codeunited.kulturarv.kulturarvClient.parsers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.codeunited.kulturarv.kulturarvClient.util.CoordinatesProjectionsConverter;
import dk.codeunited.kulturarv.kulturarvClient.util.jhlabs.Point2D;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.model.Building;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * Knows how to parse building xml into {@link Building} object. Note, that
 * there is no schema (at least we are not aware of one) for the building xmls.
 * Therefore we exploit regex to get information out.
 * 
 * <p>
 * Nearly all buildings have id and coordinate, so we can at least get that. In
 * some rare cases this information is not present in the xml.
 * 
 * @author Maksim Sorokin
 */
public class BuildingXMLParser {

	/**
	 * Describes regex patterns to use for search.
	 */
	private enum Patterns {
		ID("xmlns:kuas=\"http://www.kulturarv.dk\" Id=\"(.+?)\" Vers="), //
		ADDRESS_X(
				"<kuas:AddressXCoordinateValue>(.+?)</kuas:AddressXCoordinateValue>"), //
		ADDRESS_Y(
				"<kuas:AddressYCoordinateValue>(.+?)</kuas:AddressYCoordinateValue>"), //
		NAME("<kuas:Betegnelse>(.+?)</kuas:Betegnelse>"), //
		ARCHITECTURE_DESC(
				"<kuas:ArkitektoniskVurdering>(.+?)</kuas:ArkitektoniskVurdering>");

		private String patternString;

		private Patterns(String patternString) {
			this.patternString = patternString;
		}

		public String getPatternString() {
			return patternString;
		}
	}

	/**
	 * Parses the xml and returns building object out. Note, that the parsing is
	 * in the try/catch block as the we might not find what we need in the
	 * passed xml.
	 * 
	 * @param xml
	 *            xml to parse
	 * @return building object parsed from the xml
	 */
	public static Building parse(String xml) {
		try {
			long id = Long.parseLong(getChunk(xml,
					Patterns.ID.getPatternString()));
			double x = Double.parseDouble(getChunk(xml,
					Patterns.ADDRESS_X.getPatternString()));
			double y = Double.parseDouble(getChunk(xml,
					Patterns.ADDRESS_Y.getPatternString()));
			Point2D.Double epsg25832Coord = new Point2D.Double(x, y);
			GeoLocation location = CoordinatesProjectionsConverter
					.fromEPSG25832ToGoogleMaps(epsg25832Coord);
			String name = getName(xml);
			boolean isInteresting = true;
			if (name == null) {
				name = "Bygning - " + id;
				isInteresting = false;
			}

			return new Building(id, location, name, isInteresting);
		} catch (Exception e) {
			LogBridge
					.warning("Failed parsing building information from \"full\" xml: "
							+ e.getMessage());
		}

		return null;
	}

	private static String getName(String xml) throws Exception {
		String name = getChunk(xml, Patterns.NAME.getPatternString());
		if (name != null) {
			return name;
		}
		String architectureDesc = getChunk(xml,
				Patterns.ARCHITECTURE_DESC.getPatternString());
		if (architectureDesc != null) {
			return architectureDesc;
		}

		return null;
	}

	static String getChunk(String s, String patternString) throws Exception {
		final Pattern pattern = Pattern.compile(patternString);
		final Matcher matcher = pattern.matcher(s);
		matcher.find();
		try {
			return matcher.group(1);
		} catch (Exception e) { // just did not find the pattern
			return null;
		}
	}
}