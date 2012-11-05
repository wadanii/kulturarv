package org.mixare.lib;

import android.app.Activity;

/**
 * An interface for MixState, so that it can be used in the library / plugin
 * side, without knowing the implementation.
 * 
 * @author A. Egal
 * @author mixare
 */
public interface MixStateInterface {

	boolean handleEvent(Activity ctx, String onPress);
}