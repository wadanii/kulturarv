package dk.codeunited.kulturarv.kulturarvClient.util.jhlabs;

import java.io.Serializable;
import java.text.NumberFormat;

/**
 * @author Jerry Huxtable, JH Labs
 */
public class Unit implements Serializable {

	static final long serialVersionUID = -6704954923429734628L;

	public final static int ANGLE_UNIT = 0;
	public final static int LENGTH_UNIT = 1;
	public final static int AREA_UNIT = 2;
	public final static int VOLUME_UNIT = 3;

	public String name, plural, abbreviation;
	public double value;
	public static NumberFormat format;

	static {
		format = NumberFormat.getNumberInstance();
		format.setMaximumFractionDigits(2);
		format.setGroupingUsed(false);
	}

	public Unit(String name, String plural, String abbreviation, double value) {
		this.name = name;
		this.plural = plural;
		this.abbreviation = abbreviation;
		this.value = value;
	}

	public double toBase(double n) {
		return n * value;
	}

	public double fromBase(double n) {
		return n / value;
	}

	public double parse(String s) throws NumberFormatException {
		try {
			return format.parse(s).doubleValue();
		} catch (java.text.ParseException e) {
			throw new NumberFormatException(e.getMessage());
		}
	}

	public String format(double n) {
		return format.format(n) + " " + abbreviation;
	}

	public String format(double n, boolean abbrev) {
		if (abbrev)
			return format.format(n) + " " + abbreviation;
		return format.format(n);
	}

	public String format(double x, double y, boolean abbrev) {
		if (abbrev)
			return format.format(x) + "/" + format.format(y) + " "
					+ abbreviation;
		return format.format(x) + "/" + format.format(y);
	}

	public String format(double x, double y) {
		return format(x, y, true);
	}

	@Override
	public String toString() {
		return plural;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Unit) {
			return ((Unit) o).value == value;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (int) (value * 1000);
	}
}