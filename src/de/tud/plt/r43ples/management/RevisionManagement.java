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
	public static final String prefix_rmo = "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n";
	public static final String prefixes = "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n"
			+ prefix_rmo
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n";
	

	
	/**
	 * Put existing graph under version control. existence of graph is not checked.
	 * 
	 * @param graphName the graph name of the existing graph
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void putGraphUnderVersionControl(final String graphName) throws HttpException, IOException {
		logger.info("Put existing graph under version control with the name " + graphName);
	
		// General variables
		int revisionNumber  = 0;
		String revisionUri = graphName + "-revision-" + revisionNumber;
		String addSetGraphUri = graphName + "-delta-added-" + revisionNumber;
		String removeSetGraphUri = graphName + "-delta-removed-" + revisionNumber;
		
		// Create new revision
		String queryContent = String.format(
				"<%s> a rmo:Revision; " +
				"	rmo:revisionOf <%s>; " +
				"	rmo:deltaAdded <%s>; " +
				"	rmo:deltaRemoved <%s>; " +
				"	rmo:revisionNumber \"%s\". %n"
				,  revisionUri, graphName, addSetGraphUri, removeSetGraphUri, revisionNumber);
		
		// Add MASTER branch		
		queryContent += String.format(
				"<%s> a rmo:Master, rmo:Branch, rmo:Reference;%n"
				+ " rmo:fullGraph <%s>;%n"
				+ "	rmo:references <%s>;%n"
				+ "	rdfs:label \"master\".%n",
				graphName + "-master", graphName, revisionUri);
		
		String queryRevision = prefix_rmo + String.format("INSERT IN GRAPH <%s> {%s}", Config.revision_graph, queryContent);
		TripleStoreInterface.executeQueryWithAuthorization(queryRevision, "HTML");
	}
	
	
	/**
	 * Create a new revision.
	 * 
	 * @param graphName the graph name
	 * @param addedAsNTriples the data set of added triples as N-Triples
	 * @param removedAsNTriples the data set of removed triples as N-Triples
	 * @param user the user name who creates the revision
	 * @param commitMessage the title of the revision
	 * @param usedRevisionNumber the number of the revision which is used for creation of the new
	 * @param revisionName the revision name which was specified by the client (revision number, branch name or tag name)
	 * @return new revision number
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static String createNewRevision(final String graphName, final String addedAsNTriples, final String removedAsNTriples, final String user, final String commitMessage, final ArrayList<String> usedRevisionNumber) throws HttpException, IOException {
		logger.info("Start creation of new revision!");
		
		// General variables
		String dateString = getDateString();
		String newRevisionNumber  = getNextRevisionNumber(graphName, usedRevisionNumber.get(0));
		String commitUri = graphName+"-commit-" + newRevisionNumber;
		String revisionUri = graphName + "-revision-" + newRevisionNumber;
		String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
		String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;
		String personUri =  getUserName(user);
		
		// Create a new commit (activity)
		StringBuilder queryContent = new StringBuilder(1000);
		queryContent.append(String.format(
				"<%s> a rmo:Commit; " +
				"	prov:wasAssociatedWith <%s>;" +
				"	prov:generated <%s>;" +
				"	dc-terms:title \"%s\";" +
				"	prov:atTime \"%s\". %n",
				commitUri, personUri, revisionUri, commitMessage, dateString));
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String revUri = getRevisionUri(graphName, iterator.next());
			queryContent.append(String.format("<%s> prov:used <%s>. %n", commitUri, revUri));
		}
		
		// Create new revision
		queryContent.append(String.format(
				"<%s> a rmo:Revision; " +
				"	rmo:revisionOf <%s>; " +
				"	rmo:deltaAdded <%s>; " +
				"	rmo:deltaRemoved <%s>; " +
				"	rmo:revisionNumber \"%s\". %n"
				,  revisionUri, graphName, addSetGraphUri, removeSetGraphUri, newRevisionNumber));
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String revUri = getRevisionUri(graphName, iterator.next());
			queryContent.append(String.format("<%s> prov:wasDerivedFrom <%s> .", revisionUri, revUri));
		}
		String query = prefixes + String.format("INSERT IN GRAPH <%s> { %s }%n", Config.revision_graph, queryContent.toString()) ;
		
		// Move branch to new revision
		String branchIdentifier = usedRevisionNumber.get(0).toString();
		String oldRevisionUri = getRevisionUri(graphName, branchIdentifier);
		
		String queryBranch = prefixes + String.format(""
				+ "SELECT ?branch ?graph "
				+ "FROM <%s>"
				+ "WHERE{"
				+ "	?branch a rmo:Branch; "
				+ "		rmo:references <%s>;"
				+ "		rmo:fullGraph ?graph."
				+ "	{?branch rdfs:label \"%s\"} UNION {<%s> rmo:revisionNumber \"%s\"}"
				+ "}", Config.revision_graph, oldRevisionUri, branchIdentifier, oldRevisionUri, branchIdentifier);
		QuerySolution sol = ResultSetFactory.fromXML(TripleStoreInterface.executeQueryWithAuthorization(queryBranch, "XML")).next(); 
		String branchName = sol.getResource("?branch").toString();
		String branchGraph = sol.getResource("?graph").toString();
			
		query += String.format("DELETE FROM GRAPH <%s> { <%s> rmo:references <%s>. }%n", Config.revision_graph, branchName, oldRevisionUri);
		query += String.format("INSERT IN GRAPH <%s> { <%s> rmo:references <%s>. }%n", Config.revision_graph, branchName, revisionUri);
		
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
		
//		// Update full graph of branch
//		query += String.format("DELETE FROM GRAPH <%s> {%n %s %n}%n", branchGraph, removedAsNTriples);
//		query += String.format("INSERT IN GRAPH <%s> {%n %s %n}%n", branchGraph, addedAsNTriples);
//		
//		// Create new graph with delta-added-newRevisionNumber
//		logger.info("Create new graph with name " + addSetGraphUri);
//		query += String.format("CREATE SILENT GRAPH <%s>%n", addSetGraphUri);
//		query += String.format("INSERT IN GRAPH <%s> { %s }%n", addSetGraphUri, addedAsNTriples);
//		
//		// Create new graph with delta-removed-newRevisionNumber
//		logger.info("Create new graph with name " + removeSetGraphUri);
//		query += String.format("CREATE SILENT GRAPH <%s>%n", removeSetGraphUri);
//		query += String.format("INSERT IN GRAPH <%s> { %s }%n", removeSetGraphUri, removedAsNTriples);
	
		// Execute queries
		logger.info("Execute all queries updating the revision graph, full graph and change sets");
		TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
		
		// Update full graph of branch
		TripleStoreInterface.executeQueryWithAuthorization(String.format("DELETE FROM GRAPH <%s> {%n %s %n}%n", branchGraph, removedAsNTriples));
		RevisionManagement.executeINSERT(branchGraph, addedAsNTriples);
		
		// Create new graph with delta-added-newRevisionNumber
		logger.info("Create new graph with name " + addSetGraphUri);
		TripleStoreInterface.executeQueryWithAuthorization(String.format("CREATE SILENT GRAPH <%s>%n", addSetGraphUri));
		RevisionManagement.executeINSERT(addSetGraphUri, addedAsNTriples);
		
		// Create new graph with delta-removed-newRevisionNumber
		logger.info("Create new graph with name " + removeSetGraphUri);
		TripleStoreInterface.executeQueryWithAuthorization(String.format("CREATE SILENT GRAPH <%s>%n", removeSetGraphUri));
		RevisionManagement.executeINSERT(removeSetGraphUri, removedAsNTriples);
		
		return newRevisionNumber;
	}


	/**
	 * Create a new reference which can be a branch or a tag
	 * 
	 * @param referenceType type of reference. can be "branch" or "tag"
	 * @param graphName the graph name
	 * @param revisionNumber the revision number where the reference should start or be attached to
	 * @param newReferenceName name of the new branch
	 * @param user user who performs this reference generation
	 * @param message message describing intent of this command
	 * @throws IOException 
	 * @throws IdentifierAlreadyExistsException 
	 * @throws AuthenticationException 
	 */
	public static void createReference(final String referenceType, final String graphName, final String revisionNumber, final String newReferenceName, final String user, final String message) throws HttpException, IOException, IdentifierAlreadyExistsException {
		logger.info("Start creation of new " + referenceType);
		
		// Check branch existence
		if (checkReferenceNameExistence(graphName, newReferenceName)) {
			// Branch name is already in use
			logger.error("The reference name '" + newReferenceName + "' is for the graph '" + graphName + "' already in use.");
			throw new IdentifierAlreadyExistsException("The reference name '" + newReferenceName + "' is for the graph '" + graphName + "' already in use.");
		} else {
			// General variables
			String dateString = getDateString();
			String commitUri = graphName + "-commit-" + referenceType + "-" + newReferenceName;
			String referenceUri = graphName + "-" + referenceType + "-" + newReferenceName;
			String referenceTypUri = (referenceType.equals("tag")) ? "rmo:Tag" : "rmo:Branch";
			String revisionUri = getRevisionUri(graphName, revisionNumber);
			String personUri =  getUserName(user);
				
			// Create a new commit (activity)
			String queryContent =	String.format(
					"<%s> a rmo:ReferenceCommit; " +
					"	prov:wasAssociatedWith <%s> ;" +
					"	prov:generated <%s> ;" +
					"   prov:used <%s> ;" +
					"	dc-terms:title \"%s\" ;" +
					"	prov:atTime \"%s\" .%n",
					commitUri, personUri, referenceUri, revisionUri, message, dateString);

			// Create new branch
			queryContent += String.format(
					"<%s> a %s, rmo:Reference; "
					+ " rmo:fullGraph <%s>; "
					+ "	prov:wasDerivedFrom <%s>; "
					+ "	rmo:references <%s>; "
					+ "	rdfs:label \"%s\". "
					, referenceUri, referenceTypUri, referenceUri, revisionUri, revisionUri, newReferenceName);
			
			// Update full graph of branch
			generateFullGraphOfRevision(graphName, revisionNumber, referenceUri);
			
			// Execute queries
			String query = prefixes + String.format("INSERT IN GRAPH <%s> { %s }", Config.revision_graph, queryContent) ;
			TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
		}		
	}
	

	/**
	 * Checks if graph exists in triple store. Works only when the graph is not empty.
	 * 
	 * @param graphName the graph name
	 * @return boolean value if specified graph exists and contains at least one triple elsewhere it will return false
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static boolean checkGraphExistence(final String graphName) throws HttpException, IOException {
		String query = "ASK { GRAPH <" + graphName + "> {?s ?p ?o} }";
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
		return result.equals("true");
	}
	

	/**
	 * Creates the whole revision from the add and delete sets of the predecessors. Saved in graph tempGraphName.
	 * 
	 * @param graphName the graph name
	 * @param revisionName revision number or revision name to build content for
	 * @param tempGraphName the graph where the temporary graph is stored
	 * @throws IOException 
	 * @throws AuthenticationException 
	 */
	public static void generateFullGraphOfRevision(final String graphName, final String revisionName, final String tempGraphName) throws HttpException, IOException {
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
		
		// add- und delete-sets could be extracted from revision tree information
		// hard coded variant is faster
		
		while (!list.isEmpty()) {
			// Add data to temporary graph
			TripleStoreInterface.executeQueryWithAuthorization("ADD GRAPH <"+graphName + "-delta-removed-" + number + "> TO GRAPH <" +tempGraphName + ">", "HTML");
			// Remove data from temporary graph (no opposite of SPARQL ADD available)
			TripleStoreInterface.executeQueryWithAuthorization("DELETE { GRAPH <" +tempGraphName + "> { ?s ?p ?o.} } WHERE { GRAPH <"+graphName + "-delta-added-" + number + "> {?s ?p ?o.}}", "HTML");
			
			number = list.pollFirst();
		}		
		
	}
	
	
	
	/**
	 * Get the revision URI for a given reference name or revision number
	 * 
	 * @param graphName the graph name
	 * @param revisionIdentifier reference name or revision number
	 * @return URI of identified revision
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String getRevisionUri(final String graphName, final String revisionIdentifier) throws HttpException, IOException {
		String query = prefix_rmo + String.format(
				"SELECT ?rev WHERE { GRAPH <%s> {"
				+ "{?rev a rmo:Revision; rmo:revisionOf <%s>; rmo:revisionNumber \"%s\" .}"
				+ "UNION {?rev a rmo:Revision; rmo:revisionOf <%s>. ?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\" .}"
				+ "} }",
				Config.revision_graph, graphName, revisionIdentifier, graphName, revisionIdentifier);
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				logger.error("Identifier not unique: " + revisionIdentifier);
				throw new InternalServerErrorException("Identifier not unique: " + revisionIdentifier);
			}
			return qs.getResource("?rev").toString();
		} else {
			logger.error("No Revision or Reference found with identifier: " + revisionIdentifier);
			throw new InternalServerErrorException("No Revision or Reference found with identifier: " + revisionIdentifier);
		}
	}
	
	
	/**
	 * Get the reference URI for a given reference name or revision number
	 * 
	 * @param graphName the graph name
	 * @param referenceIdentifier reference name or revision number
	 * @return URI of identified revision
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String getReferenceUri(final String graphName, final String referenceIdentifier) throws HttpException, IOException {
		String query = prefix_rmo + String.format(
				"SELECT ?ref WHERE { GRAPH <%s> {"
				+ "	?ref a rmo:Reference; rmo:references ?rev."
				+ " ?rev a rmo:Revision; rmo:revisionOf <%s>."
				+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref rdfs:label \"%s\" .}"
				+ "} }",
				Config.revision_graph, graphName, referenceIdentifier, referenceIdentifier);
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalServerErrorException("Identifier is not unique for specified graph name: " + referenceIdentifier);
			}	
			return qs.getResource("?ref").toString();
		} else {
			throw new InternalServerErrorException("No Revision or Reference found with identifier: " + referenceIdentifier);
		}
	}
	
	
	/**
	 * Get the revision number of a given reference name.
	 * 
	 * @param graphName the graph name
	 * @param referenceName the reference name
	 * @return the revision number of given reference name
	 * @throws HttpException
	 * @throws IOException
	 */
	public static String getRevisionNumber(final String graphName, final String referenceName) throws HttpException, IOException {
		String query = prefix_rmo + String.format(
				"SELECT ?revNumber WHERE { GRAPH <%s> {"
				+ "	?rev a rmo:Revision; rmo:revisionNumber ?revNumber; rmo:revisionOf <%s>."
				+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\".}"
				+ "} }",
				Config.revision_graph, graphName, referenceName, referenceName);
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		ResultSet resultSet = ResultSetFactory.fromXML(result);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalServerErrorException("Identifier not unique: " + referenceName);
			}
			return qs.getLiteral("?revNumber").toString();
		} else {
			throw new InternalServerErrorException("No Revision or Reference found with identifier: " + referenceName);
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
	public static Tree getRevisionTree(final String graphName) throws HttpException, IOException {
		logger.info("Start creation of revision tree of graph " + graphName + "!");

		Tree tree = new Tree();
		//create query
		String queryRevisions =	prefixes + String.format("SELECT ?uri ?revNumber ?fullGraph " +
									"FROM <%s> " +
									"WHERE {" +
									"?uri a rmo:Revision;"
									+ "	rmo:revisionOf <%s>; "
									+ "	rmo:revisionNumber ?revNumber."
									+ "OPTIONAL { ?branch rmo:references ?uri; rmo:fullGraph ?fullGraph.}"
									+ " }", Config.revision_graph, graphName);
		String resultSparql = TripleStoreInterface.executeQueryWithAuthorization(queryRevisions, "XML");
		ResultSet resultsCommits = ResultSetFactory.fromXML(resultSparql);
		while(resultsCommits.hasNext()) {
			QuerySolution qsCommits = resultsCommits.next();
			String revision = qsCommits.getResource("?uri").toString();
			String revisionNumber = qsCommits.getLiteral("?revNumber").getString();
			String fullGraph = "";
			if (qsCommits.getResource("?fullGraph")!=null)
				fullGraph = qsCommits.getResource("?fullGraph").toString();
			
			logger.debug("Found revision: " + revision + ".");
			tree.addNode(revisionNumber, revision, fullGraph);
		}
		
		String queryRevisionConnection = prefixes + String.format("SELECT ?revNumber ?preRevNumber " +
				"FROM <%s> " +
				"WHERE {" +
				"?rev a rmo:Revision;"
				+ "	rmo:revisionOf <%s>; "
				+ "	rmo:revisionNumber ?revNumber; "
				+ "	prov:wasDerivedFrom ?preRev. "
				+ "?preRev rmo:revisionNumber ?preRevNumber. "
				+ " }", Config.revision_graph, graphName);
		String resultSparqlConnection = TripleStoreInterface.executeQueryWithAuthorization(queryRevisionConnection, "XML");
		ResultSet resultRevConnection = ResultSetFactory.fromXML(resultSparqlConnection);
		while(resultRevConnection.hasNext()) {
			QuerySolution qsCommits = resultRevConnection.next();
			String revision = qsCommits.getLiteral("?revNumber").toString();
			String preRevision = qsCommits.getLiteral("?preRevNumber").toString();
			tree.addEdgeNode(revision, preRevision);
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
	public static String getMasterRevisionNumber(final String graphName) throws HttpException, IOException {
		logger.info("Get MASTER revision number of graph " + graphName);

		String queryString = prefix_rmo + String.format(
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
	  * Checks whether the referenced reference has at least one own revision.
	  * 
	  * @param graphName the graph name
	  * @param referenceIdentifier the reference identifier which was specified by the client (branch name or tag name)
	  * @return true when it is an empty branch
	  * @throws HttpException 
	  * @throws IOException 
	  */
	 private static boolean isBranchEmpty(final String graphName, final String referenceIdentifier) throws IOException, HttpException {
	 	String referenceUri = getReferenceUri(graphName, referenceIdentifier);
 		String queryASKBranch = prefixes + String.format("ASK { GRAPH <%s> { "
 				+ " <%s> rmo:references ?rev; prov:wasDerivedFrom ?rev ."
 				+ " }} ",
 				Config.revision_graph, referenceUri);
 		String resultASKBranch = TripleStoreInterface.executeQueryWithAuthorization(queryASKBranch, "HTML");
 		return resultASKBranch.equals("true");
	 }
	 
	
	 
	public static String getNextRevisionNumber(final String graphName, final String revisionIdentifier) throws HttpException, IOException{
		String revisionNumber = getRevisionNumber(graphName, revisionIdentifier);
		if (isBranchEmpty(graphName, revisionIdentifier)) {
			return getRevisionNumberForNewBranch(graphName, revisionNumber);
		} else {
			return getNextRevisionNumberForLastRevisionNumber(graphName, revisionNumber);
		}
	}
	
	
	/**
	 * Get the next revision number for specified revision number of any branch.
	 * 
	 * @param graphName the graph name
	 * @param revisionNumber the revision number of the last revision
	 * @return the next revision number for specified revision of branch
	 */
	public static String getNextRevisionNumberForLastRevisionNumber(final String graphName, final String revisionNumber) {
		if (revisionNumber.contains("-")) {
			return revisionNumber.substring(0, revisionNumber.indexOf('-') + 1) + (Integer.parseInt(revisionNumber.substring(revisionNumber.indexOf('-') + 1, revisionNumber.length())) + 1);
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
	public static String getRevisionNumberForNewBranch(final String graphName, final String revisionNumber) throws HttpException, IOException {
		logger.info("Get the revision number for a new branch of graph " + graphName + " and revision number " + revisionNumber); 		
		int ii = 0;
		String newRevisionNumber;
		final int MAX_TRIES = 99;
		while (ii < MAX_TRIES) {
			newRevisionNumber = revisionNumber + "." + ii + "-0";
			String queryASK = prefixes + String.format(""
					+ "ASK { GRAPH <%s> { "
					+ " ?rev a rmo:Revision;"
					+ "		rmo:revisionOf <%s>;"
					+ "		rmo:revisionNumber \"%s\"}}",
					Config.revision_graph, graphName, newRevisionNumber);
			String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
			if (resultASK.equals("false")) {
				return newRevisionNumber;
			}
			ii++;
		}
		return null;
	}
	
	
	
	/**
	 * Split huge INSERT statements into separate queries of up to fifty triple statements.
	 * 
	 * @param graphName the graph name
	 * @param dataSetAsNTriples the data to insert as N-Triples
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static void executeINSERT(final String graphName, final String dataSetAsNTriples) throws HttpException, IOException {

		final int MAX_STATEMENTS = 200;
		String[] lines = dataSetAsNTriples.split("\\.\\s*<");
		int counter = 0;
		StringBuilder insert = new StringBuilder();
		
		for (int i=0; i < lines.length; i++) {
			String sub = lines[i];
			if (!sub.equals("") && !sub.startsWith("#")) {
				if (!sub.startsWith("<")) {
					sub = "<" + sub;
				}
				if (i < lines.length - 1) {
					sub = sub + ".";
				}
				insert.append('\n').append(sub);
				counter++;
				if (counter == MAX_STATEMENTS-1) {
					TripleStoreInterface.executeQueryWithAuthorization("INSERT IN GRAPH <" + graphName + "> { " + insert + "}", "HTML");
					counter = 0;
					insert = new StringBuilder();
				}
			}
		}
		TripleStoreInterface.executeQueryWithAuthorization("INSERT IN GRAPH <" + graphName + "> { " + insert + "}", "HTML");
	}
	
	
	
	/**
	 * Returns the name of the full graph of revision of a graph if it is available.
	 * @param graphName name of the revisioned graph
	 * @param revisionName revision number or branch or tag name of the graph
	 * @return name of the full graph of a revision of a graph
	 * @throws AuthenticationException
	 * @throws IOException
	 */
	public static String getFullGraphName(final String graphName, final String revisionName) throws HttpException, IOException {
		String query = prefixes + String.format("SELECT ?graph { GRAPH <%s> { "
				+ " ?rev a rmo:Revision; rmo:revisionOf <%s> . "
				+ " ?ref a rmo:Reference; rmo:references ?rev; rmo:fullGraph ?graph ."
				+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
				Config.revision_graph, graphName, revisionName, revisionName);
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		if (ResultSetFactory.fromXML(result).hasNext()) {
			QuerySolution qs = ResultSetFactory.fromXML(result).next();
			return qs.getResource("?graph").toString();
		} else {
			return null;
		}
	}
	
	
	/**
	 * Download complete revision information of R43ples from SPARQL endpoint. 
	 * Provide only information from specified graph if not null
	 * @param graphName provide only information from specified graph (if not NULL)
	 * @param format serialization of the RDF model
	 * @return String containing the RDF model in the specified serialization
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String getRevisionInformation(final String graphName, final String format) throws HttpException, IOException {
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
		} else {
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
	 * Get revised graphs in R43ples.
	 * @param format serialization of the response
	 * @return String containing the SPARQL response in specified format
	 * @throws IOException 
	 * @throws HttpException 
	 */
	public static String getRevisedGraphs(final String format) throws HttpException, IOException {
		String sparqlQuery = prefix_rmo + String.format(""
				+ "SELECT DISTINCT ?graph "
				+ "FROM <%s> "
				+ "WHERE {"
				+ "	?rev rmo:revisionOf ?graph."
				+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterface.executeQueryWithAuthorization(sparqlQuery, format);
	}
	
	
	/**
	 * Deletes all information for a specific named graph including all full graphs and information in the R43ples system.
	 * @param graph graph to be purged
	 * @throws HttpException
	 * @throws IOException
	 */
	public static void purgeGraph(final String graph) throws HttpException, IOException {
		logger.info("Purge graph "+graph+" and all related R43ples information.");
		// Drop all full graphs as well as add and delete sets which are related to specified graph 
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
			logger.debug("Graph deleted: " + graphName);
		}
		// Remove information from revision graph		
		String queryDelete = prefixes + String.format(
				"DELETE { GRAPH <%s> {?s ?p ?o} } "
				+ "WHERE {"
				+ "  GRAPH <%s> {"
				+ "    {?s a rmo:Revision; rmo:revisionOf <%s>;	?p ?o.}"
				+ "    UNION {?s a rmo:Reference; rmo:references [rmo:revisionOf <%s>]; ?p ?o.}"
				+ "    UNION {?s a rmo:Commit; prov:generated [rmo:revisionOf <%s>]; ?p ?o.}"
				+ "} }", Config.revision_graph, Config.revision_graph, graph, graph, graph);
		TripleStoreInterface.executeQueryWithAuthorization(queryDelete, "XML");
	}
	
	
	/**
	 * @param user name as string
	 * @return URI of person
	 * @throws HttpException
	 * @throws IOException
	 */
	private static String getUserName(final String user)
			throws HttpException, IOException {
		// When user does not already exists - create new

		String query = prefixes + String.format(
				"SELECT ?personUri { GRAPH <%s>  { "
				+ "?personUri a prov:Person;"
				+ "  rdfs:label \"%s\"."
				+ "} }", Config.revision_graph, user);
		String result = TripleStoreInterface.executeQueryWithAuthorization(query, "XML");
		ResultSet results = ResultSetFactory.fromXML(result);		
		if (results.hasNext()) {
			logger.debug("User " + user + " already exists.");
			QuerySolution qs = results.next();
			return qs.getResource("?personUri").toString();
		} else {
			String personUri =  "http://eatld.et.tu-dresden.de/persons/" + user;
			logger.debug("User does not exists. Create user " + personUri + ".");
			query = prefixes + String.format("INSERT IN GRAPH <%s> { <%s> a prov:Person; rdfs:label \"%s\". }", Config.revision_graph, personUri, user);
			TripleStoreInterface.executeQueryWithAuthorization(query, "HTML");
			return personUri;
		}
	}
	
	
	/**
	 * @return current date formatted as xsd:DateTime
	 */
	private static String getDateString() {
		// Create current time stamp
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH:mm:ss");
		String dateString = df.format(date);
		logger.info("Time stamp created: " + dateString);
		return dateString;
	}
	
	
	/**
	 * Check whether the branch name is already used by specified graph name.
	 * 
	 * @param graphName the corresponding graph name
	 * @param referenceName the branch name to check
	 * @return true when branch already exists elsewhere false
	 * @throws HttpException 
	 * @throws IOException 
	 */
	private static boolean checkReferenceNameExistence(final String graphName, final String referenceName) throws IOException, HttpException {
		String queryASK = prefixes + String.format("ASK { GRAPH <%s> { "
				+ " ?ref a rmo:Reference; rdfs:label \"%s\". "
				+ " ?ref rmo:references ?rev ."
				+ " ?rev rmo:revisionOf <%s> ."
				+ " }} ",
				Config.revision_graph, referenceName, graphName);
		String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
		return resultASK.equals("true");
	}
	
	
	/**
	 * Checks if specified revision of the graph is a branch revision, meaning a terminal node in a branch.
	 * @param graphName name of the revisioned graph
	 * @param identifier revision number or branch or tag name of the graph
	 * @return true if specified revision of the graph is a branch
	 * @throws HttpException
	 * @throws IOException
	 */
	public static boolean isBranch(final String graphName, final String identifier) throws HttpException, IOException {
		String queryASK = prefixes + String.format("ASK { GRAPH <%s> { "
				+ " ?rev a rmo:Revision; rmo:revisionOf <%s>. "
				+ " ?ref a rmo:Reference; rmo:references ?rev ."
				+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
				Config.revision_graph, graphName, identifier, identifier);
		String resultASK = TripleStoreInterface.executeQueryWithAuthorization(queryASK, "HTML");
		
		return resultASK.equals("true");
	}
	
}
