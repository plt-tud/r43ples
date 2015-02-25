package de.tud.plt.r43ples.exception;

/**
 * Identifier already exists de.tud.plt.r43ples.exception is thrown when name of
 * branch or tag or ... already exists.
 * 
 * @author Stephan Hensel
 *
 */

public class IdentifierAlreadyExistsException extends InternalErrorException {

	/** The default serial version UID **/
	private static final long serialVersionUID = 1L;


	public IdentifierAlreadyExistsException(String message) {
		super(message);
	}

}
