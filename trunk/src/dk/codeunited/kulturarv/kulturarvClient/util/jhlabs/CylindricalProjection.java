package dk.codeunited.kulturarv.kulturarvClient.util.jhlabs;

/**
 * The superclass for all cylindrical projections.
 * 
 * @author Jerry Huxtable, JH Labs
 */
public class CylindricalProjection extends Projection {

	@Override
	public boolean isRectilinear() {
		return true;
	}

	@Override
	public String toString() {
		return "Cylindrical";
	}
}