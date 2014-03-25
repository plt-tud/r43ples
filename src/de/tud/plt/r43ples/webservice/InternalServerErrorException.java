package de.tud.plt.r43ples.webservice;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * create a HTTP not found exception
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
		super(Response.status(Status.INTERNAL_SERVER_ERROR).type("text/plain").build());
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
 
    public InternalServerErrorException(String... errors)
    {
        this(Arrays.asList(errors));
    }
 
    public InternalServerErrorException(List<String> errors)
    {
        super(Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_XHTML_XML)
                .entity(new GenericEntity<List<String>>(errors)
                {}).build());
    }
 
}