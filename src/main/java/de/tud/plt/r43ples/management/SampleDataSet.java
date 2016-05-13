package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;

/**
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class SampleDataSet {

	/** The logger. */
	private static Logger logger = Logger.getLogger(SampleDataSet.class);
	
	/** The user. **/
	private static final String user = "butler";

	public static DataSetGenerationResult createSampleDataset1() throws InternalErrorException  {
		DataSetGenerationResult result = new DataSetGenerationResult();	
		String graph = "http://test.com/r43ples-dataset-1";
		result.graphName = graph;
		RevisionManagement.purgeRevisionInformation(graph);
		String revisionNumber0 = RevisionManagement.putGraphUnderVersionControl(graph, "2016-01-01T14:51:37.011");
		result.revisions.put("master-0", revisionNumber0);

		String revisionNumber1 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-1.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-1.nt"), user,
				"2016-01-01T14:51:37.012",
				"test commit message 1", revisionNumber0);
		result.revisions.put("master-1", revisionNumber1);
		
		String revisionNumber2 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-2.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-2.nt"), user,
				"2016-01-02T09:06:52.521",
				"test commit message 2", revisionNumber1);
		result.revisions.put("master-2", revisionNumber2);
		
		String revisionNumber3 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-3.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-3.nt"), user,
				"2016-01-08T14:07:59.739",
				"test commit message 3", revisionNumber2);
		result.revisions.put("master-3", revisionNumber3);
		
		String revisionNumber4 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-4.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-4.nt"), user,
				"2016-01-10T08:12:23.018",
				"test commit message 4", revisionNumber3);
		result.revisions.put("master-4", revisionNumber4);
		
		String revisionNumber5 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset1/added-5.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-5.nt"), user,
				"2016-01-21T23:07:52.104",
				"test commit message 5", revisionNumber4);
		result.revisions.put("master-5", revisionNumber5);
		return result;
	}

	public static DataSetGenerationResult createSampleDataset2() throws InternalErrorException {
		DataSetGenerationResult result = new DataSetGenerationResult();	
		String graph = "http://test.com/r43ples-dataset-2";
		result.graphName = graph;
		RevisionManagement.purgeRevisionInformation(graph);
		String revisionNumber0 = RevisionManagement.putGraphUnderVersionControl(graph);
		result.revisions.put("master-0", revisionNumber0);

		String revisionNumber1 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test2-addSet-1.nt"),
				ResourceManagement.getContentFromResource("samples/test2-deleteSet-1.nt"), user,
				"test commit message 1", revisionNumber0);
		result.revisions.put("master-1", revisionNumber1);
		String revisionNumber2 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/test2-addSet-2.nt"),
				ResourceManagement.getContentFromResource("samples/test2-deleteSet-2.nt"), user,
				"test commit message 2", revisionNumber1);
		result.revisions.put("master-2", revisionNumber2);
		return result;
	}
	
	public static DataSetGenerationResult createSampleDataset3() throws InternalErrorException  {
		DataSetGenerationResult result = new DataSetGenerationResult();	
		String graph = "http://test.com/r43ples-dataset-3";
		result.graphName = graph;
		RevisionManagement.purgeRevisionInformation(graph);
		String revisionNumber0 = RevisionManagement.putGraphUnderVersionControl(graph, "2015-01-01T14:51:37");
		result.revisions.put("master-0", revisionNumber0);
		
		
		String revisionNumber1 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset3/added-1.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-1.nt"), user,
				"2015-02-01T21:32:52",
				"test commit message 1", revisionNumber0);
		result.revisions.put("master-1", revisionNumber1);
		// Create a new branch B1
				DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graph, revisionNumber1, "B1");

		String revisionB1_0 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset3/added-2.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-2.nt"), user,
				"2015-03-02T09:06:52",
				"test commit message 2", "B1".toLowerCase());
		result.revisions.put("b1-0", revisionB1_0);
		
		String revisionNumber3 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset3/added-3.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-3.nt"), user,
				"2015-04-08T14:07:59",
				"test commit message 3", revisionNumber1);
		result.revisions.put("master-3", revisionNumber3);
		
		String revisionNumber4 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset3/added-4.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-4.nt"), user,
				"2015-05-10T08:12:23",
				"test commit message 4", revisionNumber3);
		result.revisions.put("master-4", revisionNumber4);
		ArrayList<String> usedRevisions5 = new ArrayList<>();
		usedRevisions5.add(revisionNumber4);
		usedRevisions5.add(revisionB1_0);
		String revisionNumber5 = RevisionManagement.createNewRevision(graph,
				ResourceManagement.getContentFromResource("samples/dataset3/added-5.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-5.nt"), user,
				"2015-06-21T23:07:52",
				"test commit message 5", usedRevisions5);
		result.revisions.put("master-5", revisionNumber5);
		return result;
	}


	
	/**
	 * Create an example graph of the following structure,
	 * 
	 *                  ADD: D,E              ADD: G
	 *               +-----X---------------------X--------- (Branch B1 = [B,C,E,G)
	 *               |  DEL: A                DEL: D
	 * ADD: A,B,C    |
	 * ---X----------+ (Master)
	 * DEL: -        |
	 *               |  ADD: D,H              ADD: I    ADD: J
	 *               +-----X---------------------X---------X----- (Branch B2 = [A,B,D,H,I,J)
	 *                  DEL: C                DEL: -    DEL: -
	 * 
	 * 
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 *
	 */
	public static DataSetGenerationResult createSampleDataSetMerging() throws InternalErrorException {
		DataSetGenerationResult result = new DataSetGenerationResult();		
		String graphName = "http://test.com/r43ples-dataset-merging";
		result.graphName = graphName;
		
		//delete the old graph
		RevisionManagement.purgeRevisionInformation(graphName);
		
		String revision0 = RevisionManagement.putGraphUnderVersionControl(graphName);
		result.revisions.put("master-0", revision0);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		String revision1 = RevisionManagement.createNewRevision(graphName, triples, null, user, "Initial commit", revision0);
		result.revisions.put("master-1", revision1);

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"A\". \n";
		String revisionB1_0 = RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "First commit to B1", "B1".toLowerCase());
		result.revisions.put("b1-0", revisionB1_0);
		

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		String revisionB2_0 = RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "First commit to B2", "B2".toLowerCase());
		result.revisions.put("b2-0", revisionB2_0);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		String revisionB1_1 = RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "Second commit to B1", revisionB1_0);
		result.revisions.put("b1-1", revisionB1_1);
		

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		String revisionB2_1 = RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "Second commit to B2", revisionB2_0);
		result.revisions.put("b2-1", revisionB2_1);
		
		// Third commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		String revisionB2_2 = RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "Third commit to B2", revisionB2_1);
		result.revisions.put("b2-2", revisionB2_2);
		
		logger.info("Example graph <" + graphName +"> created.");
		return result;
	}
	
	
	
	
		
	
	/**
	 * Create an example graph of the following structure,
	 * 
	 *                  ADD: D,E              ADD: G
	 *               +-----X---------------------X--------- (Branch B1)
	 *               |  DEL: -                DEL: -
	 * ADD: A,B,C    |
	 * ---X----------+ (Master)
	 * DEL: -        |
	 *               |  ADD: H              ADD: I    ADD: J
	 *               +-----X---------------------X---------X----- (Branch B2)
	 *                  DEL: C                DEL: -    DEL: -
	 * 
	 * 
	 * @throws InternalErrorException 
	 * @throws IOException 
	 * @throws TemplateException 
	 *
	 */
	public static String createSampleDataSetRebase() throws InternalErrorException {
		String graphName = "http://test.com/r43ples-dataset-rebase";

		//delete the old graph
		RevisionManagement.purgeRevisionInformation(graphName);
		String revision0 = RevisionManagement.putGraphUnderVersionControl(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		String revision1 = RevisionManagement.createNewRevision(graphName, triples, null, user, "Initial commit", revision0);
		

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		String revisionB1_0 = RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "First commit to B1", "B1".toLowerCase());
		

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		String revisionB2_0 = RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "First commit to B2", "B2".toLowerCase());

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "Second commit to B1", revisionB1_0);
		

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		String revisionB2_1 = RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "Second commit to B2", revisionB2_0);
		
		// Third commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "Third commit to B2", revisionB2_1);
		
		logger.info("Example graph <" + graphName +"> created.");
		return graphName;
	}
	
	
	
	/**
	 * 
	 * 
	 * @returns graphName
	 * @throws InternalErrorException 
	 */
	public static String createSampleDataSetMergingClasses() throws InternalErrorException {		
		String graphName = "http://test.com/r43ples-dataset-merging-classes";
		/** The initial content file path **/
		String initialContentFilePath = "verification/ExampleGraphClasses_initial.triples";

		// Read initial content from file to string
		String initialContent = ResourceManagement.getContentFromResource(initialContentFilePath);

		//delete the old graph
		RevisionManagement.purgeRevisionInformation(graphName);
		
		String revision0 = RevisionManagement.putGraphUnderVersionControl(graphName);

		// Initial commit
		String revision1 = RevisionManagement.createNewRevision(graphName, initialContent, null, user, "Initial commit", revision0);

		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, "B2");

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
		String revisionB1_0 = RevisionManagement.createNewRevision(graphName, insertT4, null, user, "First commit to B1", "B1".toLowerCase());
		
		// Second commit to B1 - delete sub plant T4
		DatasetGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B1", graphName, revisionB1_0,
				"<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> ?p ?o . \n");

		// First commit to B2 - insert sub plant T4
		String revisionB2_0 = RevisionManagement.createNewRevision(graphName, insertT4, null, user, "First commit to B2", "B2".toLowerCase());
		
		// Second commit to B2 - delete armature V002
		DatasetGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B2", graphName,revisionB2_0,
				"<http://eatld.et.tu-dresden.de/batch/A3A5R01TZU> ?p ?o . \n");
		
		logger.info("Example graph <" + graphName +"> created.");
		return graphName;
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
	 * @returns graphName
	 * @throws InternalErrorException
	 */
	public static String createSampleDataSetRenaming() throws InternalErrorException {
		String graphName = "http://test.com/r43ples-dataset-renaming";

		//delete the old graph
		RevisionManagement.purgeRevisionInformation(graphName);
		String revision0 = RevisionManagement.putGraphUnderVersionControl(graphName);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP1> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";
		String revision1 = RevisionManagement.createNewRevision(graphName, triples, null, user, "Initial commit", revision0);
		
		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, "B1");

		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, "B2");

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n";
		String revisionB1_0 = RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "First commit to B1", "B1".toLowerCase());
		
		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"H\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";
		String revisionB2_0 = RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "First commit to B2", "B2".toLowerCase());
		
		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP1> \"G\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";		
		RevisionManagement.createNewRevision(graphName, triplesInsert, triplesDelete, user, "Second commit to B1", revisionB1_0);
		
		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"I\". \n";
		RevisionManagement.createNewRevision(graphName, triplesInsert, null, user, "Second commit to B2", revisionB2_0);
		logger.info("Example graph <" + graphName +"> created.");
		
		return graphName;
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
	 * @returns graphName
	 * @throws InternalErrorException
	 */
	public static String createSampleDataSetComplexStructure() throws InternalErrorException {
		String graphName = "http://test.com/r43ples-dataset-complex-structure";
		
		//delete the old graph
		RevisionManagement.purgeRevisionInformation(graphName);
		String revision0 = RevisionManagement.putGraphUnderVersionControl(graphName);
		
		// Initial commit
		String triples =  "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		String revision1 = RevisionManagement.createNewRevision(graphName, triples, null, user, "Initial commit", revision0);
		
		// Create a new branch B1
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, "B1");
		
		// Create a new branch B2
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, "B2");
		
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
		RevisionManagement.createTag(graphName, "b2x", "v0.2", "butler", "tag version v0.2");
		
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
		
		logger.info("Example graph <" + graphName +"> created.");
		return graphName;
	}
	
}
