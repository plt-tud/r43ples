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
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

/** 
 * Provides a interface to the triple store with URI and port specified in the configuration file.
 *
 * @author Stephan Hensel
 *
 */
public class TripleStoreInterface {

	private static UsernamePasswordCredentials credentials;
	/** The logger. */
	private static Logger logger = Logger.getLogger(TripleStoreInterface.class);
	private static String endpoint;


	
	/**
	 * The constructor.
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void init(String sparql_endpoint, String sparql_username, String sparql_password) throws HttpException, IOException {
		credentials = new UsernamePasswordCredentials(sparql_username, sparql_password);
		endpoint = sparql_endpoint;
		if (!RevisionManagement.checkGraphExistence(Config.revision_graph)){
			logger.info("Create revision graph");
			executeQueryWithAuthorization("CREATE SILENT GRAPH <" + Config.revision_graph +">", "HTML");
	 	}
	}
	
	/**
	 * Executes a SPARQL-query against the triple store without authorization.
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeQueryWithoutAuthorization(String query, String format) throws IOException {
		URL url = null;
		
		url = new URL(endpoint+ "?query=" + URLEncoder.encode(query, "UTF-8") + "&format=" + URLEncoder.encode(format, "UTF-8") + "&timeout=0");
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
	
	
	public static String executeQueryWithAuthorization(String query) throws IOException, HttpException {
		return executeQueryWithAuthorization(query, "XML");
	}

	
	/**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String executeQueryWithAuthorization(String query, String format) throws IOException, HttpException {
		String result = null;
		
		logger.debug("Hide all keywords in comments");
		// TODO: fix issue when no line ending after these keywords
		query = query.replace("USER", "#USER").replace("MESSAGE", "#MESSAGE").replace("REVISION", "#REVISION");	
		
		logger.debug("Execute query on SPARQL endpoint:"+ query);
		DefaultHttpClient httpClient = new DefaultHttpClient();
	    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
			
	    HttpPost request = new HttpPost(endpoint);
		
		//set up HTTP Post Request (look at http://virtuoso.openlinksw.com/dataspace/doc/dav/wiki/Main/VOSSparqlProtocol for Protocol)
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("format",format));
		nameValuePairs.add(new BasicNameValuePair("query", query));
		
		HttpResponse response =  null;
		InputStreamReader in = null;
		
		
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		
		//Execute Query
		response = httpClient.execute(request);
		logger.debug("Statuscode: " + response.getStatusLine().getStatusCode());
		try{
			in = new InputStreamReader(response.getEntity().getContent());
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
	
}
