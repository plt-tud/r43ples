package de.tud.plt.r43ples.examples;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.management.ExampleGenerationManagement;

/**
 * Create an example graph which contains classes.
 * 
 * @author Stephan Hensel
 *
 */
public class CreateExampleGraphClasses {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CreateExampleGraphClasses.class);
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
		String initialContent = ExampleGenerationManagement.readFileToString(initialContentFilePath, StandardCharsets.UTF_8);
		
		// Create new example graph
		ExampleGenerationManagement.createNewGraph(graphName);
		
		// Initial commit
		ExampleGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", initialContent);

		// Create a new branch B1
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");
		
		// Create a new branch B2
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

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
		
		ExampleGenerationManagement.executeInsertQuery(user, "First commit to B1", graphName, "B1", insertT4);

		// Second commit to B1 - delete sub plant T4
		ExampleGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B1", graphName, "B1", "<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> ?p ?o . \n");

		// First commit to B2 - insert sub plant T4
		ExampleGenerationManagement.executeInsertQuery(user, "First commit to B2", graphName, "B2", insertT4);
		
		// Second commit to B2 - delete armature V002
		ExampleGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B2", graphName, "B2", "<http://eatld.et.tu-dresden.de/batch/A3A5R01TZU> ?p ?o . \n");
		
		logger.info("Example graph created.");
	}
	
}