package dk.codeunited.kulturarv.kulturarvClient.parsers;

import java.util.HashMap;
import java.util.Map;

import dk.codeunited.kulturarv.log.LogBridge;

/**
 * Parsers building rating information from "simple" xml.
 * 
 * @author Maksim Sorokin
 */
public class BuildingRatingXMLParser {

	private enum Patterns {
		ID("<id>(.+?)</id>"), //
		VALUE("<bevaringsv.rdi>(.+?)</bevaringsv.rdi>"); // danish char is
															// replaced with "."

		private String patternString;

		private Patterns(String patternString) {
			this.patternString = patternString;
		}

		public String getPatternString() {
			return patternString;
		}
	}

	/**
	 * Parses building value from given "simple" xml.
	 * 
	 * @param xml
	 *            xml to get building value from
	 * @return buliding value from given "simple" xml
	 */
	public static Map.Entry<Long, Integer> parse(String xml) {
		try {
			long id = Long.parseLong(BuildingXMLParser.getChunk(xml,
					Patterns.ID.getPatternString()));
			int value = Integer.parseInt(BuildingXMLParser.getChunk(xml,
					Patterns.VALUE.getPatternString()));

			Map<Long, Integer> m = new HashMap<Long, Integer>();
			m.put(id, value);
			return m.entrySet().iterator().next();
		} catch (Exception e) {
			LogBridge
					.warning("Failed parsing building rating information from \"simple\" xml: "
							+ e.getMessage());
		}

		return null;
	}
}