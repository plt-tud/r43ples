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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Create an example graph which contains classes of the following structure:
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
public class CreateExampleGraphClasses {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraphClasses.class);
	/** The endpoint. **/
	private static String endpoint = "http://localhost:9998/r43ples/sparql";
	/** The graph name. **/
	private static String graphName = "http://exampleGraphClasses";
	/** The user. **/
	private static String user = "shensel";
	/** The initial content file path **/
	private static String initialContentFilePath = "resources/verification/ExampleGraphClasses_initial.triples";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
				
		// Read initial content from file to string
		String initialContent = readFileToString(initialContentFilePath, StandardCharsets.UTF_8);
		
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
		executeInsertQuery(user, "Initial commit", graphName, "0", initialContent);

		// Create a new branch B1
		createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");
		
		// Create a new branch B2
		createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

		// First commit to B1 - insert sub plant T4
		String insertT4 = "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://eatld.et.tu-dresden.de/mso/Unit> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://www.w3.org/2000/01/rdf-schema#label> \"T4\"@en . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/rfid> \"E00401007837683C\"@en . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R02ZZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/isPartOfProcessCell> <http://eatld.et.tu-dresden.de/batch/A3A5R03UZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R06OZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R01ZZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/comosUid> \"A3A5R07QZU\"@en . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/plantID> \"=TUDPLT.A1.T4\"@en . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R02BZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R1AMZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R05NZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://eatld.et.tu-dresden.de/mso/hasEquipment> <http://eatld.et.tu-dresden.de/batch/A3A5R01PZU> . \n"
						+ "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> <http://www.w3.org/2000/01/rdf-schema#comment> \"Subplant flush\"@en . \n";
		executeInsertQuery(user, "First commit to B1", graphName, "B1", insertT4);

		// Second commit to B1 - delete sub plant T4
		executeDeleteWhereQuery(user, "Second commit to B1", graphName, "B1", "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> ?p ?o . \n");

		// First commit to B2 - insert sub plant T4
		executeInsertQuery(user, "First commit to B2", graphName, "B2", insertT4);
		
		// Second commit to B2 - delete armature V002
		executeDeleteWhereQuery(user, "Second commit to B2", graphName, "B2", "<http://eatld.et.tu-dresden.de/batch/A3A5R01TZU> ?p ?o . \n");
		
		logger.info("Example graph created.");
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
	private static String readFileToString(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

}
