package dk.codeunited.kulturarv.kulturarvClient;

/**
 * Exception that is thrown when we are highly suspicious that Kulturarv service
 * is unavailable. However, this is not necessarily true.
 * 
 * @author Maksim Sorokin
 */
public class KulturarvUnavailableException extends RuntimeException {

	private static final long serialVersionUID = -129818130555493587L;

	public KulturarvUnavailableException(Exception e) {
		super(e);
	}
}