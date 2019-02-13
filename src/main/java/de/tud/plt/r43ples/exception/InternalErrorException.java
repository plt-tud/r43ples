package de.tud.plt.r43ples.exception;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * create a HTTP Internal Server Error de.tud.plt.r43ples.exception
 *
 */
public class InternalErrorException extends Exception {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger logger = LogManager.getLogger(InternalErrorException.class);


	/**
	 * Creates an Internal Error Exception
	 * 
	 * @param message
	 *            the String that is the entity of the 500 response.
	 */
	public InternalErrorException(String message) {
		super(message);
		logger.error(message);
	}

}