package dk.codeunited.kulturarv.kulturarvClient.util.jhlabs;

import java.util.Hashtable;

import dk.codeunited.kulturarv.log.LogBridge;

/**
 * This source is copied and adapted from Java Map Projection Library <a
 * href="http://www.jhlabs.com/java/maps/proj/"
 * >http://www.jhlabs.com/java/maps/proj/</a>. The project is released under
 * released under the Apache License.
 * 
 * The project relies on certain code, which is not available in the Android
 * environment, for example {@code java.awt}. Therefore, we adapt only needd
 * parts of the source code.
 * 
 * @author Jerry Huxtable, JH Labs
 */
public class ProjectionFactory {

	private final static String[] EPSG25832_ARGS = new String[] { "+proj=utm",
			"+zone=32", "+ellps=GRS80", "+units=m" };

	private final static double SIXTH = .1666666666666666667; /* 1/6 */
	private final static double RA4 = .04722222222222222222; /* 17/360 */
	private final static double RA6 = .02215608465608465608; /* 67/3024 */
	private final static double RV4 = .06944444444444444444; /* 5/72 */
	private final static double RV6 = .04243827160493827160; /* 55/1296 */

	private static AngleFormat format = new AngleFormat(
			AngleFormat.ddmmssPattern, true);

	public static Projection getEPGS25832Projection() {
		Projection projection = null;
		Ellipsoid ellipsoid = null;
		double a = 0, b = 0, es = 0;

		Hashtable params = new Hashtable();
		for (int i = 0; i < EPSG25832_ARGS.length; i++) {
			String arg = EPSG25832_ARGS[i];
			if (arg.startsWith("+")) {
				int index = arg.indexOf('=');
				if (index != -1) {
					String key = arg.substring(1, index);
					String value = arg.substring(index + 1);
					params.put(key, value);
				}
			}
		}

		Class projectionClass = TransverseMercatorProjection.class;
		try {
			projection = (Projection) projectionClass.newInstance();
			projection.setName("tmerc");
		} catch (Exception e) {
			LogBridge.error("Cannot initialize projection", e);
		}

		String s;

		// Set the ellipsoid
		String ellipsoidName = "";
		s = (String) params.get("R");
		if (s != null)
			a = Double.parseDouble(s);
		else {
			s = (String) params.get("ellps");
			if (s == null)
				s = (String) params.get("datum");
			if (s != null) {
				Ellipsoid[] ellipsoids = Ellipsoid.ellipsoids;
				for (int i = 0; i < ellipsoids.length; i++) {
					if (ellipsoids[i].shortName.equals(s)) {
						ellipsoid = ellipsoids[i];
						break;
					}
				}
				if (ellipsoid == null)
					throw new ProjectionException("Unknown ellipsoid: " + s);
				es = ellipsoid.eccentricity2;
				a = ellipsoid.equatorRadius;
				ellipsoidName = s;
			} else {
				s = (String) params.get("a");
				if (s != null)
					a = Double.parseDouble(s);
				s = (String) params.get("es");
				if (s != null) {
					es = Double.parseDouble(s);
				} else {
					s = (String) params.get("rf");
					if (s != null) {
						es = Double.parseDouble(s);
						es = es * (2. - es);
					} else {
						s = (String) params.get("f");
						if (s != null) {
							es = Double.parseDouble(s);
							es = 1.0 / es;
							es = es * (2. - es);
						} else {
							s = (String) params.get("b");
							if (s != null) {
								b = Double.parseDouble(s);
								es = 1. - (b * b) / (a * a);
							}
						}
					}
				}
				if (b == 0)
					b = a * Math.sqrt(1. - es);
			}

			s = (String) params.get("R_A");
			if (s != null && Boolean.getBoolean(s)) {
				a *= 1. - es * (SIXTH + es * (RA4 + es * RA6));
			} else {
				s = (String) params.get("R_V");
				if (s != null && Boolean.getBoolean(s)) {
					a *= 1. - es * (SIXTH + es * (RV4 + es * RV6));
				} else {
					s = (String) params.get("R_a");
					if (s != null && Boolean.getBoolean(s)) {
						a = .5 * (a + b);
					} else {
						s = (String) params.get("R_g");
						if (s != null && Boolean.getBoolean(s)) {
							a = Math.sqrt(a * b);
						} else {
							s = (String) params.get("R_h");
							if (s != null && Boolean.getBoolean(s)) {
								a = 2. * a * b / (a + b);
								es = 0.;
							} else {
								s = (String) params.get("R_lat_a");
								if (s != null) {
									double tmp = Math.sin(parseAngle(s));
									if (Math.abs(tmp) > MapMath.HALFPI)
										throw new ProjectionException("-11");
									tmp = 1. - es * tmp * tmp;
									a *= .5 * (1. - es + tmp)
											/ (tmp * Math.sqrt(tmp));
									es = 0.;
								} else {
									s = (String) params.get("R_lat_g");
									if (s != null) {
										double tmp = Math.sin(parseAngle(s));
										if (Math.abs(tmp) > MapMath.HALFPI)
											throw new ProjectionException("-11");
										tmp = 1. - es * tmp * tmp;
										a *= Math.sqrt(1. - es) / tmp;
										es = 0.;
									}
								}
							}
						}
					}
				}
			}
		}
		projection.setEllipsoid(new Ellipsoid(ellipsoidName, a, es,
				ellipsoidName));

		// Other arguments
		// projection.setProjectionLatitudeDegrees( 0 );
		// projection.setProjectionLatitude1Degrees( 0 );
		// projection.setProjectionLatitude2Degrees( 0 );
		s = (String) params.get("lat_0");
		if (s != null)
			projection.setProjectionLatitudeDegrees(parseAngle(s));
		s = (String) params.get("lon_0");
		if (s != null)
			projection.setProjectionLongitudeDegrees(parseAngle(s));
		s = (String) params.get("lat_1");
		if (s != null)
			projection.setProjectionLatitude1Degrees(parseAngle(s));
		s = (String) params.get("lat_2");
		if (s != null)
			projection.setProjectionLatitude2Degrees(parseAngle(s));
		s = (String) params.get("lat_ts");
		if (s != null)
			projection.setTrueScaleLatitudeDegrees(parseAngle(s));
		s = (String) params.get("x_0");
		if (s != null)
			projection.setFalseEasting(Double.parseDouble(s));
		s = (String) params.get("y_0");
		if (s != null)
			projection.setFalseNorthing(Double.parseDouble(s));

		s = (String) params.get("k_0");
		if (s == null)
			s = (String) params.get("k");
		if (s != null)
			projection.setScaleFactor(Double.parseDouble(s));

		s = (String) params.get("units");
		if (s != null) {
			Unit unit = Units.findUnits(s);
			if (unit != null)
				projection.setFromMetres(1.0 / unit.value);
		}
		s = (String) params.get("to_meter");
		if (s != null)
			projection.setFromMetres(1.0 / Double.parseDouble(s));

		if (projection instanceof TransverseMercatorProjection) {
			s = (String) params.get("zone");
			if (s != null)
				((TransverseMercatorProjection) projection).setUTMZone(Integer
						.parseInt(s));
		}

		projection.initialize();

		return projection;
	}

	private static double parseAngle(String s) {
		return format.parse(s, null).doubleValue();
	}
}