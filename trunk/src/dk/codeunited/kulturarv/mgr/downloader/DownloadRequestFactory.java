package dk.codeunited.kulturarv.mgr.downloader;

import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.model.GeoLocation;

/**
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class DownloadRequestFactory {

	public static GeoLocationBasedDownloadRequest createDownloadRequest(
			DataSource datasource, double lat, double lon, double alt,
			float radius, String locale) {

		GeoLocationBasedDownloadRequest downloadRequest = new GeoLocationBasedDownloadRequest(
				datasource, GeoLocation.fromDegrees(lat, lon, alt), radius);

		if (datasource.getType() == DataSource.DataSourceType.WIKIPEDIA
				|| datasource.getType() == DataSource.DataSourceType.TWITTER) {
			downloadRequest.setParams(datasource.createRequestParams(lat, lon,
					alt, radius, locale));
		}

		return downloadRequest;
	}
}