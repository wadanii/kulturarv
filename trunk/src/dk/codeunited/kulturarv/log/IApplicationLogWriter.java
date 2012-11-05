package dk.codeunited.kulturarv.log;

/**
 * @author Kostas Rutkauskas
 */
interface IApplicationLogWriter {

	void debug(String message);

	void warning(String message);

	void error(String message, Throwable exception);

	void fatal(String message, Throwable exception);
}