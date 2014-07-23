package de.tud.plt.r43ples.adminInterface;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.ClientProtocolException;
import org.apache.jena.atlas.logging.Log;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.management.RevisionManagement;
import de.tud.plt.r43ples.management.Tree;
import de.tud.plt.r43ples.management.TripleStoreInterface;
import de.tud.plt.r43ples.visualisation.YEDExport;

public class AdministratorInterface {

	private static Logger logger = Logger.getLogger(AdministratorInterface.class);
	
	
	public static void main(String[] args) throws ConfigurationException, HttpException, IOException {
		Config.readConfig("r43ples.conf");
		TripleStoreInterface.init(Config.sparql_endpoint, Config.sparql_user, Config.sparql_password);
		start();
	}
	
	/**
	 * Start the administration interface.
	 * @throws IOException 
	 * @throws HttpException
	 */
	public static void start() throws HttpException, IOException {
		System.out.println("\nAdministration interface!");
		System.out.println("=========================\n");
		
		while (true) {
			String commandValue = readCommandDataFromSystemIN(
					"Command options:\n"
					+ " c - create a new revision from turtle file\n"
					+ " e - export revision to turtle file\n"
					+ " g - generate revision graph (refreshed yEd export)\n"
					+ " t - tag a revision\n"
					+ " i - import a new graph under revision control\n"
					+ " m - merge two revisions\n"
					+ " l - list all revisioned graphs\n"
					+ " p - purge all R43ples data \n"
					+ " d - purge all R43ples data (excluding MASTER revision) \n"
					+ " x - put eXisting graph under version control \n"
					+ " j - prepare for jMeter permormance test \n"
					+ " q - quit \n \nEnter command: ");
			
			System.out.println("============================================================\n");
			switch (commandValue) {
			case "c":
				System.out.println("Create a new revision from turtle file.");
				createNewRevisionFromTurtleFile();
				break;
			case "e":
				System.out.println("Export revision to turtle file.");
				exportRevisionToTurtleFile();
				break;
			case "g":
				System.out.println("Generate resfreshed yEd export.");
				generateRefreshedYEDExport();
				break;
			case "t":
				System.out.println("Create a tag for a revision.");
				createTag();
				break;
			case "i":
				System.out.println("Import a new graph under version control.");
				createNewGraphUnderVersionControl();
				break;
			case "m":
				System.out.println("Merge two revisions.");
				mergeRevisionAI();
				break;
			case "l":
				System.out.println("List all revisioned graphs.");
				listAllRevisionedGraphs();
				break;
			case "p":
				System.out.println("Purge all R43ples information");
				purgeR43plesInformation(false);
				break;
			case "d":
				System.out.println("Delete all R43ples information (excluding MASTER revisions)");
				purgeR43plesInformation(true);
				break;
			case "x":
				System.out.println("Put existing graph under version control.");
				putExistingGraphUnderVersionControl();
				break;
			case "j":
				System.out.println("Prepare for jMeter performance test.");
				prepareJmeterPerformanceTest();
				break;
			case "q":
				System.out.println("Quit.");
				System.exit(0);
				break;
			default:
				break;
			}
		}
	}
	

	private static void putExistingGraphUnderVersionControl() throws HttpException, IOException {
		String graphName = getUserInputExistingGraph();
		RevisionManagement.putGraphUnderVersionControl(graphName);
	}

	private static void createTag() throws HttpException, IOException {
		String graphName = getUserInputExistingGraph();
		String revisionNumber = getUserInputRevisionNumber();
		
		System.out.println("Please enter tag name:");			
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String tagName = br.readLine();
		
		try {
			RevisionManagement.createTag(graphName, revisionNumber, tagName, "Admin", "commited from Admin Interface");
		} catch (IdentifierAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
	}

	/**
	 * Create new revision from turtle file.
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws AuthenticationException 
	 */
	private static void createNewRevisionFromTurtleFile() throws HttpException, IOException {
		
		String graphName = "";
		try {
			graphName = getUserInputExistingGraph();
		} catch (Exception e1) {
			createNewRevisionFromTurtleFile();
		}
		
		// Get the revision number
		System.out.println("Please enter the revision number:");
		
		String revisionNumberString = "";
		String revisionNumber = "0";
		BufferedReader brRN = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			revisionNumberString = brRN.readLine();
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			createNewRevisionFromTurtleFile();
			return;
		}
		try {
			revisionNumber = revisionNumberString;
		} catch (NumberFormatException e) {
			// TODO Create check if valid revision number was entered
			System.out.println("Entered value was not a valid integer value. Please try again.");
			createNewRevisionFromTurtleFile();
			return;
		}
		
		// Get the file
		System.out.println("Please enter the path to the turtle file:");
		
		String filePath = "";
		
		BufferedReader brFP = new BufferedReader(new InputStreamReader(System.in));
		
		String modelStringAsNTriples = "";
		
		try {
			filePath = brFP.readLine();
			
			Model model = new DiffResolveTool().readFile(filePath);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			model.write(os, "N-TRIPLES");
			
			try {
				modelStringAsNTriples = new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.out.println("There was a UnsupportedEncodingException. Please try again.");
				createNewRevisionFromTurtleFile();
			}
			
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			createNewRevisionFromTurtleFile();
			return;
		}
		
		// Create temporary graph <RM-INOUT-TEMP>
		TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <RM-INOUT-TEMP>", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <RM-INOUT-TEMP>", "HTML");
					
		// Add data to temporary graph
		RevisionManagement.executeINSERT("RM-INOUT-TEMP", modelStringAsNTriples);
		
		RevisionManagement.generateFullGraphOfRevision(graphName,revisionNumberString, "RM-TEMP-" + graphName);
			    
		// Get all added triples
		String queryAddedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
									"  GRAPH <RM-INOUT-TEMP> { ?s ?p ?o }" +
									"  FILTER NOT EXISTS { GRAPH <RM-TEMP-" + graphName + "> { ?s ?p ?o } }" +
									" }";
		
		String addedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");

		// Get all removed triples
		String queryRemovedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
										"  GRAPH <RM-TEMP-" + graphName + "> { ?s ?p ?o }" +
										"  FILTER NOT EXISTS { GRAPH <RM-INOUT-TEMP> { ?s ?p ?o } }" +
										" }";
		
		String removedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");
		
		ArrayList<String> list = new ArrayList<String>();
		list.add(revisionNumber);
		
		// Create new revision
		RevisionManagement.createNewRevision(graphName, addedTriples, removedTriples, "Administrator", "Created new revision from turtle file.", list, list.get(0));
	}

	/**
	 * @return existing graph selected by the user
	 * @throws AuthenticationException 
	 * @throws Exception 
	 */
	private static String getUserInputExistingGraph()
			throws IOException, HttpException {
		// Get the graph name
		System.out.println("Please enter the graph name:");
		
		String graphName = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		graphName = br.readLine();
		if (!RevisionManagement.checkGraphExistence(graphName)) {
			System.out.println("Entered graph name does not exists. Please try again.");
			throw new IOException("Entered graph name does not exists. Please try again.");
		}
		return graphName;
	}
	
	/**
	 * @return revision number selected by the user
	 * @throws AuthenticationException 
	 * @throws Exception 
	 */
	private static String getUserInputRevisionNumber()
			throws IOException, HttpException {
		// Get the revision number
		System.out.println("Please enter the revision number:");
		
		String revisionNumberString = "";
		String revisionNumber = "0";
		BufferedReader brRN = new BufferedReader(new InputStreamReader(System.in));
		
		revisionNumberString = brRN.readLine();
		revisionNumber = revisionNumberString;
		return revisionNumber;
	}
	
	/**
	 * Export revision to turtle file.
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws AuthenticationException 
	 */
	private static void exportRevisionToTurtleFile() throws HttpException, IOException {
		
		// Get the graph name
		System.out.println("Please enter the graph name:");
		
		String graphName = "";
	
		try {
			graphName = getUserInputExistingGraph();
			
		} catch (IOException | AuthenticationException e) {
			System.out.println("There was a IOException. Please try again.");
			exportRevisionToTurtleFile();
			return;
		}
		
		// Get the revision number
		System.out.println("Please enter the revision number:");
		
		String revisionNumberString = "";
		String revisionNumber = "0";
		BufferedReader brRN = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			revisionNumberString = brRN.readLine();
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			exportRevisionToTurtleFile();
			return;
		}
		try {
			revisionNumber = revisionNumberString;
		} catch (NumberFormatException e) {
			// TODO Create check if valid revision number was entered
			System.out.println("Entered value was not a valid integer value. Please try again.");
			exportRevisionToTurtleFile();
			return;
		}
		
		// Get the file
		System.out.println("Please enter the path to the result turtle file:");
		
		String filePath = "";
		
		BufferedReader brFP = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			filePath = brFP.readLine();			
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			exportRevisionToTurtleFile();
			return;
		}
		
		RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, "RM-TEMP-" + graphName);
		
		// Get the whole graph content
		String queryCONSTRUCTContent = 	"CONSTRUCT {?s ?p ?o} " +
										"FROM <RM-TEMP-" + graphName + "> " +
										"WHERE {?s ?p ?o}";
		
		
		// Write data to file
		try {
			String resultCONSTRUCTDel = TripleStoreInterface.executeQueryWithAuthorization(queryCONSTRUCTContent, "Turtle");
			FileUtils.writeStringToFile(new File(filePath), resultCONSTRUCTDel);
		} catch (IOException | AuthenticationException e) {
			System.out.println("There was a IOException. Please try again.");
			exportRevisionToTurtleFile();
			return;
		}
	}	
	
	
	/**
	 * Create new graph under version control.
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws AuthenticationException 
	 */
	private static void createNewGraphUnderVersionControl() throws HttpException, IOException {
		
		// Get the graph name
		System.out.println("Please enter the graph name:");
		
		String graphName = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			graphName = br.readLine();

			if (RevisionManagement.checkGraphExistence(graphName)) {
				System.out.println("Entered graph already exists. Please try again.");
				createNewGraphUnderVersionControl();
				return;
			}
			
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			createNewGraphUnderVersionControl();
			return;
		}
		
		// Get the file
		System.out.println("Please enter the path to the file with the initial triples as turtle:");
		
		String filePath = "";
		
		BufferedReader brFP = new BufferedReader(new InputStreamReader(System.in));
		
		String modelStringAsNTriples = "";
		
		try {
			filePath = brFP.readLine();
			
			Model model = new DiffResolveTool().readFile(filePath);
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			model.write(os, "N-TRIPLES");
			
			try {
				modelStringAsNTriples = new String(os.toByteArray(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				System.out.println("There was a UnsupportedEncodingException. Please try again.");
				createNewGraphUnderVersionControl();
			}
			
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			createNewGraphUnderVersionControl();
			return;
		}

		// create new graph with version control
		if (RevisionManagement.createNewGraphWithVersionControl(graphName, modelStringAsNTriples))
			System.out.println("Successfully created");
		else
			System.out.println("Error");
	}
	
	
	/**
	 * Merge two revisions.
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 * @throws AuthenticationException 
	 */
	private static void mergeRevisionAI() throws HttpException, IOException {
		
		DiffResolveTool diffResolveTool = new DiffResolveTool();
		
		// Get the graph name
		System.out.println("Please enter the graph name:");
		
		String graphName = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			graphName = br.readLine();

			if (RevisionManagement.checkGraphExistence(graphName)) {
				System.out.println("Entered graph name does not exists. Please try again.");
				mergeRevisionAI();
				return;
			}
			
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			mergeRevisionAI();
			return;
		}
		
		// Specify the ontology to use
		System.out.println("Please enter the ontology used by " + graphName + ":");
		
		String ontologyName = "";
		
		BufferedReader brO = new BufferedReader(new InputStreamReader(System.in));

		try {
			ontologyName=brO.readLine();
			
			if (RevisionManagement.checkGraphExistence(ontologyName)) {
				System.out.println("Entered graph name does not exists. Please try again.");
				mergeRevisionAI();
				return;
			}
			
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			mergeRevisionAI();
			return;
		}
		
		// Get the first revision number
		System.out.println("Please enter the first revision number:");
		
		String revisionNumber1 = "";
		String firstRevisionNumber = "0";
		BufferedReader brRN1 = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			revisionNumber1 = brRN1.readLine();
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			mergeRevisionAI();
			return;
		}
		try {
			firstRevisionNumber = revisionNumber1;
		} catch (NumberFormatException e) {
			// TODO Create check if valid revision number was entered
			System.out.println("Entered value was not a valid integer value. Please try again.");
			mergeRevisionAI();
			return;
		}
		
		// Get the second revision number
		System.out.println("Please enter the second revision number:");
		
		String revisionNumber2 = "";
		String secondRevisionNumber = "0";
		BufferedReader brRN2 = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			revisionNumber2 = brRN2.readLine();
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			mergeRevisionAI();
			return;
		}
		try {
			secondRevisionNumber = revisionNumber2;
		} catch (NumberFormatException e) {
			// TODO Create check if valid revision number was entered
			System.out.println("Entered value was not a valid integer value. Please try again.");
			mergeRevisionAI();
			return;
		}
		
		System.out.println("Start merging revision " + firstRevisionNumber + " and revision " + secondRevisionNumber + " of graph " + graphName + " with the help of ontology " + ontologyName +"!");
		
		
		// Get the graph content
		// First revision
		RevisionManagement.generateFullGraphOfRevision(graphName, firstRevisionNumber, graphName + "-r43ples-temp");
		String firstRevisionContentTurtle = TripleStoreInterface.executeQueryWithAuthorization("CONSTRUCT {?s ?p ?o} FROM <" + graphName + "-r43ples-temp" + "> WHERE {?s ?p ?o}", "Turtle");
		// Second revision
		RevisionManagement.generateFullGraphOfRevision(graphName, secondRevisionNumber, graphName + "-r43ples-temp");
		String secondRevisionContentTurtle = TripleStoreInterface.executeQueryWithAuthorization("CONSTRUCT {?s ?p ?o} FROM <" + graphName + "-r43ples-temp" + "> WHERE {?s ?p ?o}", "Turtle");
		// Ontology
		String queryOntology = 	"CONSTRUCT {?s ?p ?o} " +
								"FROM <" + ontologyName + "> " +
								"WHERE {?s ?p ?o}";
		String ontologyContentTurtle = TripleStoreInterface.executeQueryWithAuthorization(queryOntology, "Turtle");

		// Generate models
		StringReader srR1 = new StringReader(firstRevisionContentTurtle);
		Model modelR1 = ModelFactory.createDefaultModel();
		modelR1.read(srR1, null, "TURTLE");
		
		StringReader srR2 = new StringReader(secondRevisionContentTurtle);
		Model modelR2 = ModelFactory.createDefaultModel();
		modelR2.read(srR2, null, "TURTLE");
		
		StringReader srO = new StringReader(ontologyContentTurtle);
		Model modelO = ModelFactory.createDefaultModel();
		modelO.read(srO, null, "TURTLE");
		
		
		// Generate structure
		diffResolveTool.generateStructureModelFromOntology(modelO, ontologyName);

		// Check for differences
		HashMap<String, Differences> checkedForDifferencesResult = diffResolveTool.checkForDifferences(modelR1, modelR2);
		
		Model mergedModel = null;
		
		//output if there are differences or not
		if (diffResolveTool.differenceInAllElements(checkedForDifferencesResult) == true) {
			System.out.println("There are differences between the data sets!");
			System.out.println("============================================\n");
			//start navigation
			Executer exe = new Executer(checkedForDifferencesResult, diffResolveTool, modelR1, modelR2);
			exe.printListOfAllElements();
			//TODO extend to review first whether all conflicts were solved
			mergedModel = exe.getMergedGraph1();
		} else {
			System.out.println("There are no differences between the data sets!");
			System.out.println("===============================================\n");
			mergedModel = modelR1;
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		mergedModel.write(os, "N-TRIPLES");
		String mergedModelStringAsNTriples = "";
		try {
			mergedModelStringAsNTriples = new String(os.toByteArray(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			System.out.println("There was a UnsupportedEncodingException the merged revision could not be created!");
		}
		Log.debug(AdministratorInterface.class, mergedModelStringAsNTriples);
		
//		Tree tree = RevisionManagement.createRevisionTree(graphName);
		// TODO Auf welchem Branch soll die merged Revision abgelegt werden????
		// Zurzeit kann nicht gemerged werden, da das zuerst spezifiziert werden muss
		// die Methode, die derzeit f�r die n�chste Revisionsnummer genutzt wird, kann an dieser Stelle auch nicht verwendet werden, da theoretisch beliebige Revisionen gemerged werden k�nnen
		// Die Methode erstellt aber ausgehend von der letzten Revision des branches die neue Revisionsnummer
		//String newRevisionNumber = RevisionManagement.getLastRevisionNumber(graphName)+1;
//		String newRevisionNumber = RevisionManagement.getLastRevisionNumber(graphName)+1;
//		
//		RevisionManagement.createNewMergedRevision(graphName, "Adminstrator", newRevisionNumber, firstRevisionNumber, secondRevisionNumber, mergedModelStringAsNTriples);
//		System.out.println("Merged revision with revision number " + newRevisionNumber + " was created");
	}

	
	/**
	 * List all revisioned graphs.
	 * 
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	private static void listAllRevisionedGraphs() throws HttpException, IOException {
		String graphInformation = TripleStoreInterface.executeQueryWithAuthorization("SELECT DISTINCT ?graph FROM <r43ples-revisions> WHERE {?s <http://eatld.et.tu-dresden.de/rmo#revisionOf> ?graph}", "XML");

		ResultSet results = ResultSetFactory.fromXML(graphInformation);
		
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			System.out.println(qs.getResource("?graph").toString());
		}
		
	};
	
	
	
	/**
	 * Refresh yEd export file.
	 * @throws AuthenticationException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	private static void generateRefreshedYEDExport() throws HttpException, IOException {
		// Get the graph name
		System.out.println("Please enter the graph name:");
		
		String graphName = "";
		try {
			graphName=getUserInputExistingGraph();
		} catch (IOException e) {
			System.out.println("There was a IOException. Please try again.");
			generateRefreshedYEDExport();
			return;
		}
		
		Tree tree = RevisionManagement.getRevisionTree(graphName);
		String filePath = Config.yed_filepath;

		try {
			new YEDExport().writeYEDDataToFile(tree, RevisionManagement.getMasterRevisionNumber(graphName), filePath);
		} catch (IOException e) {
			System.out.println("There was a IOException while refreshing yEd.");
			return;
		}
		System.out.println("yEd export file was refreshed.");
		
	}
	
	
	/**
	 * purge all information that was stored from R43ples. Use with care. You will loose a lot of information.
	 *  drops also temporary graphs
	 * @param keepMaster if true keeps the MASTER revision
	 * @throws AuthenticationException 
	 * @throws IOException 
	 */
	private static void purgeR43plesInformation(boolean keepMaster) throws HttpException, IOException {
		logger.info("purge R43ples information.");
		String query =
				"PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> "
				+ "SELECT DISTINCT ?graph FROM <"+Config.revision_graph+"> WHERE {";
		if (keepMaster)
			query += "	{ ?branch rmo:fullGraph ?graph MINUS {?branch a rmo:Master} }";
		else
			query += "	{ ?branch rmo:fullGraph ?graph }";
		query +=
				" UNION {?rev rmo:deltaAdded ?graph}"
				+ " UNION {?rev rmo:deltaRemoved ?graph}"
				+ " UNION { GRAPH ?graph {?s ?p ?o.} FILTER REGEX(str(?graph), 'RM-TEMP') }"
				+ "}";
		String graphInformation = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		ResultSet results = ResultSetFactory.fromXML(graphInformation);		
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			String graphName = qs.getResource("?graph").toString();
			TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <"+graphName+">","XML");
			System.out.println("Graph deleted: " + graphName);
		}
		TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <"+Config.revision_graph+">","XML");
		System.out.println("Graph deleted: " + Config.revision_graph);
	}
	
	
	private static void prepareJmeterPerformanceTest() throws HttpException, IOException {
		logger.info("Prepare jMeter performance test.");
		// ClassLoader in order to load files from /resources/ directory
		final ClassLoader loader = AdministratorInterface.class.getClassLoader();
		int[] changesizes = {10,20,30,40,50,60,70,80,90,100};
		int[] datasizes = {100,1000,10000,100000,1000000};
		final int REVISIONS = 20;
		
		for (int j = 0; j < datasizes.length; j++) {
			int datasize = datasizes[j];
			for (int i = 0; i < changesizes.length; i++) {
				int changesize = changesizes[i];
				String graphName = "dataset-"+datasize+"-"+changesize;
				TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <" + graphName +">", "HTML");
				URL fileDataset = loader.getResource("dataset/dataset-"+datasize+".nt");
				String dataSetAsNTriples = FileUtils.readFileToString(new File(fileDataset.getFile()));
				RevisionManagement.createNewGraphWithVersionControl(graphName, dataSetAsNTriples);
				for (int revision = 1; revision <= REVISIONS; revision++) {
					URL fileName = loader.getResource("dataset/addset-"+changesize+"-"+revision+".nt");
					String addedAsNTriples = FileUtils.readFileToString(new File(fileName.getFile()));
					ArrayList<String> list = new ArrayList<>();
					list.add(Integer.toString(revision-1));
					RevisionManagement.createNewRevision(graphName, addedAsNTriples, "", "test", "test creation", list, list.get(0));
				}
			}
		}
	}
	
	

	/**
	 * Reads command data from System.in.
	 * 
	 * @param toWrite the message to write
	 * @return entered value
	 */
	private static String readCommandDataFromSystemIN(String toWrite) {
		System.out.print(toWrite);
		
		String inputData = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		try {
			inputData=br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println();
		
		return inputData;
	}
}
