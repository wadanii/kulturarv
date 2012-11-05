package dk.codeunited.kulturarv.mgr.context.task;

import java.util.List;

import org.mixare.lib.marker.Marker;

import android.content.Context;
import android.os.AsyncTask;
import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.log.LogBridge;
import dk.codeunited.kulturarv.mgr.downloader.DownloadRequestProcessor;
import dk.codeunited.kulturarv.mgr.downloader.GeoLocationBasedDownloadRequest;

/**
 * @author mixare
 */
public class MarkerDownloaderTask extends
		AsyncTask<GeoLocationBasedDownloadRequest, Void, List<Marker>> {

	public interface OnMarkersDownloadedListener {
		void onMarkersDownloaded(DataSource source, List<Marker> markers);

		void onDownloadFailed(DataSource source, Throwable e);
	}

	private final Context ctx;
	private final OnMarkersDownloadedListener listener;
	private DataSource currentDataSource;

	public DataSource getCurrentDataSource() {
		return currentDataSource;
	}

	public MarkerDownloaderTask(Context ctx,
			OnMarkersDownloadedListener listener) {
		this.ctx = ctx;
		this.listener = listener;
	}

	@Override
	protected List<Marker> doInBackground(
			GeoLocationBasedDownloadRequest... params) {
		GeoLocationBasedDownloadRequest downloadRequest = params[0];
		this.currentDataSource = downloadRequest.getSource();

		List<Marker> markers = null;
		try {
			markers = DownloadRequestProcessor.processDownloadRequest(ctx,
					downloadRequest);
		} catch (Exception e) {
			LogBridge.error("Failed downloading markers from "
					+ getCurrentDataSource().getName(), e);
			if (listener != null) {
				listener.onDownloadFailed(getCurrentDataSource(), e);
			}
		}
		return markers;
	}

	@Override
	protected void onPostExecute(List<Marker> markers) {
		if (listener != null && markers != null) {
			listener.onMarkersDownloaded(getCurrentDataSource(), markers);
		}
	}

	@Override
	protected void onPreExecute() {
		//
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		//
	}
}