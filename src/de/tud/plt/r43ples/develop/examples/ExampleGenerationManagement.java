package de.tud.plt.r43ples.develop.examples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Provides methods for generation of example graphs.
 * 
 * @author Stephan Hensel
 *
 */
public class ExampleGenerationManagement {

	/** The logger. */
	private static Logger logger = Logger.getLogger(ExampleGenerationManagement.class);
	/** The endpoint. **/
	private static String endpoint = "http://localhost:9998/r43ples/sparql";
	
	
	/**
	 * Create new graph.
	 * 
	 * @param graphName the graph name
	 * @throws IOException
	 */
	public static void createNewGraph(String graphName) throws IOException {
		// Purge silent example graph
		logger.info("Purge silent example graph");
		String query = String.format("DROP SILENT GRAPH <%s>", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// Create new example graph
		logger.info("Create new example graph");
		query = String.format("CREATE GRAPH <%s>", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
	}
	
	
	/**
	 * Create new branch.
	 * 
	 * @param user the user
	 * @param message the message
	 * @param graphName the graph name
	 * @param revision the revision
	 * @param branchName the branch name
	 * @throws IOException
	 */
	public static void createNewBranch(String user, String message, String graphName, String revision, String branchName) throws IOException {
		logger.info(message);
		String query = String.format(""
				+ "USER \"%s\" \n"
				+ "MESSAGE \"%s\" \n"
				+ "BRANCH GRAPH <%s> REVISION \"%s\" TO \"%s\" \n", user, message, graphName, revision, branchName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
	}
	
	
	/**
	 * Execute INSERT query.
	 * 
	 * @param user the user
	 * @param message the message
	 * @param graphName the graph name
	 * @param revision the revision
	 * @param triples the triples to insert
	 * @throws IOException
	 */
	public static void executeInsertQuery(String user, String message, String graphName, String revision, String triples) throws IOException {
		logger.info(message);
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "}", user, message, graphName, revision, triples);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorizationPost(query, "HTML"));
	}
	
	
	/**
	 * Execute DELETE query.
	 * 
	 * @param user the user
	 * @param message the message
	 * @param graphName the graph name
	 * @param revision the revision
	 * @param triples the triples to delete
	 * @throws IOException
	 */
	public static void executeDeleteQuery(String user, String message, String graphName, String revision, String triples) throws IOException {
		logger.info(message);
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "DELETE { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "}", user, message, graphName, revision, triples);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorizationPost(query, "HTML"));
	}
	
	
	/**
	 * Execute DELETE WHERE query.
	 * 
	 * @param user the user
	 * @param message the message
	 * @param graphName the graph name
	 * @param revision the revision
	 * @param triples the triples to delete
	 * @throws IOException
	 */
	public static void executeDeleteWhereQuery(String user, String message, String graphName, String revision, String triples) throws IOException {
		logger.info(message);
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "DELETE { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "		%s %n"
				+ "	} %n"
				+ "}"
				+ "WHERE { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "		%s %n"
				+ "	} %n"
				+ "}", user, message, graphName, revision, triples, graphName, revision, triples);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorizationPost(query, "HTML"));
	}
	
	
	/**
	 * Execute INSERT - DELETE query.
	 * 
	 * @param user the user
	 * @param message the message
	 * @param graphName the graphName
	 * @param revision the revision
	 * @param triplesInsert the triples to insert
	 * @param triplesDelete the triples to delete
	 * @throws IOException
	 */
	public static void executeInsertDeleteQuery(String user, String message, String graphName, String revision, String triplesInsert, String triplesDelete) throws IOException {
		logger.info(message);
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "} %n"
				+ "DELETE { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "}", user, message, graphName, revision, triplesInsert, graphName, revision, triplesDelete);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorizationPost(query, "HTML"));
	}
	
	
	/**
	 * Executes a SPARQL-query against an endpoint without authorization.
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeQueryWithoutAuthorization(String query, String format) throws IOException {
		URL url = null;
		
		url = new URL(endpoint + "?query=" + URLEncoder.encode(query, "UTF-8") + "&format=" + URLEncoder.encode(format, "UTF-8") + "&timeout=0");
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
	 * Executes a SPARQL-query against an endpoint without authorization using HTTP-POST.
	 * 
	 * @param query the SPARQL query
	 * @param format the format of the result (e.g. HTML, xml/rdf, JSON, ...)
	 * @return the result of the query
	 * @throws IOException 
	 */
	public static String executeQueryWithoutAuthorizationPost(String query, String format) throws IOException {
		URL url = new URL(endpoint);
		Map<String,Object> params = new LinkedHashMap<>();
		params.put("query", query);
		params.put("format", format);
		params.put("timeout", 0);
				
		StringBuilder postData = new StringBuilder();
		for (Map.Entry<String,Object> param : params.entrySet()) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
		logger.debug(postData.toString());
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);

		Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
		String result = "";
		for (int c; (c = in.read()) >= 0; result += (char)c);
		return result;
	}
	
	
	/**
	 * Read file to string.
	 * 
	 * @param path the path to read
	 * @param encoding the encoding
	 * @return the file content
	 * @throws IOException
	 */
	public static String readFileToString(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
	
}
