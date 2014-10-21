package de.tud.plt.r43ples.examples;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.ExampleGenerationManagement;

/**
 * Create an example graph of the following structure:
 * 
 *                  ADD: 2D               ADD: 1G
 *               +-----X---------------------X--------- (Branch B1)
 *               |  DEL: 1A               DEL: 2D
 * ADD: 1A,1B,2C |
 * ---X----------+ (Master)
 * DEL: -        |
 *               |  ADD: 2D,2H            ADD: 2I
 *               +-----X---------------------X--------- (Branch B2)
 *                  DEL: 2C               DEL: -
 * 
 * Contains the renaming of 1A to 1G.
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraphRenaming {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraphRenaming.class);
	/** The graph name. **/
	private static String graphName = "http://exampleGraphRenaming";
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
		String triples =  "<http://example.com/testS> <http://example.com/testP1> \"A\". \n"
						+ "<http://example.com/testS> <http://example.com/testP1> \"B\". \n"
						+ "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";
		
		ExampleGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);
		
		// Create a new branch B1
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");
		
		// Create a new branch B2
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");
		
		// First commit to B1
		String triplesInsert =	  "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";
		
		String triplesDelete =	  "<http://example.com/testS> <http://example.com/testP1> \"A\". \n";
		
		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1", triplesInsert, triplesDelete);
		
		// First commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP2> \"D\". \n"
						+ "<http://example.com/testS> <http://example.com/testP2> \"H\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";
		
		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2", triplesInsert, triplesDelete);
		
		// Second commit to B1
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP1> \"G\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";
		
		ExampleGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1", triplesInsert, triplesDelete);
		
		// Second commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP2> \"I\". \n";
		
		ExampleGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2", triplesInsert);
		
		logger.info("Example graph created.");
	}

}
