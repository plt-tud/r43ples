package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

public class SampleDataSet {

	/** The logger. */
	private static Logger logger = Logger.getLogger(SampleDataSet.class);

	public static void createSampleDataset1(String graph) throws HttpException, IOException {
		RevisionManagement.purgeGraph(graph);
		RevisionManagement.putGraphUnderVersionControl(graph);

		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-1.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-1.nt"), "test_user",
				"test commit message 1", list);
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-2.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-2.nt"), "test_user",
				"test commit message 2", list);
		list.remove("1");
		list.add("2");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-3.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-3.nt"), "test_user",
				"test commit message 3", list);
		list.remove("2");
		list.add("3");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test-delta-added-4.nt"),
				ResourceManagement.getContentFromResource("samples/test-delta-removed-4.nt"), "test_user",
				"test commit message 4", list);
	}

	public static void createSampleDataset2(String graph) throws HttpException, IOException {
		RevisionManagement.purgeGraph(graph);
		RevisionManagement.putGraphUnderVersionControl(graph);

		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test2-delta-added-1.nt"),
				ResourceManagement.getContentFromResource("samples/test2-delta-removed-1.nt"), "test_user",
				"test commit message 1", list);
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test2-delta-added-2.nt"),
				ResourceManagement.getContentFromResource("samples/test2-delta-removed-2.nt"), "test_user",
				"test commit message 2", list);
	}

	
	/**
	 * Create an example graph of the following structure,
	 * 
	 *                  ADD: D,E              ADD: G
	 *               +-----X---------------------X--------- (Branch B1)
	 *               |  DEL: A                DEL: D
	 * ADD: A,B,C    |
	 * ---X----------+ (Master)
	 * DEL: -        |
	 *               |  ADD: D,H              ADD: I    ADD: J
	 *               +-----X---------------------X---------X----- (Branch B2)
	 *                  DEL: C                DEL: -    DEL: -
	 * 
	 * 
	 * @author Stephan Hensel
	 * @author Markus Graube
	 *
	 */
	public static void createSampleDataSetMerging(String graphName) throws IOException, HttpException {
		/** The user. **/
		String user = "shensel";

		// Create new example graph
		ExampleGenerationManagement.createNewGraph(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";

		ExampleGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);

		// Create a new branch B1
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");

		// Create a new branch B2
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"A\". \n";

		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";

		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2",
				triplesInsert, triplesDelete);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";

		ExampleGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		ExampleGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2",
				triplesInsert);
		
		// Third commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		ExampleGenerationManagement.executeInsertQuery(user, "Third commit to B2", graphName, "B2",
				triplesInsert);

		logger.info("Example graph created.");
	}

	public static void createSampleDataSetMergingClasses(String graphName) throws IOException, HttpException {
		/** The user. **/
		String user = "shensel";
		/** The initial content file path **/
		String initialContentFilePath = "resources/verification/ExampleGraphClasses_initial.triples";

		// Read initial content from file to string
		String initialContent = ExampleGenerationManagement.readFileToString(initialContentFilePath,
				StandardCharsets.UTF_8);

		// Create new example graph
		ExampleGenerationManagement.createNewGraph(graphName);

		// Initial commit
		ExampleGenerationManagement
				.executeInsertQuery(user, "Initial commit", graphName, "0", initialContent);

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
		ExampleGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B1", graphName, "B1",
				"<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> ?p ?o . \n");

		// First commit to B2 - insert sub plant T4
		ExampleGenerationManagement.executeInsertQuery(user, "First commit to B2", graphName, "B2", insertT4);

		// Second commit to B2 - delete armature V002
		ExampleGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B2", graphName, "B2",
				"<http://eatld.et.tu-dresden.de/batch/A3A5R01TZU> ?p ?o . \n");

		logger.info("Example graph created.");

	}

	public static void createSampleDataSetRenaming(String graphName) throws IOException, HttpException {
		/** The user. **/
		String user = "shensel";

		// Create new example graph
		ExampleGenerationManagement.createNewGraph(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP1> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";

		ExampleGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);

		// Create a new branch B1
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");

		// Create a new branch B2
		ExampleGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n";

		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"H\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";

		ExampleGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2",
				triplesInsert, triplesDelete);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP1> \"G\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";

		ExampleGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"I\". \n";

		ExampleGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2",
				triplesInsert);

		logger.info("Example graph created.");
	}


}
