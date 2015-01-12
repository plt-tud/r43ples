package de.tud.plt.r43ples.management;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
	 */
	public static void createNewGraph(String graphName) {
		// Purge silent example graph
		logger.info("Purge silent example graph");
		String query = String.format("DROP SILENT GRAPH <%s>", graphName);
		logger.info("Execute query: \n" + query);
		logger.info("Response: \n" + ep.sparql("HTML", query));
		
		// Create new example graph
		logger.info("Create new example graph");
		query = String.format("CREATE SILENT GRAPH <%s>", graphName);
		
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
	 */
	public static void createNewBranch(String user, String message, String graphName, String revision, String branchName) {
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
	 */
	 public static void executeInsertQuery(String user, String message, String graphName, String revision, String triples) {
		logger.info(message);
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "INSERT DATA { GRAPH <%s> REVISION \"%s\" %n"
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
	 */
	public static void executeDeleteQuery(String user, String message, String graphName, String revision, String triples) {
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
	 */
	public static void executeDeleteWhereQuery(String user, String message, String graphName, String revision, String triples) {
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
	 */
	public static void executeInsertDeleteQuery(String user, String message, String graphName, String revision, String triplesInsert, String triplesDelete) {
		logger.info(message);
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "INSERT DATA { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "} ; %n"
				+ "DELETE DATA { GRAPH <%s> REVISION \"%s\" %n"
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
	 * @return the file content
	 * @throws IOException
	 */
	public static String readFileToString(String path) throws IOException {
		StringBuilder string = new StringBuilder();
		InputStream is = ClassLoader.getSystemResourceAsStream(path);
		
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            string.append(line);
        }
		
		return string.toString();
	}
	
}
