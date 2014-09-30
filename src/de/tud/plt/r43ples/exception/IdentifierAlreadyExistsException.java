package de.tud.plt.r43ples.exception;

/**
 * Identifier already exists de.tud.plt.r43ples.exception is thrown when name of branch or tag or ... already exists.
 * 
 * @author Stephan Hensel
 *
 */
public class IdentifierAlreadyExistsException extends Exception {

	/** The default serial version UID **/
	private static final long serialVersionUID = 1L;

	public IdentifierAlreadyExistsException() { super(); }
	public IdentifierAlreadyExistsException(String message) { super(message); }
	public IdentifierAlreadyExistsException(String message, Throwable cause) { super(message, cause); }
	public IdentifierAlreadyExistsException(Throwable cause) { super(cause); }

}
