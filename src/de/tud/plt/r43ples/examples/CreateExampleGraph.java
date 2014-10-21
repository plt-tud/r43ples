package de.tud.plt.r43ples.examples;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.ExampleGenerationManagement;

/**
 * Create an example graph of the following structure:
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
	/** The graph name. **/
	private static String graphName = "http://exampleGraph";
	/** The user. **/
	private static String user = "shensel";
	
	
	/**
	 * Main entry point. Create the example graph.
	 * 
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	
		// Create new example graph
		ExampleGenerationManagement.createNewGraph(graphName);
		
		// Initial commit
		String triples =  "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		ExampleGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);
		
		// Create a new branch B1
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");
		
		// Create a new branch B2
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");
		
		// First commit to B1
		String triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
								+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		
		String triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"A\". \n";
		
		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1", triplesInsert, triplesDelete);
		
		// First commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2", triplesInsert, triplesDelete);
		
		// Second commit to B1
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		ExampleGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1", triplesInsert, triplesDelete);
		
		// Second commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		
		ExampleGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2", triplesInsert);
		
		logger.info("Example graph created.");
	}

}