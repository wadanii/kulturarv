package dk.codeunited.kulturarv.mgr.context;

import org.mixare.lib.MixUtils;
import org.mixare.lib.render.Matrix;
import org.mixare.lib.render.MixVector;

/**
 * This class calculates the bearing and pitch out of the angles.
 * 
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class MixState {

	private float curBearing;
	private float curPitch;

	public float getCurBearing() {
		return curBearing;
	}

	public float getCurPitch() {
		return curPitch;
	}

	public void calcPitchBearing(Matrix rotationM) {
		MixVector looking = new MixVector();
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);
		this.curBearing = (int) (MixUtils.getAngle(0, 0, looking.x, looking.z) + 360) % 360;

		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
		this.curPitch = -MixUtils.getAngle(0, 0, looking.y, looking.z);
	}
}