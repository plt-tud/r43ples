package de.tud.plt.r43ples.exception;

/**
 * Identifier already exists de.tud.plt.r43ples.exception is thrown when name of
 * branch or tag or ... already exists.
 * 
 * @author Markus Graube
 *
 */

public class QueryErrorException extends InternalErrorException {

	/** The default serial version UID **/
	private static final long serialVersionUID = 1L;


	public QueryErrorException(String message) {
		super(message);
	}

}
