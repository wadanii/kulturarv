package dk.codeunited.kulturarv.marker;

import org.mixare.lib.gui.PaintScreen;

import android.location.Location;
import dk.codeunited.kulturarv.data.DataSource;
import dk.codeunited.kulturarv.mgr.context.DataView;

/**
 * The SocialMarker class represents a marker, which contains data from sources
 * like twitter etc. Social markers appear at the top of the screen and show a
 * small logo of the source.
 * 
 * @author hannes
 * @author mixare
 */
public class SocialMarker extends LocalMarker {

	public SocialMarker(String id, String title, double latitude,
			double longitude, double altitude, String URL, int type, int color,
			DataSource dataSource) {
		super(id, title, latitude, longitude, altitude, URL, type, color,
				dataSource);
	}

	@Override
	public void update(Location curGPSFix) {

		// we want the social markers to be on the upper part of
		// your surrounding sphere
		double altitude = curGPSFix.getAltitude() + Math.sin(0.35) * distance
				+ Math.sin(0.4)
				* (distance / (DataView.getRadius() * 1000f / distance));
		mGeoLoc.setAltitude(altitude);
		super.update(curGPSFix);

	}

	@Override
	public void draw(PaintScreen dw) {

		drawTextBlock(dw);

		if (isVisible) {
			float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			dw.setStrokeWidth(maxHeight / 10f);
			dw.setFill(false);
			dw.setColor(getColour());
			dw.paintCircle(cMarker.x, cMarker.y, maxHeight / 1.5f);
		}
	}
}