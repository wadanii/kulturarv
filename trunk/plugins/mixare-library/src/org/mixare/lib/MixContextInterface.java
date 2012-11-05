package org.mixare.lib;

/**
 * An interface for MixContext, so that it can be used in the libary / Plugin
 * side, without knowing the implementation.
 * 
 * @author A. Egal
 * @author mixare
 */
public interface MixContextInterface {

	void loadMixViewWebPage(String url) throws Exception;
}