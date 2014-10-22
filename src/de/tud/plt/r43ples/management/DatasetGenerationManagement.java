package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.webservice.Endpoint;

/**
 * Provides methods for generation of example graphs.
 * 
 * @author Stephan Hensel
 *
 */
public class DatasetGenerationManagement {

	/** The logger. */
	private static Logger logger = Logger.getLogger(DatasetGenerationManagement.class);
	/** The endpoint. **/
	private static Endpoint ep = new Endpoint();
	
	
	/**
	 * Create new graph.
	 * 
	 * @param graphName the graph name
	 * @throws IOException
	 * @throws HttpException 
	 */
	public static void createNewGraph(String graphName) throws IOException, HttpException {
		// Purge silent example graph
		logger.info("Purge silent example graph");
		String query = String.format("DROP SILENT GRAPH <%s>", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + ep.sparql("HTML", query));
		
		// Create new example graph
		logger.info("Create new example graph");
		query = String.format("CREATE GRAPH <%s>", graphName);
		
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + ep.sparql("HTML", query));
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
	 * @throws HttpException 
	 */
	public static void createNewBranch(String user, String message, String graphName, String revision, String branchName) throws IOException, HttpException {
		logger.info(message);
		String query = String.format(""
				+ "USER \"%s\" \n"
				+ "MESSAGE \"%s\" \n"
				+ "BRANCH GRAPH <%s> REVISION \"%s\" TO \"%s\" \n", user, message, graphName, revision, branchName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + ep.sparql("HTML", query));
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
	 * @throws HttpException 
	 */
	 public static void executeInsertQuery(String user, String message, String graphName, String revision, String triples) throws IOException, HttpException {
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
		logger.info("Response: \n" + ep.sparql("HTML", query));
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
	 * @throws HttpException 
	 */
	public static void executeDeleteQuery(String user, String message, String graphName, String revision, String triples) throws IOException, HttpException {
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
		logger.info("Response: \n" + ep.sparql("HTML", query));
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
	 * @throws HttpException 
	 */
	public static void executeDeleteWhereQuery(String user, String message, String graphName, String revision, String triples) throws IOException, HttpException {
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
		logger.info("Response: \n" + ep.sparql("HTML", query));
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
	 * @throws HttpException 
	 */
	public static void executeInsertDeleteQuery(String user, String message, String graphName, String revision, String triplesInsert, String triplesDelete) throws IOException, HttpException {
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
		logger.info("Response: \n" + ep.sparql("HTML", query));
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
