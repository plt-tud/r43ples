package de.tud.plt.r43ples.webservice;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class ExceptionMapper implements
		javax.ws.rs.ext.ExceptionMapper<Exception> {
	private static Logger logger = Logger.getLogger(ExceptionMapper.class);

	@Override
	public Response toResponse(Exception e) {
		logger.error(e.getMessage(), e);

		Response response;
		if (e instanceof WebApplicationException) {
			WebApplicationException webEx = (WebApplicationException) e;
			response = webEx.getResponse();
		} else {
			response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("Internal error").type("text/plain").build();
		}
		return response;
	}
}
