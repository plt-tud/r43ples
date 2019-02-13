package de.tud.plt.r43ples.dataset;

import de.tud.plt.r43ples.core.MergeTypes;
import de.tud.plt.r43ples.core.R43plesCoreInterface;
import de.tud.plt.r43ples.core.R43plesCoreSingleton;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.iohelper.ResourceManagement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates different sample data sets.
 *
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class SampleDataSet {

	/** The logger. */
	private static Logger logger = LogManager.getLogger(SampleDataSet.class);
	
	/** The user. **/
	private static final String user = "butler";



	public static DataSetGenerationResult createSampleDataset1() throws InternalErrorException  {

		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		DataSetGenerationResult result = new DataSetGenerationResult();
		String graphName = "http://test.com/r43ples-dataset-1";
		result.graphName = graphName;
		RevisionGraph graph = new RevisionGraph(graphName);
		graph.purgeRevisionInformation();

		String masterBranch = "master";

		InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
		String revisionNumber0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-0", revisionNumber0);

		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset1/added-1.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-1.nt"),
				user,
				"test commit message 1",
				masterBranch);
		String revisionNumber1 = commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-1", revisionNumber1);

		UpdateCommit commit2 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset1/added-2.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-2.nt"),
				user,
				"test commit message 2",
				masterBranch);
		String revisionNumber2 = commit2.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-2", revisionNumber2);

		UpdateCommit commit3 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset1/added-3.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-3.nt"),
				user,
				"test commit message 3",
				masterBranch);
		String revisionNumber3 = commit3.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-3", revisionNumber3);

		UpdateCommit commit4 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset1/added-4.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-4.nt"),
				user,
				"test commit message 4",
				masterBranch);
		String revisionNumber4 = commit4.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-4", revisionNumber4);

		UpdateCommit commit5 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset1/added-5.nt"),
				ResourceManagement.getContentFromResource("samples/dataset1/removed-5.nt"),
				user,
				"test commit message 5",
				masterBranch);
		String revisionNumber5 = commit5.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-5", revisionNumber5);
		return result;
	}

	public static DataSetGenerationResult createSampleDataset2() throws InternalErrorException {

		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		DataSetGenerationResult result = new DataSetGenerationResult();
		String graphName = "http://test.com/r43ples-dataset-2";
		result.graphName = graphName;
		RevisionGraph graph = new RevisionGraph(graphName);
		graph.purgeRevisionInformation();

        InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
        String revisionNumber0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-0", revisionNumber0);

		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset2/added-1.nt"),
				ResourceManagement.getContentFromResource("samples/dataset2/removed-1.nt"), user,
				"test commit message 1", "master");
		String revisionNumber1 = commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-1", revisionNumber1);

		UpdateCommit commit2 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset2/added-2.nt"),
				ResourceManagement.getContentFromResource("samples/dataset2/removed-2.nt"), user,
				"test commit message 2", "master");
		String revisionNumber2 = commit2.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-2", revisionNumber2);

		logger.info("SampleDataset2 successfully created");
		return result;
	}

	public static DataSetGenerationResult createSampleDataset3() throws InternalErrorException  {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		DataSetGenerationResult result = new DataSetGenerationResult();
		String graphName = "http://test.com/r43ples-dataset-3";
		result.graphName = graphName;
		RevisionGraph graph = new RevisionGraph(graphName);
		graph.purgeRevisionInformation();

        InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
        String revisionNumber0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-0", revisionNumber0);

		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset3/added-1.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-1.nt"), user,
				"test commit message 1", "master");
		String revisionNumber1 = commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-1", revisionNumber1);
		// Create a new branch B1
		String branchNameB1 = "b1";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revisionNumber1, branchNameB1);

		UpdateCommit commitB1_0 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset3/added-2.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-2.nt"), user,
				"test commit message 2", branchNameB1);
		String revisionB1_0 = commitB1_0.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1-0", revisionB1_0);

		UpdateCommit commit3 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset3/added-3.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-3.nt"), user,
				"test commit message 3", "master");
		String revisionNumber3 = commit3.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-3", revisionNumber3);

		UpdateCommit commit4 = r43plesCore.createUpdateCommit(graphName,
				ResourceManagement.getContentFromResource("samples/dataset3/added-4.nt"),
				ResourceManagement.getContentFromResource("samples/dataset3/removed-4.nt"), user,
				"test commit message 4", "master");
		String revisionNumber4 = commit4.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-4", revisionNumber4);

		ThreeWayMergeCommit mergeCommit5 = r43plesCore.createThreeWayMergeCommit(graphName,
				branchNameB1, "master",
				user, "test commit message 5",
				null, null, MergeTypes.AUTO, false);
		String revisionNumber5 = mergeCommit5.getGeneratedRevision().getRevisionIdentifier();
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
	 */
	public static DataSetGenerationResult createSampleDataSetMerging() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		DataSetGenerationResult result = new DataSetGenerationResult();
		String graphName = "http://test.com/r43ples-dataset-merging";
		result.graphName = graphName;

		RevisionGraph graph = new RevisionGraph(graphName);

		//delete the old graph
		graph.purgeRevisionInformation();

        InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
        String revision0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-0", revision0);

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName, triples, null, user, "Initial commit", "master");
		String revision1 = commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-1", revision1);

		// Create a new branch B1
		String branchNameB1 = "b1";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, branchNameB1);

		// Create a new branch B2
		String branchNameB2 = "b2";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, branchNameB2);

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"A\". \n";
		UpdateCommit commitB1_0 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "First commit to B1", branchNameB1);
		String revisionB1_0 = commitB1_0.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1-0", revisionB1_0);

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		UpdateCommit commitB1_1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "Second commit to B1", branchNameB1);
		String revisionB1_1 = commitB1_1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1-1", revisionB1_1);

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit commitB2_0 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "First commit to B2", branchNameB2);
		String revisionB2_0 = commitB2_0.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b2-0", revisionB2_0);

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		UpdateCommit commitB2_1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "Second commit to B2", branchNameB2);
		String revisionB2_1 = commitB2_1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b2-1", revisionB2_1);

		// Third commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		UpdateCommit commitB2_2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "Third commit to B2", branchNameB2);
		String revisionB2_2 = commitB2_2.getGeneratedRevision().getRevisionIdentifier();
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
	 *
	 */
	public static String createSampleDataSetRebase() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		String graphName = "http://test.com/r43ples-dataset-rebase";

		RevisionGraph graph = new RevisionGraph(graphName);

		//delete the old graph
		graph.purgeRevisionInformation();

        InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
        String revision0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName, triples, null, user, "Initial commit", "master");
		String revision1 = commit1.getGeneratedRevision().getRevisionIdentifier();

		// Create a new branch B1
		String branchNameB1 = "b1";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, branchNameB1);

		// Create a new branch B2
		String branchNameB2 = "b2";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, branchNameB2);

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		UpdateCommit commitB1_0 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "First commit to B1", branchNameB1);
		String revisionB1_0 = commitB1_0.getGeneratedRevision().getRevisionIdentifier();

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		String triplesDelete = "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit commitB2_0 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "First commit to B2", branchNameB2);
		String revisionB2_0 = commitB2_0.getGeneratedRevision().getRevisionIdentifier();

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		UpdateCommit commitB1_1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "Second commit to B1", branchNameB1);
		String revisionB1_1 = commitB1_1.getGeneratedRevision().getRevisionIdentifier();

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		UpdateCommit commitB2_1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "Second commit to B2", branchNameB2);
		String revisionB2_1 = commitB2_1.getGeneratedRevision().getRevisionIdentifier();

		// Third commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		UpdateCommit commitB2_2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "Third commit to B2", branchNameB2);
		String revisionB2_2 = commitB2_2.getGeneratedRevision().getRevisionIdentifier();

		logger.info("Example graph <" + graphName + "> created.");

		return graphName;
	}

	/**
	 *
	 *
	 * @return graphName
	 * @throws InternalErrorException
	 */
	public static String createSampleDataSetMergingClasses() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		String graphName = "http://test.com/r43ples-dataset-merging-classes";

		RevisionGraph graph = new RevisionGraph(graphName);

		/** The initial content file path **/
		String initialContentFilePath = "verification/ExampleGraphClasses_initial.triples";

		// Read initial content from file to string
		String initialContent = ResourceManagement.getContentFromResource(initialContentFilePath);

		//delete the old graph
		graph.purgeRevisionInformation();

        InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
        String revision0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();

		// Initial commit
		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName, initialContent, null, user, "Initial commit", "master");
		String revision1 = commit1.getGeneratedRevision().getRevisionIdentifier();

		// Create a new branch B1
		String branchNameB1 = "b1";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, branchNameB1);

		// Create a new branch B2
		String branchNameB2 = "b2";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, branchNameB2);

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
		UpdateCommit commitB1_0 = r43plesCore.createUpdateCommit(graphName, insertT4, null, user, "First commit to B1", branchNameB1);
		String revisionB1_0 = commitB1_0.getGeneratedRevision().getRevisionIdentifier();

		// Second commit to B1 - delete sub plant T4
		DatasetGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B1", graphName, branchNameB1,
				"<http://eatld.et.tu-dresden.de/batch/A3A5R07QZU> ?p ?o . \n");

		// First commit to B2 - insert sub plant T4
		UpdateCommit commitB2_0 = r43plesCore.createUpdateCommit(graphName, insertT4, null, user, "First commit to B2", branchNameB2);
		String revisionB2_0 = commitB2_0.getGeneratedRevision().getRevisionIdentifier();

		// Second commit to B2 - delete armature V002
		DatasetGenerationManagement.executeDeleteWhereQuery(user, "Second commit to B2", graphName,branchNameB2,
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
	 * @return graphName
	 * @throws InternalErrorException
	 */
	public static String createSampleDataSetRenaming() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		String graphName = "http://test.com/r43ples-dataset-renaming";

		RevisionGraph graph = new RevisionGraph(graphName);

		//delete the old graph
		graph.purgeRevisionInformation();

        InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
        String revision0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();

		// Initial commit
		String triples = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n"
				+ "<http://example.com/testS> <http://example.com/testP1> \"B\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";
		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName, triples, null, user, "Initial commit", "master");
		String revision1 = commit1.getGeneratedRevision().getRevisionIdentifier();

		// Create a new branch B1
		String branchNameB1 = "b1";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B1", graphName, revision1, branchNameB1);

		// Create a new branch B2
		String branchNameB2 = "b2";
		DatasetGenerationManagement.createNewBranch(user, "Create a new branch B2", graphName, revision1, branchNameB2);

		// First commit to B1
		String triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";

		String triplesDelete = "<http://example.com/testS> <http://example.com/testP1> \"A\". \n";
		UpdateCommit commitB1_0 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "First commit to B1", branchNameB1);
		String revisionB1_0 = commitB1_0.getGeneratedRevision().getRevisionIdentifier();

		// First commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n"
				+ "<http://example.com/testS> <http://example.com/testP2> \"H\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"C\". \n";
		UpdateCommit commitB2_0 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "First commit to B2", branchNameB2);
		String revisionB2_0 = commitB2_0.getGeneratedRevision().getRevisionIdentifier();

		// Second commit to B1
		triplesInsert = "<http://example.com/testS> <http://example.com/testP1> \"G\". \n";
		triplesDelete = "<http://example.com/testS> <http://example.com/testP2> \"D\". \n";
		UpdateCommit commitB1_1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "Second commit to B1", branchNameB1);
		String revisionB1_1 = commitB1_1.getGeneratedRevision().getRevisionIdentifier();

		// Second commit to B2
		triplesInsert = "<http://example.com/testS> <http://example.com/testP2> \"I\". \n";
		UpdateCommit commitB2_1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user, "Second commit to B2", branchNameB2);
		String revisionB2_1 = commitB2_1.getGeneratedRevision().getRevisionIdentifier();
		logger.info("Example graph <" + graphName + "> created.");

		return graphName;
	}


	/**
	 * Create an example graph of the following structure:
	 *
	 *                                              ADD: -      ADD: -
	 *                                           +-----3-----------4-----------(Branch B1X)
	 *                                           |  DEL: B      DEL: C
	 *                                           |
	 *                  ADD: D,E       ADD: G    |        ADD: F
	 *               +-----1--------------2------+-----------5-----------------(Branch B1)
	 *               |  DEL: A         DEL: D             DEL: -
	 *               |
	 *               |                              ADD: J      ADD: C
	 *               |                           +-----X-----------X-----------(Branch B2X)
	 *               |                           |  DEL: -      DEL: I
	 *               |                           |
	 *               |  ADD: D,H       ADD: I    |  ADD: K,L    ADD: M
	 *               +-----X--------------X------+-----X-----------X-----------(Branch B2)
	 *               |  DEL: C         DEL: -       DEL: I      DEL: -
	 *               |
	 *               |
	 * ADD: A,B,C    |          ADD: M,N            ADD: P,R,S
	 * ---0----------+-------------X-------------------13-----------------------(MASTER)
	 * DEL: -                   DEL: C              DEL: M
	 *
	 * @return graphName
	 * @throws InternalErrorException
	 */
	public static DataSetGenerationResult createSampleDataSetComplexStructure() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		String graphName = "http://test.com/r43ples-dataset-complex-structure";

		DataSetGenerationResult result = new DataSetGenerationResult();

		// Branch identifier
		String masterBranch = "master";
		String b1Branch = "B1";
		String b2Branch = "B2";
		String b1XBranch = "B1X";
		String b2XBranch = "B2X";

		RevisionGraph graph = new RevisionGraph(graphName);
		result.graphName = graphName;

		//delete the old graph
		graph.purgeRevisionInformation();

		// Initial commit
		String triples =  "<http://example.com/testS> <http://example.com/testP> \"A\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"B\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, triples, null, user, "Initial commit");
		Revision revisionMaster_1 = initialCommit.getGeneratedRevision();
		String revisionIdentifierMaster_1 = revisionMaster_1.getRevisionIdentifier();
		result.revisions.put("master-1", revisionIdentifierMaster_1);

		// Create a new branch B1
		BranchCommit b1Commit = r43plesCore.createBranchCommit(graph, b1Branch, revisionMaster_1, user, "Create a new branch B1");
		// Create a new branch B2
		BranchCommit b2Commit = r43plesCore.createBranchCommit(graph, b2Branch, revisionMaster_1, user, "Create a new branch B2");

		// First commit to B1
		String triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
								+ "<http://example.com/testS> <http://example.com/testP> \"E\". \n";
		String triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"A\". \n";
		UpdateCommit b1Commit1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"First commit to B1", b1Branch);
		String revisionIdentifierB1_1 = b1Commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1-1", revisionIdentifierB1_1);

		// Second commit to B1
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"G\". \n";
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n";
		UpdateCommit b1Commit2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"Second commit to B1", b1Branch);
		Revision revisionB1_2 = b1Commit2.getGeneratedRevision();
		String revisionIdentifierB1_2 = revisionB1_2.getRevisionIdentifier();
		result.revisions.put("b1-2", revisionIdentifierB1_2);

		// Create a new branch B1X
		BranchCommit b1XCommit = r43plesCore.createBranchCommit(graph, b1XBranch, revisionB1_2, user, "Create a new branch B1X");

		// First commit to B1X
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"B\". \n";
		UpdateCommit b1XCommit1 = r43plesCore.createUpdateCommit(graphName, null, triplesDelete, user,"First commit to B1X", b1XBranch);
		String revisionIdentifierB1X_1 = b1XCommit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1X-1", revisionIdentifierB1X_1);

		// Second commit to B1X
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit b1XCommit2 = r43plesCore.createUpdateCommit(graphName, null, triplesDelete, user,"Second commit to B1X", b1XBranch);
		String revisionIdentifierB1X_2 = b1XCommit2.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1X-2", revisionIdentifierB1X_2);

		// Third commit to B1
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"F\". \n";
		UpdateCommit b1Commit3 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user,"Third commit to B1", b1Branch);
		String revisionIdentifierB1_3 = b1Commit3.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b1-3", revisionIdentifierB1_3);

		// First commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"D\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"H\". \n";
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit b2Commit1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"First commit to B2", b2Branch);
		String revisionIdentifierB2_1 = b2Commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b2-1", revisionIdentifierB2_1);

		// Second commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		UpdateCommit b2Commit2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user,"Second commit to B2", b2Branch);
		Revision revisionB2_2 = b2Commit2.getGeneratedRevision();
		String revisionIdentifierB2_2 = revisionB2_2.getRevisionIdentifier();
		result.revisions.put("b2-2", revisionIdentifierB2_2);

		// Create a new branch B2X
		BranchCommit b2XCommit = r43plesCore.createBranchCommit(graph, b2XBranch, revisionB2_2, user, "Create a new branch B2X");

		// First commit to B2X
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"J\". \n";
		UpdateCommit b2XCommit1 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user,"First commit to B2X", b2XBranch);
		Revision revisionB2X_1 = b2XCommit1.getGeneratedRevision();
		String revisionIdentifierB2X_1 = revisionB2X_1.getRevisionIdentifier();
		result.revisions.put("b2X-1", revisionIdentifierB2X_1);

		// Tag the last revision of branch B2X
        TagCommit tagCommit = r43plesCore.createTagCommit(graph, "v0.2", revisionB2X_1, user, "tag version v0.2");

		// Second commit to B2X
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		UpdateCommit b2XCommit2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"Second commit to B2X", b2XBranch);
		String revisionIdentifierB2X_2 = b2XCommit2.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b2X-2", revisionIdentifierB2X_2);

		// Third commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"K\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"L\". \n";
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"I\". \n";
		UpdateCommit b2Commit3 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"Third commit to B2", b2Branch);
		String revisionIdentifierB2_3 = b2Commit3.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b2-3", revisionIdentifierB2_3);

		// Fourth commit to B2
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"M\". \n";
		UpdateCommit b2Commit4 = r43plesCore.createUpdateCommit(graphName, triplesInsert, null, user,"Fourth commit to B2", b2Branch);
		String revisionIdentifierB2_4 = b2Commit4.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("b2-4", revisionIdentifierB2_4);

		// Second commit to master
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"M\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"N\". \n";
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"C\". \n";
		UpdateCommit masterCommit2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"Second commit to MASTER", masterBranch);
		String revisionIdentifierMaster_2 = masterCommit2.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-2", revisionIdentifierMaster_2);

		// Third commit to master
		triplesInsert =	  "<http://example.com/testS> <http://example.com/testP> \"P\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"R\". \n"
						+ "<http://example.com/testS> <http://example.com/testP> \"S\". \n";
		triplesDelete =	  "<http://example.com/testS> <http://example.com/testP> \"M\". \n";
		UpdateCommit masterCommit3 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user,"Third commit to MASTER", masterBranch);
		String revisionIdentifierMaster_3 = masterCommit3.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-3", revisionIdentifierMaster_3);

		logger.info("Example graph <" + graphName +"> created.");
		return result;
	}


	/**
	 * Create an example graph containing a rename class.
	 *
	 * @throws InternalErrorException
	 */
	public static DataSetGenerationResult createSampleDataSetHLCAggregation() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		DataSetGenerationResult result = new DataSetGenerationResult();
		String graphName = "http://test.com/r43ples-dataset-hlc-aggregation";
		result.graphName = graphName;
		String masterBranch = "master";

		RevisionGraph graph = new RevisionGraph(graphName);

		//delete the old graph
		graph.purgeRevisionInformation();

		InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
		String revision0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-0", revision0);

		// Initial commit
		String triples = "<http://example.com/house> a <http://www.w3.org/2000/01/rdf-schema#Class>. \n"
				+ "<http://example.com/house> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://example.com/building>. \n"
				+ "<http://example.com/building> a <http://www.w3.org/2000/01/rdf-schema#Class>. \n";
		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName, triples, null, user, "Initial commit", "master");
		String revision1 = commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-1", revision1);

		// Commit to master
		String triplesInsert = "<http://example.com/myhouse> a <http://www.w3.org/2000/01/rdf-schema#Class>. \n"
				+ "<http://example.com/myhouse> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://example.com/building>. \n"
				+ "<http://example.com/myhouse> <http://example.com/hasDoor> \"BigDoor\". \n";
		String triplesDelete = "<http://example.com/house> a <http://www.w3.org/2000/01/rdf-schema#Class>. \n"
				+ "<http://example.com/house> <http://www.w3.org/2000/01/rdf-schema#subClassOf> <http://example.com/building>. \n";
		UpdateCommit commit2 = r43plesCore.createUpdateCommit(graphName, triplesInsert, triplesDelete, user, "Rename class", masterBranch);
		String revision2 = commit2.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-2", revision2);

		logger.info("Example graph <" + graphName +"> created.");
		return result;
	}


	/**
	 * Create an example graph for the coevolution which uses the ontology elements of createSampleDataSetHLCAggregation().
	 *
	 * @throws InternalErrorException
	 */
	public static DataSetGenerationResult createSampleDataSetCoEvolution() throws InternalErrorException {
		R43plesCoreInterface r43plesCore = R43plesCoreSingleton.getInstance();

		DataSetGenerationResult result = new DataSetGenerationResult();
		String graphName = "http://test.com/r43ples-dataset-coevolution";
		result.graphName = graphName;

		RevisionGraph graph = new RevisionGraph(graphName);

		// delete the old graph
		graph.purgeRevisionInformation();

		InitialCommit initialCommit = r43plesCore.createInitialCommit(graphName, null, null, user, "Create graph");
		String revision0 = initialCommit.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-0", revision0);

		// Initial commit
		String triples = "<http://example-house.com/yellowhouse> a <http://example.com/house>. \n"
				+ "<http://example-house.com/yellowhouse> <http://www.w3.org/2000/01/rdf-schema#label> \"a yellow house\". \n";
		UpdateCommit commit1 = r43plesCore.createUpdateCommit(graphName, triples, null, user, "Initial commit", "master");
		String revision1 = commit1.getGeneratedRevision().getRevisionIdentifier();
		result.revisions.put("master-1", revision1);

		logger.info("Example graph <" + graphName +"> created.");
		return result;
	}
	
}