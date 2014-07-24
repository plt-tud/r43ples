package de.tud.plt.r43ples.develop.examples;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Create an example graph of the following structure,
 * 
 *                  ADD: D,E              ADD: G
 *               +-----X---------------------X--------- (Branch B1)
 *               |  DEL: A                DEL: D
 * ADD: A,B,C    |
 * ---X----------+ (Master)
 * DEL: -        |
 *               |  ADD: D,H              ADD: I
 *               +-----X---------------------X--------- (Branch B2)
 *                  DEL: C                DEL: -
 * 
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraph {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraph.class);
	/** The endpoint. **/
	private static String endpoint = "http://localhost:9998/r43ples/sparql";
	/** The graph name. **/
	private static String graphName = "exampleGraph";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
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
		
		// Initial commit
		logger.info("Initial commit");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"Initial commit.\" \n"
				+ "INSERT DATA INTO <%s> REVISION \"0\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"C\". \n"
				+ "}", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// Create a new branch B1
		logger.info("Create a new branch B1");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"Branch B1.\" \n"
				+ "BRANCH GRAPH <%s> REVISION \"1\" TO \"B1\" \n", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// Create a new branch B1
		logger.info("Create a new branch B2");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"Branch B2.\" \n"
				+ "BRANCH GRAPH <%s> REVISION \"1\" TO \"B2\" \n", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// First commit to B1
		logger.info("First commit to B1");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"First commit to B1.\" \n"
				+ "INSERT DATA INTO <%s> REVISION \"B1\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"E\". \n"
				+ "}"
				+ "DELETE DATA FROM <%s> REVISION \"B1\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "}", graphName, graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// First commit to B2
		logger.info("First commit to B2");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"First commit to B2.\" \n"
				+ "INSERT DATA INTO <%s> REVISION \"B2\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"H\". \n"
				+ "}"
				+ "DELETE DATA FROM <%s> REVISION \"B2\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"C\". \n"
				+ "}", graphName, graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// Second commit to B1
		logger.info("Second commit to B1");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"Second commit to B1.\" \n"
				+ "INSERT DATA INTO <%s> REVISION \"B1\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"G\". \n"
				+ "}"
				+ "DELETE DATA FROM <%s> REVISION \"B1\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "}", graphName, graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		// Second commit to B2
		logger.info("Second commit to B2");
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"Second commit to B2.\" \n"
				+ "INSERT DATA INTO <%s> REVISION \"B2\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"I\". \n"
				+ "}", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + executeQueryWithoutAuthorization(query, "HTML"));
		
		logger.info("Example graph created.");
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

}
