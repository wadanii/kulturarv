package dk.codeunited.kulturarv.marker;

import org.mixare.lib.gui.PaintScreen;

import android.graphics.Bitmap;
import dk.codeunited.kulturarv.data.DataSource;

/**
 * @author Kostas Rutkauskas
 */
public class ImageMarker extends POIMarker {

	private Bitmap image;

	public ImageMarker(String id, String title, double latitude,
			double longitude, double altitude, String URL, int type, int color,
			Bitmap image, DataSource dataSource) {
		super(id, title, latitude, longitude, altitude, URL, type, color,
				dataSource);
		this.image = image;
	}

	@Override
	public void drawCircle(PaintScreen dw) {
		if (isVisible) {
			float maxHeight = dw.getHeight();
			dw.setStrokeWidth(maxHeight / 100f);
			dw.setFill(false);
			dw.setColor(getColour());

			if (distance < 50.0)
				otherShape(dw);
			else
				dw.paintBitmap(image, cMarker.x, cMarker.y);
		}
	}

	@Override
	public Bitmap getImage() {
		return image;
	}
}