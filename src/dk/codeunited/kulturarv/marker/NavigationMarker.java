package dk.codeunited.kulturarv.marker;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;

import android.graphics.Path;
import android.location.Location;
import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.mgr.context.DataView;

/**
 * 
 * A NavigationMarker is displayed as an arrow at the bottom of the screen. It
 * indicates directions using the OpenStreetMap as type.
 * 
 * @author hannes
 * @author mixare
 */
public class NavigationMarker extends LocalMarker {

	public NavigationMarker(String id, String title, double latitude,
			double longitude, double altitude, String URL, int type, int color,
			DataSource dataSource) {
		super(id, title, latitude, longitude, altitude, URL, type, color,
				dataSource);
	}

	@Override
	public void update(Location curGPSFix) {

		super.update(curGPSFix);

		// we want the navigation markers to be on the lower part of
		// your surrounding sphere so we set the height component of
		// the position vector radius/2 (in meter) below the user

		locationVector.y -= DataView.getRadius() * 500f;
		// locationVector.y+=-1000;
	}

	@Override
	public void draw(PaintScreen dw) {
		drawArrow(dw);
		drawTextBlock(dw);
	}

	public void drawArrow(PaintScreen dw) {
		if (isVisible) {
			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
					signMarker.x, signMarker.y);
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);

			Path arrow = new Path();
			float radius = maxHeight / 1.5f;
			float x = 0;
			float y = 0;
			arrow.moveTo(x - radius / 3, y + radius);
			arrow.lineTo(x + radius / 3, y + radius);
			arrow.lineTo(x + radius / 3, y);
			arrow.lineTo(x + radius, y);
			arrow.lineTo(x, y - radius);
			arrow.lineTo(x - radius, y);
			arrow.lineTo(x - radius / 3, y);
			arrow.close();
			dw.paintPath(arrow, cMarker.x, cMarker.y, radius * 2, radius * 2,
					currentAngle + 90, 1);
		}
	}
}