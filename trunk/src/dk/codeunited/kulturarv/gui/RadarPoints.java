package dk.codeunited.kulturarv.gui;

import java.util.List;

import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenObj;
import org.mixare.lib.marker.Marker;

import android.graphics.Color;
import dk.codeunited.kulturarv.mgr.context.DataView;

/**
 * Takes care of the small radar in the top left corner and of its points
 * 
 * @author daniele
 * @author mixare 
 */
public class RadarPoints implements ScreenObj {
	/** The screen */
	public DataView view;
	/** The radar's range */
	private float range;
	/** Radius in pixel on screen */
	public static float RADIUS = 40;
	/** Position on screen */
	private static float originX = 0, originY = 0;
	/** Color */
	private static int radarColor = Color.argb(100, 0, 0, 200);

	@Override
	public void paint(PaintScreen dw) {
		/** radius is in KM. */
		range = DataView.getRadius() * 1000;
		/** Draw the radar */
		dw.setFill(true);
		dw.setColor(radarColor);
		dw.paintCircle(originX + RADIUS, originY + RADIUS, RADIUS);

		/** put the markers in it */
		float scale = range / RADIUS;

		// DataHandler jLayer = view.getDataHandler();]
		List<Marker> markers = view.getMarkers();

		for (int i = 0; i < markers.size(); i++) {
			Marker pm = markers.get(i);
			float x = pm.getLocationVector().x / scale;
			float y = pm.getLocationVector().z / scale;

			if (pm.isActive() && (x * x + y * y < RADIUS * RADIUS)) {
				dw.setFill(true);

				// For OpenStreetMap the color is changing based on the URL
				dw.setColor(pm.getColour());

				dw.paintRect(x + RADIUS - 1, y + RADIUS - 1, 2, 2);
			}
		}
	}

	/** Width on screen */
	@Override
	public float getWidth() {
		return RADIUS * 2;
	}

	/** Height on screen */
	@Override
	public float getHeight() {
		return RADIUS * 2;
	}
}