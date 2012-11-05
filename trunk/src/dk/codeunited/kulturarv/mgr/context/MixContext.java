package dk.codeunited.kulturarv.mgr.context;

import org.mixare.lib.render.Matrix;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.widget.Toast;
import dk.codeunited.kulturarv.KulturarvApplication;
import dk.codeunited.kulturarv.R;
import dk.codeunited.kulturarv.activity.MixView;
import dk.codeunited.kulturarv.data.DataSourceStorage;
import dk.codeunited.kulturarv.mgr.location.LocationFinder;
import dk.codeunited.kulturarv.mgr.location.LocationFinderFactory;

/**
 * Cares about location management and about the data (source, inputstream)
 * 
 * @author mixare
 * @author Kostas Rutkauskas
 */
public class MixContext extends ContextWrapper {

	public static final String TAG = KulturarvApplication.getAppContext()
			.getString(R.string.app_tag);

	private MixView mixView;

	private Matrix rotationM = new Matrix();

	/** Responsible for all location tasks */
	private LocationFinder locationFinder;

	public MixContext(MixView appCtx) {
		super(appCtx);
		mixView = appCtx;

		if (!(DataSourceStorage.getAllEnabledDataSources().size() > 0)) {
			rotationM.toIdentity();
		}
	}

	public String getStartUrl() {
		Intent intent = ((Activity) getActualMixView()).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString();
		}

		return "";
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	public void doResume(MixView mixView) {
		setActualMixView(mixView);
	}

	public void updateSmoothRotation(Matrix smoothR) {
		synchronized (rotationM) {
			rotationM.set(smoothR);
		}
	}

	public LocationFinder getLocationFinder() {
		if (this.locationFinder == null) {
			locationFinder = LocationFinderFactory.makeLocationFinder(this);
		}
		return locationFinder;
	}

	public MixView getActualMixView() {
		synchronized (mixView) {
			return this.mixView;
		}
	}

	private void setActualMixView(MixView mv) {
		synchronized (mixView) {
			this.mixView = mv;
		}
	}

	@Override
	public ContentResolver getContentResolver() {
		ContentResolver out = super.getContentResolver();
		if (super.getContentResolver() == null) {
			out = getActualMixView().getContentResolver();
		}
		return out;
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param string
	 *            message
	 */
	public void doPopUp(final String string) {
		Toast.makeText(this, string, Toast.LENGTH_LONG).show();
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param connectionGpsDialogText
	 */
	public void doPopUp(int RidOfString) {
		doPopUp(this.getString(RidOfString));
	}
}