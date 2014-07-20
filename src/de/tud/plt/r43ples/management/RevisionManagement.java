package de.tud.plt.r43ples.management;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.http.HttpException;
import org.apache.http.auth.AuthenticationException;
import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Resource;

import de.tud.plt.r43ples.webservice.InternalServerErrorException;

/**
 * This class provides methods for interaction with graphs.
 * 
 * @author Stephan Hensel
 * @author Markus Graube
 *
 */
public class RevisionManagement {

	/** The logger. **/
	private static Logger logger = Logger.getLogger(RevisionManagement.class);
	/** The SPARQL prefixes. **/
	private final static String prefix_rmo = "PREFIX rmo: <http://revision.management.et.tu-dresden.de/rmo#> \n";
	private final static String prefixes = "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ prefix_rmo
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n";
	

	
	/**
	 * Create a new revision.
	 * 
	 * 
	 * @param graphName the graph name
	 * @param addedAsNTriples the data set of added triples as N-Triples
	 * @param removedAsNTriples the data set of removed triples as N-Triples
	 * @param user the user name who creates the revision
	 * @param newRevisionNumber the new revision number
	 * @param commitMessage the title of the revision
	 * @param usedRevisionNumber the number of the revision which is used for creation of the new
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void createNewRevision(String graphName, String addedAsNTriples, String removedAsNTriples, String user, String newRevisionNumber, String commitMessage, ArrayList<String> usedRevisionNumber) throws HttpException, IOException {
		logger.info("Start creation of new revision!");
		
		String dateString = getDateString();
		
		// General variables
		String commitUri = graphName+"-commit-" + newRevisionNumber;
		String revisionUri = graphName + "-revision-" + newRevisionNumber;
		String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
		String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;
		String personUri =  getUserName(user);
		
		// Create a new commit (activity)
		String queryContent =	String.format(
				"<%s> a rmo:Commit; " +
				"	prov:wasAssociatedWith <%s>;" +
				"	prov:generated <%s>;" +
				"	dc-terms:title \"%s\";" +
				"	prov:atTime \"%s\". %n",
				commitUri, personUri, revisionUri, commitMessage, dateString);
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String rev = iterator.next();
			queryContent += String.format("<%s> prov:used <%s>. %n", commitUri, graphName + "-revision-" + rev.toString());
		}
		
		// Create new revision
		queryContent += String.format(
				"<%s> a rmo:Revision; " +
				"	rmo:revisionOf <%s>; " +
				"	rmo:deltaAdded <%s>; " +
				"	rmo:deltaRemoved <%s>; " +
				"	rmo:revisionNumber \"%s\". %n"
				,  revisionUri, graphName, addSetGraphUri, removeSetGraphUri, newRevisionNumber);
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String rev = iterator.next();
			queryContent += String.format("<%s> prov:wasDerivedFrom <%s> .",
						revisionUri, graphName + "-revision-"+rev.toString());
		}
		String query = prefixes + String.format("INSERT IN GRAPH <%s> { %s }%n", Config.revision_graph, queryContent) ;
		
		// Move branch to new revision
		String oldRevision = graphName + "-revision-" + usedRevisionNumber.get(0).toString();
		String queryBranch = prefixes + String.format("SELECT ?branch ?graph WHERE{ ?branch a rmo:Branch; rmo:references <%s>; rmo:fullGraph ?graph. }", oldRevision);
		QuerySolution sol = ResultSetFactory.fromXML(TripleStoreInterface.executeQueryWithAuthorization(queryBranch, "XML")).next(); 
		String branchName = sol.getResource("?branch").toString();
		String branchGraph = sol.getResource("?graph").toString();
		query += String.format("DELETE FROM GRAPH <%s> { <%s> rmo:references <%s>. }%n", Config.revision_graph, branchName, oldRevision);
		query += String.format("INSERT INTO GRAPH <%s> { <%s> rmo:references <%s>. }%n", Config.revision_graph, branchName, revisionUri);
		
		// Remove branch from which changes were merged, if available
		if (usedRevisionNumber.size()>1){
			String oldRevision2 = graphName + "-revision-" + usedRevisionNumber.get(1).toString();
			String queryBranch2 = prefixes + String.format("SELECT ?branch ?graph WHERE{ ?branch a rmo:Branch; rmo:references <%s>; rmo:fullGraph ?graph. }", oldRevision2);
			QuerySolution sol2 = ResultSetFactory.fromXML(TripleStoreInterface.executeQueryWithAuthorization(queryBranch2, "XML")).next();
			String removeBranchUri = sol2.getResource("?branch").toString();
			String removeBranchFullGraph = sol2.getResource("?graph").toString();
			query += String.format("DELETE { GRAPH <%s> { <%s> ?p ?o. } } WHERE { GRAPH <%s> { <%s> ?p ?o. }}%n", Config.revision_graph, removeBranchUri, Config.revision_graph, removeBranchUri);
			query += String.format("DROP SILENT GRAPH <%s>%n", removeBranchFullGraph);
		}
		
		// Update full graph of branch
		query += String.format("DELETE FROM GRAPH <%s> {%n %s %n}%n", branchGraph, removedAsNTriples);
		query += String.format("INSERT INTO GRAPH <%s> {%n %s %n}%n", branchGraph, addedAsNTriples);
		
		// Create new graph with delta-added-newRevisionNumber
		logger.info("Create new graph with name " + addSetGraphUri);
		query += String.format("CREATE GRAPH <%s>%n", addSetGraphUri);
		query += String.format("INSERT IN GRAPH <%s> { %s }%n", addSetGraphUri, addedAsNTriples);
		
		// Create new graph with delta-removed-newRevisionNumber
		logger.info("Create new graph with name " + removeSetGraphUri);
		query += String.format("CREATE GRAPH <%s>%n", removeSetGraphUri);
		query += String.format("INSERT IN GRAPH <%s> { %s }%n", removeSetGraphUri, removedAsNTriples);
		

		// Execute queries
		logger.info("Execute all queries.");
		TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
	}
	
	
	/**
	 * Create a new branch
	 * 
	 * 
	 * @param graphName the graph name
	 * @param revisionNumber the revision number where the branch should start
	 * @param branchName name of the new branch
	 * @param user user who performs this commit
	 * @param commitMessage message of this commit
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void createBranch(String graphName, String revisionNumber, String branchName, String user, String commitMessage) throws HttpException, IOException {
		logger.info("Start creation of new branch!");
		
		// General variables
		String dateString = getDateString();
		String commitUri = graphName + "-commit-" + dateString;
		String branchUri = graphName + "-branch-" + branchName;
		String revisionUri = graphName + "-revision-" + revisionNumber;
		String newRevisionNumber = getRevisionNumberForNewBranch(graphName, revisionNumber);
		String newRevisionUri = graphName + "-revision-" + newRevisionNumber;
		String personName =  getUserName(user);
		
		// Create a new commit (activity)
		String queryContent =	String.format(
				"<%s> a rmo:Commit; " +
				"	prov:wasAssociatedWith <%s> ;" +
				"	prov:generated <%s>, <%s> ;" +
				"   prov:used <%s> ;" +
				"	dc-terms:title \"%s\" ;" +
				"	prov:atTime \"%s\" .%n",
				commitUri, personName, branchUri, newRevisionUri, revisionUri, commitMessage, dateString);
		
		// Create new revision
		queryContent += String.format(
				"<%s> a rmo:Revision ; " +
				"	rmo:revisionOf <%s> ; " +
				"   prov:wasDerivedFrom <%s> ;"  +
				"	rmo:revisionNumber \"%s\" .%n"
				,  newRevisionUri, graphName, revisionUri, newRevisionNumber);

		// Create new branch
		queryContent += String.format(
				"<%s> a rmo:Branch, rmo:Reference; "
				+ " rmo:fullGraph <%s>; "
				+ "	rmo:references <%s>; "
				+ "	rdfs:label \"%s\". "
				, branchUri, branchUri, newRevisionUri, branchName);
		
		// Update full graph of branch
		generateFullGraphOfRevision(graphName, revisionNumber, branchUri);
		
		// Execute queries
		String query = prefixes + String.format("INSERT IN GRAPH <%s> { %s }%n", Config.revision_graph, queryContent) ;
		TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
	}
	
	/**
	 * Create a new graph with version control.
	 * Checks whether graph already exists. When graph does not exist a new graph will be created and initial data set will be uploaded.
	 * 
	 * @param graphName the graph name of the new graph
	 * @param dataSetAsNTriples the initial data set as N-Triples
	 * @return boolean created or not
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static boolean createNewGraphWithVersionControl(String graphName, String dataSetAsNTriples) throws HttpException, IOException {
		logger.info("Start creation of new graph under version control with the name " + graphName + "!");

		if (checkGraphExistence(graphName)) {
			logger.info("Graph name " + graphName + " already exists. Graph can not be created.");
			return false;
		} else {
			// Create new graph
			logger.info("Create new graph with name " + graphName);
			TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <" + graphName + ">", "HTML");
			executeINSERT(graphName, dataSetAsNTriples);
			
			// Create new graph with delta-added-0
			String addSetGraphName = graphName + "-delta-added-0";
			logger.info("Create new add-set with name " + addSetGraphName);
			TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <" + addSetGraphName+">", "HTML");
			TripleStoreInterface.executeQueryWithAuthorization("COPY GRAPH <" + graphName +"> TO GRAPH <" + addSetGraphName+">", "HTML");
						
			// Create new empty graph with delta-removed-0
			String removeSetGraphName = graphName + "-delta-removed-0";
			logger.info("Create new delete-set with name " + removeSetGraphName);
			TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <" + removeSetGraphName +">", "HTML");
			
			// Insert information in revision graph
			logger.info("Insert info into revision graph.");	
			String revisionName = graphName + "-revision-0";
			String queryContent = 	String.format(
					"<%s> a rmo:Revision ; " +
					"	rmo:revisionOf <%s> ; " +
					"	rmo:deltaAdded <%s> ; " +
					"	rmo:deltaRemoved <%s> ; " +
					"	rmo:revisionNumber \"%s\" . "
					,  revisionName, graphName, addSetGraphName, removeSetGraphName, 0);
			// Add MASTER branch		
			queryContent += String.format(
					"<%s> a rmo:Master, rmo:Branch, rmo:Reference; "
					+ " rmo:fullGraph <%s>; "
					+ "	rmo:references <%s>; "
					+ "	rdfs:label \"MASTER\". ",
					graphName+"-master", graphName, revisionName);
			
			String queryRevision = String.format(
					"PREFIX rmo: <http://revision.management.et.tu-dresden.de/rmo#> "
					+ "INSERT IN GRAPH <%s> {%s}", Config.revision_graph, queryContent);
			TripleStoreInterface.executeQueryWithAuthorization(queryRevision, "HTML");
			return true;
		}
	}

	
	/**
	 * Create a tag for a specific revision
	 * 
	 * @param graphName the graph name of the existing graph
	 * @param revisionNumber revision number of specified graph which should be tagged
	 * @param tagName label of the new tag
	 * @param user user who performs this commit
	 * @param commitMessage message of this commit
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void createTag(String graphName, String revisionNumber, String tagName, String user, String commitMessage) throws HttpException, IOException {
		logger.info("Create tag for revision " + revisionNumber + " of graph " + graphName);
		
		// General variables
		String dateString = getDateString();
		String commitUri = graphName + "-commit-" + dateString;
		String tagUri = graphName + "-tag-" +tagName;
		String revisionUri = graphName + "-revision-" + revisionNumber;
		String personName =  getUserName(user);
		
		// Create a new commit (activity)
		String queryContent =	String.format(
				"<%s> a rmo:Commit; " +
				"	prov:wasAssociatedWith <%s> ;" +
				"	prov:generated <%s> ;" +
				"   prov:used <%s> ;" +
				"	dc-terms:title \"%s\" ;" +
				"	prov:atTime \"%s\" .%n",
				commitUri, personName, tagUri, revisionUri, commitMessage, dateString);
		
		// Create new branch
		queryContent += String.format(
				"<%s> a rmo:Tag, rmo:Reference; "
				+ " rmo:fullGraph <%s>; "
				+ "	rmo:references <%s>; "
				+ "	rdfs:label \"%s\". "
				, tagUri, tagUri, revisionUri, tagName);
		
		// Update full graph of branch
		generateFullGraphOfRevision(graphName, revisionNumber, tagUri);	
		
		// Execute queries
		String query = prefixes + String.format("INSERT IN GRAPH <%s> { %s }%n", Config.revision_graph, queryContent) ;
		TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");		
	}
	
	
	/**
	 * Put existing graph under version control. existence of graph is not checked.
	 * 
	 * @param graphName the graph name of the existing graph
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void putGraphUnderVersionControl(String graphName) throws HttpException, IOException {
		logger.info("Put existing graph under version control with the name " + graphName);

		// Insert information in revision graph
		logger.info("Insert info into revision graph.");	
		String revisionName = graphName + "-revision-0";
		String queryContent = 	String.format(
				"<%s> a rmo:Revision ;%n" +
				"	rmo:revisionOf <%s> ;%n" +
				"	rmo:revisionNumber \"%s\" .%n"
				,  revisionName, graphName, 0);
		// Add MASTER branch		
		queryContent += String.format(
				"<%s> a rmo:Master, rmo:Branch, rmo:Reference;%n"
				+ " rmo:fullGraph <%s>;%n"
				+ "	rmo:references <%s>;%n"
				+ "	rdfs:label \"master\".%n",
				graphName+"-master", graphName, revisionName);
		
		String queryRevision = String.format(
				"PREFIX rmo: <http://revision.management.et.tu-dresden.de/rmo#> "
				+ "INSERT IN GRAPH <%s> {%s}", Config.revision_graph, queryContent);
		TripleStoreInterface.executeQueryWithAuthorization(queryRevision, "HTML");
	}
	

	/**
	 * Checks if graph exists in triple store. Works only when the graph is not empty.
	 * 
	 * @param graphName the graph name
	 * @return boolean value if specified graph exists and contains at least one triple elsewhere it will return false
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static boolean checkGraphExistence(String graphName) throws HttpException, IOException {
		// Ask whether graph exists
		String query = "ASK { GRAPH <" + graphName + "> {?s ?p ?o} }";
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
		return result.equals("true");
	}
	

	/**
	 * Creates the whole revision from the add and delete sets of the predecessors. Saved in graph tempGraphName.
	 * 
	 * @param graphName the graph name
	 * @param revisionNumber the revision number to build content for
	 * @param tempGraphName the graph where the temporary graph is stored
	 * @return revision content as turtle
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void generateFullGraphOfRevision(String graphName, String revisionName, String tempGraphName) throws HttpException, IOException {
		logger.info("Rebuild whole content of revision " + revisionName + " of graph <" + graphName + "> into temporary graph <" + tempGraphName+">");
		String revisionNumber = getRevisionNumber(graphName, revisionName);
		
		// Create temporary graph
		TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <" + tempGraphName + ">", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <" + tempGraphName + ">", "HTML");
		
		// Create path to revision
		LinkedList<String> list = getRevisionTree(graphName).getPathToRevision(revisionNumber);
		logger.info("Path to revision: " + list.toString());
		
		// Copy branch to temporary graph
		String number = list.pollFirst();
		TripleStoreInterface.executeQueryWithAuthorization("COPY GRAPH <" + RevisionManagement.getFullGraphName(graphName, number) + "> TO GRAPH <" + tempGraphName + ">", "HTML");
		
		// add- und delete-sets koennten auch aus revisionsinformationen gewonnen werden, anstatt aus programmiertem schema => aber langsamer
		
		/** Variant 1
		 * download single changesets and commit them to the temporary graph on the server
		 * **/
//		while (!list.isEmpty()) {
//			// Get the add set
//			String queryCONSTRUCTAdd = 	"CONSTRUCT {?s ?p ?o} " +
//										"FROM <" + graphName + "-delta-added-" + number + "> " +
//										"WHERE {?s ?p ?o}";
//			String resultCONSTRUCTAdd = TripleStoreInterface.executeQueryWithAuthorization(queryCONSTRUCTAdd, "text/plain");
//			
//			// Get the delete set
//			String queryCONSTRUCTDel = 	"CONSTRUCT {?s ?p ?o} " +
//										"FROM <" + graphName + "-delta-removed-" + number + "> " +
//										"WHERE {?s ?p ?o}";
//			String resultCONSTRUCTDel = TripleStoreInterface.executeQueryWithAuthorization(queryCONSTRUCTDel, "text/plain");
//			
//			// Add data to temporary graph
//			TripleStoreInterface.executeQueryWithAuthorization("INSERT DATA INTO <" + tempGraphName + "> { " + resultCONSTRUCTDel + "}", "HTML");
//			// Remove data from temporary graph
//			TripleStoreInterface.executeQueryWithAuthorization("DELETE DATA FROM <" + tempGraphName + "> { " + resultCONSTRUCTAdd + "}", "HTML");
//			
//			number = list.pollFirst();
//		}
		
		/** Variant 2 
		 * 
		 * **/
		while (!list.isEmpty()) {
			// Add data to temporary graph
			TripleStoreInterface.executeQueryWithAuthorization("ADD GRAPH <"+graphName + "-delta-removed-" + number + "> TO GRAPH <" +tempGraphName + ">", "HTML");
			// Remove data from temporary graph (no opposite of SPARQL ADD available)
			TripleStoreInterface.executeQueryWithAuthorization("DELETE { GRAPH <" +tempGraphName + "> { ?s ?p ?o.} } WHERE { GRAPH <"+graphName + "-delta-added-" + number + "> {?s ?p ?o.}}", "HTML");
			
			list.pollFirst();
		}		
		
	}
	
	
	public static String getRevisionNumber(String graphName, String referenceName) throws HttpException, IOException {
		String queryASK = prefix_rmo + String.format(
				"ASK { GRAPH <%s> { ?rev a rmo:Revision; rmo:revisionOf <%s>; rmo:revisionNumber \"%s\" .}}",
				Config.revision_graph, graphName, referenceName);
		String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
		if (resultASK.equals("true"))
			return referenceName;
		else {		
			String query = prefix_rmo + String.format("SELECT ?revisionNumber { GRAPH <%s> {%n"
					+ " ?rev a rmo:Revision; rmo:revisionOf <%s>; rmo:revisionNumber ?revisionNumber .%n"
					+ " ?reference a rmo:Reference; rmo:references ?rev; rdfs:label ?refName.%n"
					+ " FILTER ( UCASE(?refName) = UCASE(\"%s\")). } } ",
					Config.revision_graph, graphName, referenceName);
			String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
			try {
				QuerySolution qs = ResultSetFactory.fromXML(result).next();
				return qs.getLiteral("?revisionNumber").toString();
			}
			catch (Exception e){
				throw new InternalServerErrorException("No Revision or Reference found with identifier: " + referenceName);
			}
		}
	}


	/**
	 * Creates a tree with all revisions (with predecessors and successors and references of tags and branches)
	 * 
	 * @param graphName the graph name
	 * @return the revision tree
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static Tree getRevisionTree(String graphName) throws HttpException, IOException {
		logger.info("Start creation of revision tree of graph " + graphName + "!");

		Tree tree = new Tree();
		//create query
		String queryStringCommits =	prefixes + String.format("SELECT ?uri ?revNumber ?preRevNumber ?fullGraph " +
									"FROM <%s> " +
									"WHERE {" +
									"?uri a rmo:Revision;"
									+ "	rmo:revisionOf <%s>; "
									+ "	rmo:revisionNumber ?revNumber; "
									+ "	prov:wasDerivedFrom ?preRev. "
									+ "?preRev rmo:revisionNumber ?preRevNumber. "
									+ "OPTIONAL { ?branch rmo:references ?uri; rmo:fullGraph ?fullGraph.} "
									+ " }", Config.revision_graph, graphName);
		
		String resultSparql = TripleStoreInterface.executeQueryWithAuthorization(queryStringCommits, "XML");
		
		ResultSet resultsCommits = ResultSetFactory.fromXML(resultSparql);
		
		// Iterate through all commits
		while(resultsCommits.hasNext()) {
			QuerySolution qsCommits = resultsCommits.next();
			String revision = qsCommits.getResource("?uri").toString();
			logger.debug("Found revision: " + revision + ".");
			
			String predecessor = qsCommits.getLiteral("?preRevNumber").getString();
			String generated = qsCommits.getLiteral("?revNumber").getString();
						
			tree.addNode(generated, predecessor);
			Resource t = qsCommits.getResource("?fullGraph");
			if (t!=null){
				String fullGraph = t.getURI();
				if (fullGraph.equals(""))
					tree.addFullGraphOfNode(generated, fullGraph);
			}
			
		}
		
		return tree;
	}
	
	
	/**
	 * Get the MASTER revision number of a graph.
	 * 
	 * @param graphName the graph name
	 * @return the MASTER revision number
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static String getMasterRevisionNumber(String graphName) throws HttpException, IOException {
		logger.info("Get MASTER revision number of graph " + graphName);

		String queryString = String.format("PREFIX rmo: <http://revision.management.et.tu-dresden.de/rmo#>%n" +
				"SELECT ?revisionNumber " +
				"FROM <%s> " +
				"WHERE {" +
				"	?master a rmo:Master; rmo:references ?revision . " +
				"	?revision rmo:revisionNumber ?revisionNumber; rmo:revisionOf <%s> . " +
				"}", Config.revision_graph, graphName);
		String resultSparql = TripleStoreInterface.executeQueryWithAuthorization(queryString, "XML");
		ResultSet results = ResultSetFactory.fromXML(resultSparql);
		QuerySolution qs = results.next();
		return qs.getLiteral("?revisionNumber").getString();
	}
	
	
	/**
	 * Get the next revision number for specified revision number of any branch.
	 * 
	 * @param graphName the graph name
	 * @param revisionNumber the revision number of the last revision
	 * @return the next revision number for specified revision of branch
	 */
	public static String getNextRevisionNumberForLastRevisionNumber(String graphName, String revisionNumber) {
		if (revisionNumber.contains("-")) {
			return revisionNumber.substring(0, revisionNumber.indexOf("-") + 1) + (Integer.parseInt(revisionNumber.substring(revisionNumber.indexOf("-") + 1, revisionNumber.length())) + 1);
		} else {
			return Integer.toString((Integer.parseInt(revisionNumber) + 1));
		}
	}
	
	
	/**
	 * Get the revision number for a new branch.
	 * 
	 * @param graphName the graph name
	 * @param revisionNumber the revision number of the revision which should be branched
	 * @return the revision number of the new branch
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static String getRevisionNumberForNewBranch(String graphName, String revisionNumber) throws HttpException, IOException {
		logger.info("Get the revision number for a new branch of graph " + graphName + " and revision number " + revisionNumber); 		
		String startIdentifierRevisionNumber = "0";
		String checkIdentifierRevisionNumber = "0";
		if (revisionNumber.contains("-")) {
			startIdentifierRevisionNumber = revisionNumber.substring(0, revisionNumber.indexOf("-")) + ".";
			checkIdentifierRevisionNumber = startIdentifierRevisionNumber;
		} else {
			startIdentifierRevisionNumber = revisionNumber;
			checkIdentifierRevisionNumber = startIdentifierRevisionNumber + ".";
		}

		String queryString = prefixes + String.format("SELECT MAX(xsd:integer(STRAFTER(STRBEFORE(xsd:string(?revisionNumber), \"-\"), \"%s.\"))) as ?number %n" +
				"FROM <%s> %n" +
				"WHERE { %n" +
				"	?revision rmo:revisionNumber ?revisionNumber; %n"
				+ "		rmo:revisionOf <%s>. %n" +
				"} ", startIdentifierRevisionNumber, Config.revision_graph, graphName);
		String resultSparql = TripleStoreInterface.executeQueryWithAuthorization(queryString, "XML");
		ResultSet results = ResultSetFactory.fromXML(resultSparql);
		QuerySolution qs = results.next();
		if (qs.getLiteral("?number") != null) {
			if (qs.getLiteral("?number").getString().equals("")) {
				// No max value was found - means that this is the creation of the first branch for this revision
				return startIdentifierRevisionNumber + ".0-0";
			} else {
				if (qs.getLiteral("?number").getInt() == 0) {
					String queryASK = prefixes + String.format("ASK { GRAPH <%s> { "
							+ " <%s> a rmo:Revision . } } ",
							Config.revision_graph, graphName + "-revision-" + checkIdentifierRevisionNumber + "0-0");
					String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
					if (resultASK.equals("false")) {
						return startIdentifierRevisionNumber + ".0-0";
					} else {
						// Max value + 1
						return startIdentifierRevisionNumber + "." + (qs.getLiteral("?number").getInt() + 1) + "-0";
					}
				} else {
					// Max value + 1
					return startIdentifierRevisionNumber + "." + (qs.getLiteral("?number").getInt() + 1) + "-0";
				}
			}
		} else {
			// No max value was found - means that this is the creation of the first branch for this revision
			return startIdentifierRevisionNumber + ".0-0";
		}
	}
	
	
	/**
	 * Set MASTER revision number of a graph and refresh MASTER graph.
	 * 
	 * @param graphName the graph name
	 * @param revisionNumberOfNewHeadRevision the revision number of the new MASTER revision
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void setHeadRevisionNumber(String graphName, String revisionNumberOfNewHeadRevision) throws HttpException, IOException {
		logger.info("Set MASTER revision number of graph " + graphName + " to " + revisionNumberOfNewHeadRevision + "!");
		String query =	String.format(
						"DELETE DATA FROM <%s>" +
						"	{ <http://revision.management.et.tu-dresden.de/graphs/revisions/head> <http://www.w3.org/2002/07/owl#sameAs> <http://revision.management.et.tu-dresden.de/graphs/revisions/revision-" + getMasterRevisionNumber(graphName) + "> . }" +
						"INSERT DATA INTO <%s>" +
						"	{ <http://revision.management.et.tu-dresden.de/graphs/revisions/head> <http://www.w3.org/2002/07/owl#sameAs> <http://revision.management.et.tu-dresden.de/graphs/revisions/revision-" + revisionNumberOfNewHeadRevision + "> . }",
						Config.revision_graph, Config.revision_graph);
		
		TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
		
		// Update content of MASTER graph - therefore create content of new MASTER revision and copy RM-TEMP-graphName to graph
		generateFullGraphOfRevision(graphName, revisionNumberOfNewHeadRevision, "RM-TEMP-" + graphName);
		TripleStoreInterface.executeQueryWithAuthorization("COPY <RM-TEMP-" + graphName + "> TO <" + graphName + ">", "HTML");		
	}
	
	
	/**
	 * Create new merged revision.
	 * 
	 * @param graphName the graph name
	 * @param user the user
	 * @param newRevisionNumber the new revision number
	 * @param revisionNumber1 the revision number of the first revision
	 * @param revisionNumber2 the revision number of the second revision
	 * @param generatedVersionAsNTriples the merged revision as N-Triples
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void createNewMergedRevision(String graphName, String user, String newRevisionNumber, String revisionNumber1, String revisionNumber2, String generatedVersionAsNTriples) throws HttpException, IOException {
		logger.info("Start merging of revisions " + revisionNumber1 + " and " + revisionNumber2 + " of graph " + graphName + "!");
		
		// Create temporary graphs
		TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <RM-MERGE-TEMP-1>", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <RM-MERGE-TEMP-1>", "HTML");
		generateFullGraphOfRevision(graphName, revisionNumber1, "RM-TEMP-" + graphName);
		TripleStoreInterface.executeQueryWithAuthorization("COPY <RM-TEMP-" + graphName+ "> TO <RM-MERGE-TEMP-1>", "HTML");
		
		TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <RM-MERGE-TEMP-2>", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <RM-MERGE-TEMP-2>", "HTML");
		generateFullGraphOfRevision(graphName, revisionNumber2, "RM-TEMP-" + graphName);
		TripleStoreInterface.executeQueryWithAuthorization("COPY <RM-TEMP-" + graphName+ "> TO <RM-MERGE-TEMP-2>", "HTML");
		
		TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <RM-MERGE-TEMP-MERGED>", "HTML");
		TripleStoreInterface.executeQueryWithAuthorization("CREATE GRAPH <RM-MERGE-TEMP-MERGED>", "HTML");
		executeINSERT("RM-MERGE-TEMP-MERGED", generatedVersionAsNTriples);

		// Get all added triples (concatenate all triples which are in MERGED but not in 1 and all triples which are in MERGED but not in 2) 
		String queryAddedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
									"  GRAPH <RM-MERGE-TEMP-MERGED> { ?s ?p ?o }" +
									"  FILTER NOT EXISTS { GRAPH <RM-MERGE-TEMP-1> { ?s ?p ?o } }" +
									" }";
		String addedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");
		
		queryAddedTriples = "CONSTRUCT {?s ?p ?o} WHERE {" +
							"  GRAPH <RM-MERGE-TEMP-MERGED> { ?s ?p ?o }" +
							"  FILTER NOT EXISTS { GRAPH <RM-MERGE-TEMP-2> { ?s ?p ?o } }" +
							" }";		
		addedTriples += TripleStoreInterface.executeQueryWithAuthorization(queryAddedTriples, "text/plain");
		
		// Get all removed triples (concatenate all triples which are in 1 but not in MERGED and all triples which are in 2 but not in MERGED) 
		String queryRemovedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
										"  GRAPH <RM-MERGE-TEMP-1> { ?s ?p ?o }" +
										"  FILTER NOT EXISTS { GRAPH <RM-MERGE-TEMP-MERGED> { ?s ?p ?o } }" +
										" }";
		String removedTriples = TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");
		
		queryRemovedTriples = 	"CONSTRUCT {?s ?p ?o} WHERE {" +
								"  GRAPH <RM-MERGE-TEMP-2> { ?s ?p ?o }" +
								"  FILTER NOT EXISTS { GRAPH <RM-MERGE-TEMP-MERGED> { ?s ?p ?o } }" +
								" }";		
		removedTriples += TripleStoreInterface.executeQueryWithAuthorization(queryRemovedTriples, "text/plain");		
		
		// Create list with the 2 predecessors
		ArrayList<String> usedRevisionNumbers = new ArrayList<String>();
		usedRevisionNumbers.add(revisionNumber1);
		usedRevisionNumbers.add(revisionNumber2);
		
		createNewRevision(graphName, addedTriples, removedTriples, user, newRevisionNumber, "Merged revisions " + revisionNumber1 + " and " + revisionNumber2 + "!", usedRevisionNumbers);
	}
	
	
	/**
	 * Split huge INSERT statements into separate queries of up to ten triple statements.
	 * 
	 * @param graphName the graph name
	 * @param dataSetAsNTriples the data to insert as N-Triples
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void executeINSERT(String graphName, String dataSetAsNTriples) throws HttpException, IOException {

		final int MAX_STATEMENTS = 10;
		String lines[] = dataSetAsNTriples.split("\\.\\s*<");
		int counter = 0;
		String insert = "";
		
		for (int i=0; i<lines.length; i++) {
			String sub = lines[i];
			
			if (!sub.startsWith("<")) {
				sub = "<" + sub;
			}
			if (i < lines.length - 1) {
				sub = sub + ".";
			}
			insert = insert + "%n" + sub;
			counter++;
			if (counter == MAX_STATEMENTS-1) {
				TripleStoreInterface.executeQueryWithAuthorization("INSERT IN GRAPH <" + graphName + "> { " + insert + "}", "HTML");
				counter = 0;
				insert = "";
			}
		}
		TripleStoreInterface.executeQueryWithAuthorization("INSERT IN GRAPH <" + graphName + "> { " + insert + "}", "HTML");
	}
	
	
	/**
	 * Checks if specified revision of the graph is a branch revision, meaning a terminal node in a branch.
	 * @param graphName name of the revisioned graph
	 * @param revisionName revision number or branch or tag name of the graph
	 * @return
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public static boolean isBranch(String graphName, String revisionName) throws HttpException, IOException {
		String queryASK = prefix_rmo + String.format("ASK { GRAPH <%s> { "
				+ " ?rev a rmo:Revision; rmo:revisionOf <%s>. "
				+ " ?ref a rmo:Reference; rmo:references ?rev ."
				+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
				Config.revision_graph, graphName, revisionName, revisionName);
		String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
		return resultASK.equals("true");
	}
	
	
	/**
	 * Returns the name of the full graph of revision of a graph if it is available
	 * @param graphName name of the revisioned graph
	 * @param revisionName revision number or branch or tag name of the graph
	 * @return
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public static String getFullGraphName(String graphName, String revisionName) throws HttpException, IOException {
		String query = prefix_rmo + String.format("SELECT ?graph { GRAPH <%s> { "
				+ " ?rev a rmo:Revision; rmo:revisionOf <%s> . "
				+ " ?ref a rmo:Reference; rmo:references ?rev; rmo:fullGraph ?graph ."
				+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
				Config.revision_graph, graphName, revisionName, revisionName);
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		QuerySolution qs = ResultSetFactory.fromXML(result).next();
		return qs.getResource("?graph").toString();
	}
	
	
	/**
	 * Download complete revision information of R43ples from SPARQL endpoint. Provide only information from specified graph if not null
	 * @param graphName provide only information from specified graph (if not NULL)
	 * @param format serialisation of the RDF model
	 * @return String containing the RDF model in the specified serialisation
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static String getRevisionInformation(String graphName, String format) throws HttpException, IOException {
		String sparqlQuery;
		if (graphName.equals("")) {
			 sparqlQuery = String.format(
					"CONSTRUCT"
				+ "	{ ?s ?p ?o} "
				+ "FROM <%s> "
				+ "WHERE {"
				+ "	?s ?p ?o."
				+ "}",
				Config.revision_graph);
		}
		else {
			sparqlQuery = prefix_rmo + String.format(
					"CONSTRUCT"
					+ "	{ "
					+ "		?revision ?r_p ?r_o. "
					+ "		?reference ?ref_p ?ref_o. "
					+ "		?commit	?c_p ?c_o. "
					+ "	}"
					+ "FROM <%s> "
					+ "WHERE {"
					+ "	?revision rmo:revisionOf <%s>; ?r_p ?r_o. "
					+ " OPTIONAL {?reference rmo:references ?revision; ?ref_p ?ref_o. }"
					+ " OPTIONAL {?commit ?p ?revision; ?c_p ?c_o. }"
					+ "}",
					Config.revision_graph, graphName);
		}
		return TripleStoreInterface.executeQueryWithAuthorization(sparqlQuery, format);
	}
	
	/**
	 * Deletes all information for a specific named graph including all full graphs and information in the R43ples system
	 * @param graph graph to be purged
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public static void purgeGraph(String graph) throws HttpException, IOException {
		logger.info("purge R43ples information.");
		String query = prefixes + String.format(
				"SELECT DISTINCT ?graph FROM <%s> WHERE {"
				+ "		?rev rmo:revisionOf <%s>."
				+ " 	{?rev rmo:deltaAdded ?graph}"
				+ " UNION {?rev rmo:deltaRemoved ?graph}"
				+ " UNION {?ref rmo:references ?rev; rmo:fullGraph ?graph}"
				+ "}", Config.revision_graph, graph);
		String graphInformation = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		ResultSet results = ResultSetFactory.fromXML(graphInformation);		
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			String graphName = qs.getResource("?graph").toString();
			TripleStoreInterface.executeQueryWithAuthorization("DROP SILENT GRAPH <"+graphName+">","XML");
			System.out.println("Graph deleted: " + graphName);
		}
		String queryDelete = String.format(
				prefixes
				+ "DELETE"
				+ " { GRAPH <%s> {?s ?p ?o} }"
				+ "WHERE {"
				+ "	GRAPH <%s> { {"
				+ "		?s a rmo:Revision; rmo:revisionOf <%s>;"
				+ "			?p ?o."
				+ " } UNION {"
				+ " 	?s a rmo:Reference; rmo:references [rmo:revisionOf <%s>];"
				+ "			?p ?o."
				+ "} UNION {"
				+ " 	?s a rmo:Commit; prov:generated [rmo:revisionOf <%s>];"
				+ "			?p ?o."
				+ "} } }", Config.revision_graph, Config.revision_graph, graph, graph, graph);
		TripleStoreInterface.executeQueryWithAuthorization(queryDelete, "XML");
		System.out.println("Graph deleted: " + Config.revision_graph);
	}
	
	
	/**
	 * @param user
	 * @param personName
	 * @param queryContent
	 * @return
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	private static String getUserName(String user)
			throws HttpException, IOException {
		// When user does not already exists - create new
		String personName =  "http://revision.management.et.tu-dresden.de/persons/" + user;
		String queryASK = String.format("PREFIX prov: <http://www.w3.org/ns/prov#> %n"
				+ "ASK { GRAPH <%s>  { "
				+ "<%s> a prov:Person"
				+ "} }", Config.revision_graph, personName);
		String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
		if (resultASK.equals("true")) {
			logger.info("User " + user + " already exists.");
		} else {
			logger.info("User does not exists. Create user " + user + ".");
			String query = prefixes + String.format("INSERT { GRAPH <%s> { <%s> a prov:Person. } }", Config.revision_graph, personName);
			logger.info(query);
			TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
		}
		return personName;
	}
	
	/**
	 * @return
	 */
	private static String getDateString() {
		// Create current time stamp
		Date date= new Date();
		DateFormat df = new SimpleDateFormat( "yyyy'-'MM'-'dd'T'HH:mm:ss" );
		String dateString = df.format(date);
		logger.info("Time stamp created: " + dateString);
		return dateString;
	}
}
