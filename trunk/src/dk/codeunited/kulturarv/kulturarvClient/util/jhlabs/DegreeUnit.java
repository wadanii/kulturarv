package dk.codeunited.kulturarv.kulturarvClient.util.jhlabs;

/**
 * @author Jerry Huxtable, JH Labs
 */
public class DegreeUnit extends Unit {

	private static final long serialVersionUID = -3212757578604686538L;

	private static AngleFormat format = new AngleFormat(
			AngleFormat.ddmmssPattern, true);

	public DegreeUnit() {
		super("degree", "degrees", "deg", 1);
	}

	@Override
	public double parse(String s) throws NumberFormatException {
		try {
			return format.parse(s).doubleValue();
		} catch (java.text.ParseException e) {
			throw new NumberFormatException(e.getMessage());
		}
	}

	@Override
	public String format(double n) {
		return format.format(n) + " " + abbreviation;
	}

	@Override
	public String format(double n, boolean abbrev) {
		if (abbrev)
			return format.format(n) + " " + abbreviation;
		return format.format(n);
	}

	@Override
	public String format(double x, double y, boolean abbrev) {
		if (abbrev)
			return format.format(x) + "/" + format.format(y) + " "
					+ abbreviation;
		return format.format(x) + "/" + format.format(y);
	}
}
