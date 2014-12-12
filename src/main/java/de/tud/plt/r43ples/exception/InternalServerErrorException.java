package de.tud.plt.r43ples.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

/**
 * create a HTTP Internal Server Error de.tud.plt.r43ples.exception
 *
 */
public class InternalServerErrorException extends WebApplicationException {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger logger = Logger.getLogger(InternalServerErrorException.class);

	/**
	 * Create a HTTP 500 (Internal Server Error) de.tud.plt.r43ples.exception.
	 */
	public InternalServerErrorException() {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
				.entity("Some error occured").build());
	}

	/**
	 * Create a HTTP 500 (Internal Server Error) de.tud.plt.r43ples.exception.
	 * 
	 * @param message
	 *            the String that is the entity of the 500 response.
	 */
	public InternalServerErrorException(String message) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(message)
				.build());
		logger.error(message);
	}

}