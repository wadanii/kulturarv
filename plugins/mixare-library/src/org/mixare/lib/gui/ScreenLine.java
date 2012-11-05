package org.mixare.lib.gui;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.FloatMath;

/**
 * The class stores a point of a two-dimensional coordinate system. (values of
 * the x and y axis)
 * 
 * @author mixare
 */
public class ScreenLine implements Parcelable {
	public float x, y;

	public ScreenLine() {
		set(0, 0);
	}

	public ScreenLine(float x, float y) {
		set(x, y);
	}

	public static final Parcelable.Creator<ScreenLine> CREATOR = new Parcelable.Creator<ScreenLine>() {
		@Override
		public ScreenLine createFromParcel(Parcel in) {
			return new ScreenLine(in);
		}

		@Override
		public ScreenLine[] newArray(int size) {
			return new ScreenLine[size];
		}
	};

	public ScreenLine(Parcel in) {
		readParcel(in);
	}

	public void set(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void rotate(float t) {
		float xp = FloatMath.cos(t) * x - FloatMath.sin(t) * y;
		float yp = FloatMath.sin(t) * x + FloatMath.cos(t) * y;

		x = xp;
		y = yp;
	}

	public void add(float x, float y) {
		this.x += x;
		this.y += y;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(x);
		dest.writeFloat(y);
	}

	public void readParcel(Parcel in) {
		x = in.readFloat();
		y = in.readFloat();
	}
}