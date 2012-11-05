package dk.codeunited.kulturarv.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mixare.lib.marker.Marker;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.data.DataSourceStorage;

/**
 * Knows how to create overlays for markers.
 * 
 * @author Maksim Sorokin
 */
public class MixOverlayCreator {

	/**
	 * Splits markers into different overlays, based on their colors. These
	 * colors should have been received from the specific datasource.
	 * 
	 * @param markers
	 *            markers to create overlays for
	 * @return overlays for different markers' types
	 */
	@SuppressLint("UseSparseArrays")
	public List<MixOverlay> getOverlays(MixMap mixMap, List<Marker> markers) {
		Map<Integer, MixOverlay> colorOverlays = new HashMap<Integer, MixOverlay>();

		for (Marker marker : markers) {
			if (marker.isActive()) {
				OverlayItem item = createOverlayItem(marker);

				if (colorOverlays.containsKey(marker.getColour())) {
					colorOverlays.get(marker.getColour()).addOverlay(item);
				} else {
					int color = marker.getColour();

					Drawable drawable = getDrawableForColor(mixMap, color);
					MixOverlay mixOverlay = new MixOverlay(mixMap, drawable);
					mixOverlay.addOverlay(item);
					colorOverlays.put(color, mixOverlay);
				}
			}
		}

		return new ArrayList<MixOverlay>(colorOverlays.values());
	}

	private OverlayItem createOverlayItem(Marker marker) {
		GeoPoint point = new GeoPoint((int) (marker.getLatitude() * 1E6),
				(int) (marker.getLongitude() * 1E6));
		OverlayItem item = new OverlayItem(point, "", "");
		return item;
	}

	private Drawable getDrawableForColor(MixMap mixMap, int color) {
		int resourceIdentifier = 0; 

		for (DataSource dataSource : DataSourceStorage.getDataSources()) {
			if (color == dataSource.getColor()) {
				resourceIdentifier = mixMap.getResources()
						.getIdentifier(
								"ic_map_marker_"
										+ dataSource.getType().toString()
												.toLowerCase(), "drawable",
								mixMap.getPackageName());
			}
		}
		if (resourceIdentifier != 0) {
			return mixMap.getResources().getDrawable(resourceIdentifier);
		}

		return mixMap.getResources().getDrawable(R.drawable.ic_map_marker_default);
	}
}