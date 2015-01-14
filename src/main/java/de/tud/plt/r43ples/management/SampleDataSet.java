package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class SampleDataSet {

	/** The logger. */
	private static Logger logger = Logger.getLogger(SampleDataSet.class);

	public static void createSampleDataset1(String graph)  {
		RevisionManagement.purgeGraph(graph);
		RevisionManagement.putGraphUnderVersionControl(graph);

		ArrayList<String> list = new ArrayList<String>();
		list.add("0");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-1.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-1.nt"), "test_user",
				"test commit message 1", list);
		list.remove("0");
		list.add("1");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-2.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-2.nt"), "test_user",
				"test commit message 2", list);
		list.remove("1");
		list.add("2");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-3.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-3.nt"), "test_user",
				"test commit message 3", list);
		list.remove("2");
		list.add("3");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-4.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-4.nt"), "test_user",
				"test commit message 4", list);
		list.remove("3");
		list.add("4");
		RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-5.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-5.nt"), "test_user",
				"test commit message 5", list);
	}

	public static void createSampleDataset2(String graph) {
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
	public static void createSampleDataSetMerging(String graphName) {
		/** The user. **/
		String user = "shensel";

		// Create new example graph
		DatasetGenerationManagement.createNewGraph(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";

		DatasetGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"A\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2",
				triplesInsert, triplesDelete);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		DatasetGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2",
				triplesInsert);
		
		// Third commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		DatasetGenerationManagement.executeInsertQuery(user, "Third commit to B2", graphName, "B2",
				triplesInsert);
		
		

		logger.info("Example graph created.");
	}
	
	
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
	 * Used in diploma thesis of Stephan Hensel.
	 * 
	 * @author Stephan Hensel
	 *
	 */
	public static void createSampleDataSetDA(String graphName) {
		/** The user. **/
		String user = "shensel";

		// Create new example graph
		DatasetGenerationManagement.createNewGraph(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";

		DatasetGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"A\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2",
				triplesInsert, triplesDelete);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		DatasetGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2",
				triplesInsert);

		logger.info("Example graph created.");
	}

	
	/**
	 * 
	 * 
	 * @param graphName the graph name
	 * @throws IOException
	 */
	public static void createSampleDataSetMergingClasses(String graphName) throws IOException {
		/** The user. **/
		String user = "shensel";
		/** The initial content file path **/
		String initialContentFilePath = "verification/ExampleGraphClasses_initial.triples";

		// Read initial content from file to string
		String initialContent = DatasetGenerationManagement.readFileToString(initialContentFilePath);

		// Create new example graph
		DatasetGenerationManagement.createNewGraph(graphName);

		// Initial commit
		DatasetGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", initialContent);

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

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

		DatasetGenerationManagement.executeInsertQuery(user, "First commit to B1", graphName, "B1", insertT4);

		// Second commit to B1 - delete sub plant T4
		DatasetGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B1", graphName, "B1",
				"<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> ?p ?o . \n");

		// First commit to B2 - insert sub plant T4
		DatasetGenerationManagement.executeInsertQuery(user, "First commit to B2", graphName, "B2", insertT4);

		// Second commit to B2 - delete armature V002
		DatasetGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B2", graphName, "B2",
				"<http://eatld.et.tu-dresden.de/batch/A3A5R01TZU> ?p ?o . \n");

		logger.info("Example graph created.");

	}

	
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
	 * @param graphName the graph name
	 * @throws IOException
	 * 
	 * @author Stephan Hensel
	 *
	 */
	public static void createSampleDataSetRenaming(String graphName) throws IOException {
		/** The user. **/
		String user = "shensel";

		// Create new example graph
		DatasetGenerationManagement.createNewGraph(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP1> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";

		DatasetGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"H\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2",
				triplesInsert, triplesDelete);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP1> \"G\". \n";

		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";

		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1",
				triplesInsert, triplesDelete);

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"I\". \n";

		DatasetGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2",
				triplesInsert);

		logger.info("Example graph created.");
	}

	
	/**
	 * Create an example graph of the following structure:
	 * 
	 *                                              ADD: -      ADD: -                                                     
	 *                                           +-----X-----------X-----------(Branch B1X)--+                            
	 *                                           |  DEL: B      DEL: C                        \                              
	 *                                           |                                             \                             
	 *                  ADD: D,E       ADD: G    |        ADD: F                                \                          
	 *               +-----X--------------X------+-----------X-----------------(Branch B1)-------+--+                       
	 *               |  DEL: A         DEL: D             DEL: -                                     \                       
	 *               |                                                                                \                      
	 *               |                              ADD: J      ADD: C                                 \                     
	 *               |                           +-----X-----------X-----------(Branch B2X)--+          \                                   
	 *               |                           |  DEL: -      DEL: I                        \          \                  
	 *               |                           |                                             \          \                  
	 *               |  ADD: D,H       ADD: I    |  ADD: K,L    ADD: M                          \          \              
	 *               +-----X--------------X------+-----X-----------X-----------(Branch B2)-------+----------+--+             
	 *               |  DEL: C         DEL: -       DEL: I      DEL: -                                          \            
	 *               |                                                                                           \           
	 *               |                                                                                            \          
	 * ADD: A,B,C    |          ADD: M,N            ADD: P,R,S                                                     \        
	 * ---X----------+-------------X-------------------X-----------------------(MASTER)-----------------------------+--      
	 * DEL: -                   DEL: C              DEL: M                                                                     
	 * 
	 * @param graphName the graph name
	 * 
	 * @author Stephan Hensel
	 *
	 */
	public static void createSampleDataSetComplexStructure(String graphName) {
		/** The user. **/
		String user = "shensel";
		
		// Create new example graph
		DatasetGenerationManagement.createNewGraph(graphName);
		
		// Initial commit
		String triples =  "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		DatasetGenerationManagement.executeInsertQuery(user, "Initial commit", graphName, "0", triples);
		
		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, "1", "B1");
		
		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, "1", "B2");
		
		// First commit to B1
		String triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
								+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		
		String triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"A\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B1", graphName, "B1", triplesInsert, triplesDelete);
		
		// Second commit to B1
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B1", graphName, "B1", triplesInsert, triplesDelete);
		
		// Create a new branch B1X
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1X", graphName, "B1", "B1X");
		
		// First commit to B1X
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"B\". \n";
		
		DatasetGenerationManagement.executeDeleteQuery(user, "First commit to B1X", graphName, "B1X", triplesDelete);
		
		// Second commit to B1X
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		DatasetGenerationManagement.executeDeleteQuery(user, "Second commit to B1X", graphName, "B1X", triplesDelete);
		
		// Third commit to B1
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"F\". \n";
		
		DatasetGenerationManagement.executeInsertQuery(user, "Third commit to B1", graphName, "B1", triplesInsert);
		
		// First commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "First commit to B2", graphName, "B2", triplesInsert, triplesDelete);
		
		// Second commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		
		DatasetGenerationManagement.executeInsertQuery(user, "Second commit to B2", graphName, "B2", triplesInsert);
		
		// Create a new branch B2X
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2X", graphName, "B2", "B2X");
		
		// First commit to B2X
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		
		DatasetGenerationManagement.executeInsertQuery(user, "First commit to B2X", graphName, "B2X", triplesInsert);
		
		// Second commit to B2X
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Second commit to B2X", graphName, "B2X", triplesInsert, triplesDelete);
		
		// Third commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"K\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"L\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Third commit to B2", graphName, "B2", triplesInsert, triplesDelete);
		
		// Fourth commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"M\". \n";
		
		DatasetGenerationManagement.executeInsertQuery(user, "Fourth commit to B2", graphName, "B2", triplesInsert);
		
		// Second commit to master
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"M\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"N\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Second commit to MASTER", graphName, "master", triplesInsert, triplesDelete);
		
		// Third commit to master
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"P\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"R\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"S\". \n";
		
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"M\". \n";
		
		DatasetGenerationManagement.executeInsertDeleteQuery(user, "Third commit to MASTER", graphName, "master", triplesInsert, triplesDelete);
		
		logger.info("Example graph created.");
	}
	
}
