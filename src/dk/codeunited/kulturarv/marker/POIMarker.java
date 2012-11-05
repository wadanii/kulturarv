package dk.codeunited.kulturarv.marker;

import java.text.DecimalFormat;

import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.TextObj;

import android.graphics.Color;
import android.graphics.Path;
import android.location.Location;
import dk.codeunited.kulturarv.data.DataSource;

/**
 * This markers represent the points of interest. On the screen they appear as
 * circles, since this class inherits the draw method of the Marker.
 * 
 * @author hannes
 * @author mixare
 */
public class POIMarker extends LocalMarker {

	public POIMarker(String id, String title, double latitude,
			double longitude, double altitude, String URL, int type, int color,
			DataSource dataSource) {
		super(id, title, latitude, longitude, altitude, URL, type, color,
				dataSource);
	}

	@Override
	public void update(Location curGPSFix) {
		super.update(curGPSFix);
	}

	@Override
	public void drawCircle(PaintScreen dw) {
		if (isVisible) {
			float maxHeight = dw.getHeight();
			dw.setStrokeWidth(maxHeight / 100f);
			dw.setFill(false);

			dw.setColor(getColour());

			// draw circle with radius depending on distance
			// 0.44 is approx. vertical fov in radians
			double angle = 2.0 * Math.atan2(10, distance);
			double radius = Math.max(
					Math.min(angle / 0.44 * maxHeight, maxHeight),
					maxHeight / 25f);

			/*
			 * distance 100 is the threshold to convert from circle to another
			 * shape
			 */
			if (distance < 150.0)
				otherShape(dw);
			else
				dw.paintCircle(cMarker.x, cMarker.y, (float) radius);

		}
	}

	@Override
	public void drawTextBlock(PaintScreen dw) {
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
		// TODO: change textblock only when distance changes

		String textStr = "";

		double d = distance;
		DecimalFormat df = new DecimalFormat("@#");
		if (d < 1000.0) {
			textStr = title + " (" + df.format(d) + "m)";
		} else {
			d = d / 1000.0;
			textStr = title + " (" + df.format(d) + "km)";
		}

		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1, 250,
				dw, underline);

		if (isVisible) {
			// based on the distance set the colour
			if (distance < 100.0) {
				textBlock.setBgColor(Color.argb(128, 52, 52, 52));
				textBlock.setBorderColor(Color.rgb(255, 104, 91));
			} else {
				textBlock.setBgColor(Color.argb(128, 0, 0, 0));
				textBlock.setBorderColor(Color.rgb(255, 255, 255));
			}

			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
					signMarker.x, signMarker.y);
			txtLab.prepare(textBlock);
			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtLab, signMarker.x - txtLab.getWidth() / 2,
					signMarker.y + maxHeight, currentAngle + 90, 1);

		}
	}

	public void otherShape(PaintScreen dw) {
		// This is to draw new shape, triangle
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
				signMarker.x, signMarker.y);
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		dw.setColor(getColour());
		float radius = maxHeight / 1.5f;
		dw.setStrokeWidth(dw.getHeight() / 100f);
		dw.setFill(false);

		Path tri = new Path();
		float x = 0;
		float y = 0;
		tri.moveTo(x, y);
		tri.lineTo(x - radius, y - radius);
		tri.lineTo(x + radius, y - radius);

		tri.close();
		dw.paintPath(tri, cMarker.x, cMarker.y, radius * 2, radius * 2,
				currentAngle + 90, 1);
	}
}