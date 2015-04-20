package de.tud.plt.r43ples.merging.model.structure;

import java.util.List;
import java.util.Map;

/**
 * Stores the HTTP response of a query.
 * 
 * @author Stephan Hensel
 *
 */
public class HttpResponse {
	
	/** The response status code. **/
	private int statusCode;
	/** The response header parameters. **/
	private Map<String, List<String>> headerParameters;
	/** The response body. **/
	private String body;
	
	
	/**
	 * The constructor.
	 * 
	 * @param statusCode the status code
	 * @param headerParameters the header parameters
	 * @param body the body
	 */
	public HttpResponse(int statusCode, Map<String, List<String>> headerParameters, String body) {
		this.statusCode = statusCode;
		this.headerParameters = headerParameters;
		this.body = body;
	}
	

	/**
	 * Get the status code.
	 * 
	 * @return the status code
	 */
	public int getStatusCode() {
		return statusCode;
	}


	/**
	 * Set the status code.
	 * 
	 * @param statusCode the status code
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}


	/**
	 * Get the header parameters.
	 * 
	 * @return the header parameters
	 */
	public Map<String, List<String>> getHeaderParameters() {
		return headerParameters;
	}


	/**
	 * Set the header parameters.
	 * 
	 * @param headerParameters the header parameters
	 */
	public void setHeaderParameters(Map<String, List<String>> headerParameters) {
		this.headerParameters = headerParameters;
	}
	
	
	/**
	 * Get header parameter by name.
	 * 
	 * @param headerParameterName the header parameter name
	 * @return the list of values
	 */
	public List<String> getHeaderParameterByName(String headerParameterName) {
		return headerParameters.get(headerParameterName);
	}

	
	/**
	 * Get the body.
	 * 
	 * @return the body
	 */
	public String getBody() {
		return body;
	}


	/**
	 * Set the body
	 * 
	 * @param body the body
	 */
	public void setBody(String body) {
		this.body = body;
	}

}
