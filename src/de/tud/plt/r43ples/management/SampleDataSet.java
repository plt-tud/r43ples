package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.http.HttpException;

import de.tud.plt.r43ples.webservice.Endpoint;

public class SampleDataSet {
	
	private static Endpoint ep =  new Endpoint();
	
	public static void createSampleDataset1(String graph) throws HttpException, IOException{
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
	
	
	public static void createSampleDataset2(String graph) throws HttpException, IOException{
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
	
	public static void createSampleDataSetMerging(String graphName) throws IOException, HttpException{
		
		// Purge silent example graph
		String query = String.format("DROP SILENT GRAPH <%s>", graphName);
		ep.sparql("text/html", query);
		
		// Create new example graph
		query = String.format("CREATE SILENT GRAPH <%s>", graphName);
		ep.sparql("text/html", query);
		
		// Initial commit
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"Initial commit.\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"0\" %n"
				+ "{"
				+ "  <http://example.com/testS> <http://example.com/testP> \"A\". %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"B\". %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"C\". %n"
				+ "} }", graphName);
		ep.sparql("", query);
		
		// Create a new branch B1
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"Branch B1.\" %n"
				+ "BRANCH GRAPH <%s> REVISION \"1\" TO \"B1\"", graphName);
		ep.sparql("", query);
		
		// Create a new branch B2
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"Branch B2.\" %n"
				+ "BRANCH GRAPH <%s> REVISION \"1\" TO \"B2\"", graphName);
		ep.sparql("", query);
		
		// First commit to B1
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"First commit to B1.\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"B1\" %n"
				+ "{ %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"D\". %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"E\". %n"
				+ "} }"
				+ "DELETE { GRAPH <%s> REVISION \"B1\" %n"
				+ "{ %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"A\". %n"
				+ "} }", graphName, graphName);
		ep.sparql("", query);
		
		// First commit to B2
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"First commit to B2.\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"B2\" %n"
				+ "{ %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"D\". %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"H\". %n"
				+ "} }"
				+ "DELETE { GRAPH <%s> REVISION \"B2\" %n"
				+ "{ %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"C\". %n"
				+ "} }", graphName, graphName);
		ep.sparql("", query);
		
		// Second commit to B1
		query = String.format(""
				+ "USER \"shensel\" %n"
				+ "MESSAGE \"Second commit to B1.\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"B1\" %n"
				+ "{ %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"G\". %n"
				+ "} }"
				+ "DELETE { GRAPH <%s> REVISION \"B1\" %n"
				+ "{ %n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"D\". %n"
				+ "} }", graphName, graphName);
		ep.sparql("", query);
		
		// Second commit to B2
		query = String.format(""
				+ "USER \"shensel\" \n"
				+ "MESSAGE \"Second commit to B2.\" \n"
				+ "INSERT { GRAPH <%s> REVISION \"B2\" \n"
				+ "{ \n"
				+ "  <http://example.com/testS> <http://example.com/testP> \"I\". \n"
				+ "} }", graphName);
		ep.sparql("", query);
	}
	
	
	
	public static void createSampleDataSetMergingClasses() throws IOException, HttpException {
		/** The graph name. **/
		String graphName = "http://exampleGraphClasses";
		/** The user. **/
		String user = "shensel";
		/** The initial content file path **/
		String initialContentFilePath = "resources/verification/ExampleGraphClasses_initial.triples";
		
		// Read initial content from file to string
		String initialContent = readFileToString(initialContentFilePath, StandardCharsets.UTF_8);
		
		// Purge silent example graph
		String query = String.format("DROP SILENT GRAPH <%s>", graphName);
		ep.sparql("", query);
		
		// Create new example graph
		query = String.format("CREATE GRAPH <%s>", graphName);
		ep.sparql("", query);
		
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
	private static void createNewBranch(String user, String message, String graphName, String revision, String branchName) throws IOException, HttpException {
		String query = String.format(""
				+ "USER \"%s\" \n"
				+ "MESSAGE \"%s\" \n"
				+ "BRANCH GRAPH <%s> REVISION \"%s\" TO \"%s\" \n", user, message, graphName, revision, branchName);
		ep.sparql("", query);
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
	private static void executeInsertQuery(String user, String message, String graphName, String revision, String triples) throws IOException, HttpException {
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "INSERT { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "}", user, message, graphName, revision, triples);
		ep.sparql("", query);
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
	private static void executeDeleteQuery(String user, String message, String graphName, String revision, String triples) throws IOException, HttpException {
		String query = String.format(
				  "USER \"%s\" %n"
				+ "MESSAGE \"%s\" %n"
				+ "DELETE { GRAPH <%s> REVISION \"%s\" %n"
				+ "	{ %n"
				+ "	  %s %n"
				+ "	} %n"
				+ "}", user, message, graphName, revision, triples);
		ep.sparql("", query);
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
	private static void executeDeleteWhereQuery(String user, String message, String graphName, String revision, String triples) throws IOException, HttpException {
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
		ep.sparql("", query);
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
	private static void executeInsertDeleteQuery(String user, String message, String graphName, String revision, String triplesInsert, String triplesDelete) throws IOException, HttpException {
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
		ep.sparql("", query);
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
