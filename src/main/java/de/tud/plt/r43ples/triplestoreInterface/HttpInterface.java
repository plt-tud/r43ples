package de.tud.plt.r43ples.triplestoreInterface;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/** 
 * Provides a interface to the triple store with URI and port specified via init method.
 *
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class HttpInterface extends TripleStoreInterface {

	private static UsernamePasswordCredentials credentials;
	/** The logger. */
	private static Logger logger = Logger.getLogger(HttpInterface.class);
	private static String endpoint;


	
	/**
	 * The constructor.
	 * 
	 * @param sparql_endpoint
	 * 			URI of the endpoint which used be used for further queries
	 * @param sparql_username
	 * 			user name which should be used for authentication
	 * @param sparql_password
	 * 			password which should be used for authentication
	 */
	public HttpInterface(String sparql_endpoint, String sparql_username, String sparql_password) {
		credentials = new UsernamePasswordCredentials(sparql_username, sparql_password);
		endpoint = sparql_endpoint;		
	}
	
	
	/**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * 
	 * @param query the SPARQL query
	 * @return the result of the query in XML serialisation
	 */
	private InputStream executeQueryWithAuthorization(String query) {
		return executeQueryWithAuthorization(query, "application/sparql-results+xml");
	}

	
    /**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query in the specified format
	 */
	private InputStream executeQueryWithAuthorization(String query, String format) {
		HttpResponse response = executeQueryWithAuthorizationResponse(query, format);
		logger.info("Statuscode: " + response.getStatusLine().getStatusCode());
		try{
			InputStream in = response.getEntity().getContent();
			if (response.getStatusLine().getStatusCode() != Status.OK.getStatusCode()) {
				logger.warn(response.getStatusLine().toString()+"\n"+in);
				in.close();
				return null;
			}
			return in;
		} catch (IOException e){
			e.printStackTrace();
			return null;
		}
	}
	
	 /**
	 * Executes a SPARQL-query against the triple store with authorization.
	 * (Based on the source code of the IAF device explorer - created by Sebastian Heinze.)
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query in the specified format
	 */
	private HttpResponse executeQueryWithAuthorizationResponse(String query, String format) {
		logger.debug("Execute query on SPARQL endpoint:\n"+ query);
		DefaultHttpClient httpClient = new DefaultHttpClient();
	    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);
			
	    HttpPost request = new HttpPost(endpoint);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("query", query));
    	try {
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	//request.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    	request.setHeader("Accept", format);
    	
		//Execute Query
		try {
			return httpClient.execute(request);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void close() {
		
	}

	@Override
	public ResultSet executeSelectQuery(String selectQueryString) {
		InputStream result = executeQueryWithAuthorization(selectQueryString, "application/sparql-results+xml");
		return ResultSetFactory.fromXML(result);
	}

	@Override
	public Model executeConstructQuery(String constructQueryString) {
		InputStream result = executeQueryWithAuthorization(constructQueryString, "application/rdf+xml");
		Model model = ModelFactory.createDefaultModel();
		if (result!=null) {
			RDFDataMgr.read(model, result, Lang.RDFXML);
		}
		return model;
	}

	@Override
	public Model executeDescribeQuery(String describeQueryString) {
		InputStream result = executeQueryWithAuthorization(describeQueryString);
		Model model = ModelFactory.createDefaultModel();
		RDFDataMgr.read(model, result, Lang.RDFXML);
		
		return model;
	}

	@Override
	public boolean executeAskQuery(String askQueryString) {
		InputStream result = executeQueryWithAuthorization(askQueryString, "application/sparql-results+xml");
		String answer;
		try {
			answer = IOUtils.toString(result);
			result.close();
			return answer.contains("<boolean>true</boolean>");
		} catch (IOException e) {
			logger.error(e);
			return false;
		}
	}

	@Override
	public void executeUpdateQuery(String updateQueryString) {
		executeQueryWithAuthorization(updateQueryString);
		
	}

	@Override
	public void executeCreateGraph(String graph) {
		executeQueryWithAuthorization("CREATE GRAPH <"+graph+">");
	}

	@Override
	public Iterator<String> getGraphs() {
		InputStream result = executeQueryWithAuthorization("SELECT DISTINCT ?graph WHERE { GRAPH ?graph { ?s ?p ?o}}");
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		List<String> list = new ArrayList<String>();
		while (resultSet.hasNext())
			list.add(resultSet.next().getResource("?graph").toString());
		return list.iterator();
	}
	
	
}
