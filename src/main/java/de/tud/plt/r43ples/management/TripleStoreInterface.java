package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;


/** 
 * Provides a interface to the triple store with URI and port specified via init method.
 *
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class TripleStoreInterface {

	private static UsernamePasswordCredentials credentials;
	/** The logger. */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	private static String endpoint;
	private static String endpoint_update;
	
	/**
	 * The constructor.
	 * 
	 * @param sparql_endpoint
	 * 			URI of the endpoint which used be used for further queries
	 * @param sparql_username
	 * 			user name which should be used for authentication
	 * @param sparql_password
	 * 			password which should be used for authentication
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void init(String sparql_endpoint, String sparql_endpoint_update, String sparql_username, String sparql_password) throws HttpException, IOException {
		credentials = new UsernamePasswordCredentials(sparql_username, sparql_password);
		endpoint = sparql_endpoint;
		endpoint_update = sparql_endpoint_update;
		if (!RevisionManagement.checkGraphExistence(Config.revision_graph)){
			logger.info("Create revision graph");
			executeQueryWithAuthorization("CREATE SILENT GRAPH <" + Config.revision_graph +">", "HTML");
	 	}
		
		// Create SDD graph
		if (!RevisionManagement.checkGraphExistence(Config.sdd_graph)){
			logger.info("Create sdd graph");
			executeQueryWithAuthorization("CREATE SILENT GRAPH <" + Config.revision_graph +">", "HTML");
			// Insert default content into SDD graph
			RevisionManagement.executeINSERT(Config.sdd_graph, MergeManagement.convertJenaModelToNTriple(MergeManagement.readTurtleFileToJenaModel(Config.sdd_graph_defaultContent)));
	 	}
		
	}
	
	/**
	 * Executes a SPARQL query against the triple store without authorization.
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeQueryWithoutAuthorization(String query, String format) throws IOException {
		URL url = null;
		String uri = endpoint;
		String arg = "query=";
		if(isUpdate(query)) {
			uri = endpoint_update;
			arg = "update=";
		}
		url = new URL(uri + "?" + arg + URLEncoder.encode(query, "UTF-8"));// + "&format=" + URLEncoder.encode(format, "UTF-8") + "&timeout=0");
		logger.debug(url.toString());

		URLConnection con = null;
		InputStream in = null;
		con = url.openConnection();
		in = con.getInputStream();
	
		String encoding = con.getContentEncoding();
		encoding = (encoding == null) ? "UTF-8" : encoding;
		String body = IOUtils.toString(in, encoding);
		return body;
		
	}
	
	/**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query in XML serialisation
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String executeQueryWithAuthorization(String query) throws IOException, HttpException {
		return executeQueryWithAuthorization(query, "XML");
	}

	
    /**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query in the specified format
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String executeQueryWithAuthorization(String query, String format) throws IOException, HttpException {
		HttpResponse response = executeQueryWithAuthorizationResponse(query, format);
		logger.debug("Statuscode: " + response.getStatusLine().getStatusCode());
		
		String result;
		
		InputStreamReader in = new InputStreamReader(response.getEntity().getContent());
		try{
			result = IOUtils.toString(in);
			if (response.getStatusLine().getStatusCode() != Status.OK.getStatusCode()) {
				throw new HttpException(response.getStatusLine().toString()+"\n"+result);
			}	
		} catch (HttpException | IOException e){
			throw e;
		}
		finally {
			in.close();
		}
		return result;
	}
	
	 /**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query in the specified format
	 * @throws  
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static HttpResponse executeQueryWithAuthorizationResponse(String query, String format) throws IOException {
		logger.debug("Execute query on SPARQL endpoint:\n"+ query);
		DefaultHttpClient httpClient = new DefaultHttpClient();
	    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
		
	    String uri = endpoint;
	    String arg = "query";
		if(isUpdate(query)) {
			uri = endpoint_update;
			arg = "update";
		}
	    
	    HttpPost request = new HttpPost(uri);
		
		//set up HTTP Post Request (look at http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSSparqlProtocol for Protocol)
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		//nameValuePairs.add(new BasicNameValuePair("format",format));
		nameValuePairs.add(new BasicNameValuePair(arg, query));
    	request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
    	request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    	if(format.equals("XML"))
    		request.setHeader("Accept", "application/rdf+xml");
    	else
    		request.setHeader("Accept", "text");
		
		//Execute Query
		return httpClient.execute(request);
	}
	
	private static boolean isUpdate(String query) {
		query = query.toLowerCase();
		if(
				(query.indexOf("select") == -1) &&
				(query.indexOf("ask") == -1) &&
				(query.indexOf("construct") == -1) &&
				(query.indexOf("describe") == -1) &&
				endpoint_update != "") {
			return true;
		}
		return false;
	}
	
	
}
