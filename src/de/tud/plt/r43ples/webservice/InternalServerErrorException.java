package de.tud.plt.r43ples.webservice;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * create a HTTP Internal Server Error exception
 *
 */
public class InternalServerErrorException extends WebApplicationException {
	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Create a HTTP 500 (Internal Server Error) exception.
	 */
	public InternalServerErrorException() {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity("Some error occured").build());
	}

	/**
	 * Create a HTTP 500 (Internal Server Error) exception.
	 * 
	 * @param message
	 *            the String that is the entity of the 500 response.
	 */
	public InternalServerErrorException(String message) {
		super(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN).entity(message).build());
	}
 
 
}