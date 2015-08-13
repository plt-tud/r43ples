package de.tud.plt.r43ples.management;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.server.UID;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.util.FileUtils;

import de.tud.plt.r43ples.exception.IdentifierAlreadyExistsException;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.merging.management.StrategyManagement;
import de.tud.plt.r43ples.revisionTree.Revision;
import de.tud.plt.r43ples.revisionTree.Tree;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

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
	public static final String prefixes = 
			  "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms: <http://purl.org/dc/terms/> \n" 
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd: <http://eatld.et.tu-dresden.de/sdd#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n";


	/**
	 * Put existing graph under version control. Existence of graph is not checked.
	 * 
	 * @param graphName
	 *            the graph name of the existing graph
	 */
	public static String putGraphUnderVersionControl(final String graphName) {
		logger.info("Put existing graph under version control with the name " + graphName);

		// General variables
		//call: create graph response
		//int revisionNumber = 0;
		UID revisionNumberUid = new UID();
		String revisionNumber = revisionNumberUid.toString();
		
		while (checkExistUID(revisionNumber, graphName)){
			UID newRevisionNumberUid = new UID();
			revisionNumber = newRevisionNumberUid.toString();		
		}
		
		
		String revisionUri = graphName + "-revision-" + revisionNumber;
		String addSetGraphUri = graphName + "-delta-added-" + revisionNumber;
		String removeSetGraphUri = graphName + "-delta-removed-" + revisionNumber;

		// Create new revision
		String queryContent = String.format(
				  "<%s> a rmo:Revision ; %n"
				+ "	rmo:revisionOf <%s> ; %n"
				+ "	rmo:deltaAdded <%s> ; %n"
				+ "	rmo:deltaRemoved <%s> ; %n"
				+ "	rmo:revisionNumber \"%s\" ; %n"
				+ "	rmo:revisionOfBranch <%s> . %n"
				,  revisionUri, graphName, addSetGraphUri, removeSetGraphUri, revisionNumber, graphName + "-master");
		
		// Add MASTER branch		
		queryContent += String.format(
				"<%s> a rmo:Master, rmo:Branch, rmo:Reference;%n"
				+ " rmo:fullGraph <%s>;%n"
				+ "	rmo:references <%s>;%n"
				+ "	rdfs:label \"master\".%n",
				graphName + "-master", graphName, revisionUri);
		
		// Add graph element
		// TODO Currently to every created graph the default SDD is referenced - provide possibility to choose SDD
		queryContent += String.format(
				"<%s> a rmo:Graph ;%n"
				+ "sddo:hasDefaultSDD sdd:defaultSDD .", 
				graphName);

		String queryRevision = prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", Config.revision_graph, queryContent);
		
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRevision);
		
		return revisionNumber;
	}
	
	
	/**
	 * Create a new revision.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param addedAsNTriples
	 *            the data set of added triples as N-Triples
	 * @param removedAsNTriples
	 *            the data set of removed triples as N-Triples
	 * @param user
	 *            the user name who creates the revision
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision 
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String createNewRevision(final String graphName, final String addedAsNTriples, final String removedAsNTriples,
			final String user, final String commitMessage, final String usedRevisionNumber) throws InternalErrorException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(usedRevisionNumber);
		return createNewRevision(graphName, addedAsNTriples, removedAsNTriples, user, commitMessage, list);
	}
	
	/**
	 * Create a new revision.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param addSetGraphUri
	 *           uri of the data set of added triples as N-Triples
	 * @param removeSetGraphUri
	 *           uri of the data set of removed triples as N-Triples
	 * @param user
	 *            the user name who creates the revision
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision 
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	
	//create new revision with patch with addedUri and removedUri
	public static String createNewRevisionWithPatch(final String graphName, final String addSetGraphUri, final String removeSetGraphUri,
			final String user, final String commitMessage, final String usedRevisionNumber) throws InternalErrorException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(usedRevisionNumber);
		return createNewRevisionWithPatch(graphName, addSetGraphUri, removeSetGraphUri, user, commitMessage, list);
	}

	/**
	 * Create a new revision with multiple prior revisions
	 * 
	 * @param graphName
	 *            the graph name
	 * @param addedAsNTriples
	 *            the data set of added triples as N-Triples
	 * @param removedAsNTriples
	 *            the data set of removed triples as N-Triples
	 * @param user
	 *            the user name who creates the revision
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision 
	 *            (for creation of merged maximal two revision are  allowed
	 *            - the first revision in array list specifies the branch where the merged revision will be created)
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String createNewRevision(final String graphName, final String addedAsNTriples,
			final String removedAsNTriples, final String user, final String commitMessage,
			final ArrayList<String> usedRevisionNumber) throws InternalErrorException {
		logger.info("Start creation of new revision!");

		// General variables
		String newRevisionNumber = getNextRevisionNumber(graphName, usedRevisionNumber.get(0));
		String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
		String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;
		String referenceGraph = getReferenceGraph(graphName, usedRevisionNumber.get(0));

		// Add Meta Information
		addMetaInformationForNewRevision(graphName, user, commitMessage, usedRevisionNumber,
				newRevisionNumber, addSetGraphUri, removeSetGraphUri);

		// Update full graph of branch
		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
			RevisionManagement.executeDELETE(referenceGraph, removedAsNTriples);
		}
		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
			RevisionManagement.executeINSERT(referenceGraph, addedAsNTriples);
		}

		// Create new graph with delta-added-newRevisionNumber
		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
			logger.info("Create new graph with name " + addSetGraphUri);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n",
				addSetGraphUri));
			RevisionManagement.executeINSERT(addSetGraphUri, addedAsNTriples);
		}

		// Create new graph with delta-removed-newRevisionNumber
		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
			logger.info("Create new graph with name " + removeSetGraphUri);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n",
					removeSetGraphUri));
			RevisionManagement.executeINSERT(removeSetGraphUri, removedAsNTriples);
		}
		
		// Remove branch from which changes were merged, if available
//		if (usedRevisionNumber.size() > 1) {
//			String oldRevision2 = graphName + "-revision-" + usedRevisionNumber.get(1).toString();
//			String queryBranch2 = prefixes
//					+ String.format(
//							"SELECT ?branch ?graph WHERE{ ?branch a rmo:Branch; rmo:references <%s>; rmo:fullGraph ?graph. }",
//							oldRevision2);
//			QuerySolution sol2 = ResultSetFactory.fromXML(
//					TripleStoreInterfaceSingleton.get().executeQueryWithAuthorization(queryBranch2, "XML")).next();
//			String removeBranchUri = sol2.getResource("?branch").toString();
//			String removeBranchFullGraph = sol2.getResource("?graph").toString();
//			String query = String.format(
//					"DELETE { GRAPH <%s> { <%s> ?p ?o. } } WHERE { GRAPH <%s> { <%s> ?p ?o. }}%n",
//					Config.revision_graph, removeBranchUri, Config.revision_graph, removeBranchUri);
//			query += String.format("DROP SILENT GRAPH <%s>%n", removeBranchFullGraph);
//			TripleStoreInterfaceSingleton.get().executeQueryWithAuthorization(query);
//		}

		return newRevisionNumber;
	}

	
	
	
	/**
	 * Create a new revision with multiple prior revisions
	 * 
	 * @param graphName
	 *            the graph name
	 * @param addSetGraphUri
	 *            uri of the data set of added triples as N-Triples
	 * @param removeSetGraphUri
	 *            uri of the data set of removed triples as N-Triples
	 * @param user
	 *            the user name who creates the revision
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision 
	 *            (for creation of merged maximal two revision are  allowed
	 *            - the first revision in array list specifies the branch where the merged revision will be created)
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String createNewRevisionWithPatch(final String graphName, final String addSetGraphUri,
			final String removeSetGraphUri, final String user, final String commitMessage,
			final ArrayList<String> usedRevisionNumber) throws InternalErrorException {
		logger.info("Start creation of new revision!");

		// General variables
		String newRevisionNumber = getNextRevisionNumber(graphName, usedRevisionNumber.get(0));
		//String addSetGraphUri = graphName + "-delta-added-" + newRevisionNumber;
		//String removeSetGraphUri = graphName + "-delta-removed-" + newRevisionNumber;
		String referenceGraph = getReferenceGraph(graphName, usedRevisionNumber.get(0));

		// Add Meta Information
		addMetaInformationForNewRevision(graphName, user, commitMessage, usedRevisionNumber,
				newRevisionNumber, addSetGraphUri, removeSetGraphUri);
		
		//get Triplelist of addedset and deletedset 
		LinkedList<String> addedTripleList =  StrategyManagement.createAddedOrRemovedTripleSet(addSetGraphUri);
		LinkedList<String> removedTripleList = StrategyManagement.createAddedOrRemovedTripleSet(removeSetGraphUri);
		
		String addedAsNTriples = "";
		String removedAsNTriples = "";
		
		for(String triple : addedTripleList) { 
			addedAsNTriples = addedAsNTriples + triple + ". \n";
		}
		
		for(String triple : removedTripleList) { 
			removedAsNTriples = removedAsNTriples + triple + ". \n";
		}
		
		logger.info("rebase added triples: " + addedAsNTriples);
		logger.info("rebase removed triples: " + removedAsNTriples);
		
		// Update full graph of branch
		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
			RevisionManagement.executeDELETE(referenceGraph, removedAsNTriples);
		}
		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
			RevisionManagement.executeINSERT(referenceGraph, addedAsNTriples);
		}


		return newRevisionNumber;
	}

	/**
	 * Add meta information to R43ples revision graph for a new revision.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param user
	 *            the user name who creates the revision
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision
	 * @param addSetGraphUri
	 * 			  name of the graph which holds the add set
	 * @param removeSetGraphUri
	 *            name of the graph which holds the delete set
	 * @throws InternalErrorException 
	 */
	public static void addMetaInformationForNewRevision(final String graphName, final String user,
			final String commitMessage, final ArrayList<String> usedRevisionNumber,
			final String newRevisionNumber, final String addSetGraphUri, final String removeSetGraphUri) throws InternalErrorException {
		String dateString = getDateString();
		String personUri = getUserName(user);
		String revisionUri = graphName + "-revision-" + newRevisionNumber;
		String commitUri = graphName + "-commit-" + newRevisionNumber;
		String branchUri = getBranchUri(graphName, usedRevisionNumber.get(0));

		// Create a new commit (activity)
		StringBuilder queryContent = new StringBuilder(1000);
		queryContent.append(String.format("<%s> a rmo:Commit; " + "	prov:wasAssociatedWith <%s>;"
				+ "	prov:generated <%s>;" + "	dc-terms:title \"%s\";" + "	prov:atTime \"%s\". %n", commitUri,
				personUri, revisionUri, commitMessage, dateString));
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String revUri = getRevisionUri(graphName, iterator.next());
			queryContent.append(String.format("<%s> prov:used <%s>. %n", commitUri, revUri));
		}

		// Create new revision
		queryContent.append(String.format(
				  "<%s> a rmo:Revision ; %n"
				+ "	rmo:revisionOf <%s> ; %n"
				+ "	rmo:deltaAdded <%s> ; %n"
				+ "	rmo:deltaRemoved <%s> ; %n"
				+ "	rmo:revisionNumber \"%s\" ; %n"
				+ "	rmo:revisionOfBranch <%s> . %n"
				,  revisionUri, graphName, addSetGraphUri, removeSetGraphUri, newRevisionNumber, branchUri));
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String revUri = getRevisionUri(graphName, iterator.next());
			queryContent.append(String.format("<%s> prov:wasDerivedFrom <%s> .", revisionUri, revUri));
		}
		String query = prefixes
				+ String.format("INSERT DATA { GRAPH <%s> { %s } } ;%n", Config.revision_graph,
						queryContent.toString());
		
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);

		// Move branch to new revision
		String branchIdentifier = usedRevisionNumber.get(0).toString();
		String oldRevisionUri = getRevisionUri(graphName, branchIdentifier);

		String queryBranch = prefixes + String.format("" 
					+ "SELECT ?branch " 
					+ "WHERE { GRAPH <%s> {" 
					+ "	?branch a rmo:Branch; "
					+ "		rmo:references <%s>."
					+ "	{?branch rdfs:label \"%s\"} UNION {<%s> rmo:revisionNumber \"%s\"}" 
					+ "} }",
					Config.revision_graph, oldRevisionUri, branchIdentifier, oldRevisionUri,
						branchIdentifier);
		QuerySolution sol = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryBranch).next();
		String branchName = sol.getResource("?branch").toString();

		query = prefixes + String.format("DELETE DATA { GRAPH <%s> { <%s> rmo:references <%s>. } };%n",
				Config.revision_graph, branchName, oldRevisionUri);
		query += String.format("INSERT DATA { GRAPH <%s> { <%s> rmo:references <%s>. } } ;%n", Config.revision_graph,
				branchName, revisionUri);

		// Execute queries
		logger.info("Execute all queries updating the revision graph, full graph and change sets");
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
	}

	
	/**
	 * Create a new tag
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionNumber
	 *            the revision number where the tag should be attached to
	 * @param tagName
	 *            name of the new tag
	 * @param user
	 *            user who performs this tag generation
	 * @param message
	 *            message describing intent of this command
	 * @throws InternalErrorException 
	 */
	public static void createTag(final String graphName,
			final String revisionNumber, final String tagName, final String user,
			final String message) throws InternalErrorException {
		createReference("tag", graphName, revisionNumber, tagName, user, message);
	}
		
	/**
	 * Create a new branch
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionNumber
	 *            the revision number where the branch should start 
	 * @param branchName
	 *            name of the new tag
	 * @param user
	 *            user who performs this branch generation
	 * @param message
	 *            message describing intent of this command
	 * @throws InternalErrorException 
	 */
	public static void createBranch(final String graphName,
			final String revisionNumber, final String branchName, final String user,
			final String message) throws InternalErrorException {
		createReference("branch", graphName, revisionNumber, branchName, user, message);
	}
			
	/**
	 * Create a new reference which can be a branch or a tag
	 * 
	 * @param referenceType
	 *            type of reference. can be "branch" or "tag"
	 * @param graphName
	 *            the graph name
	 * @param revisionNumber
	 *            the revision number where the reference should start or be
	 *            attached to
	 * @param newReferenceName
	 *            name of the new reference
	 * @param user
	 *            user who performs this reference generation
	 * @param message
	 *            message describing intent of this command
	 * @throws InternalErrorException 
	 */
	private static void createReference(final String referenceType, final String graphName,
			final String revisionNumber, final String newReferenceName, final String user,
			final String message) throws InternalErrorException {
		logger.info("Start creation of new " + referenceType);

		// Check branch existence
		if (checkReferenceNameExistence(graphName, newReferenceName)) {
			// Branch name is already in use
			logger.error("The reference name '" + newReferenceName + "' is for the graph '" + graphName
					+ "' already in use.");
			throw new IdentifierAlreadyExistsException("The reference name '" + newReferenceName
					+ "' is for the graph '" + graphName + "' already in use.");
		} else {
			// General variables
			String dateString = getDateString();
			String commitUri = graphName + "-commit-" + referenceType + "-" + newReferenceName;
			String referenceUri = graphName + "-" + referenceType + "-" + newReferenceName;
			String referenceTypUri = referenceType.equals("tag") ? "rmo:Tag" : "rmo:Branch";
			String revisionUri = getRevisionUri(graphName, revisionNumber);
			String personUri = getUserName(user);

			// Create a new commit (activity)
			String queryContent = String.format("<%s> a rmo:ReferenceCommit; "
					+ "	prov:wasAssociatedWith <%s> ;" + "	prov:generated <%s> ;" + "   prov:used <%s> ;"
					+ "	dc-terms:title \"%s\" ;" + "	prov:atTime \"%s\" .%n", commitUri, personUri,
					referenceUri, revisionUri, message, dateString);

			// Create new branch
			queryContent += String.format("<%s> a %s, rmo:Reference; " + " rmo:fullGraph <%s>; "
					+ "	prov:wasDerivedFrom <%s>; " + "	rmo:references <%s>; " + "	rdfs:label \"%s\". ",
					referenceUri, referenceTypUri, referenceUri, revisionUri, revisionUri, newReferenceName);

			// Update full graph of branch
			generateFullGraphOfRevision(graphName, revisionNumber, referenceUri);

			// Execute queries
			String query = prefixes
					+ String.format("INSERT DATA { GRAPH <%s> { %s } } ;", Config.revision_graph, queryContent);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
		}
	}

	/**
	 * Checks if graph exists in triple store. Works only when the graph is not
	 * empty.
	 * 
	 * @param graphName
	 *            the graph name
	 * @return boolean value if specified graph exists and contains at least one
	 *         triple elsewhere it will return false
	 */
	public static boolean checkGraphExistence(final String graphName){
		String query = "ASK { GRAPH <" + graphName + "> {?s ?p ?o} }";
		return TripleStoreInterfaceSingleton.get().executeAskQuery(query);
	}

	/**
	 * Creates the whole revision from the add and delete sets of the
	 * predecessors. Saved in graph tempGraphName.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionName
	 *            revision number or revision name to build content for
	 * @param tempGraphName
	 *            the graph where the temporary graph is stored
	 * @throws InternalErrorException 
	 */
	public static void generateFullGraphOfRevision(final String graphName, final String revisionName,
			final String tempGraphName) throws InternalErrorException {
		logger.info("Rebuild whole content of revision " + revisionName + " of graph <" + graphName
				+ "> into temporary graph <" + tempGraphName + ">");
		String revisionNumber = getRevisionNumber(graphName, revisionName);

		// Create temporary graph
		TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + tempGraphName + ">");
		TripleStoreInterfaceSingleton.get().executeUpdateQuery("CREATE GRAPH <" + tempGraphName + ">");

		// Create path to revision
		Tree tree =  new Tree(graphName);
		LinkedList<Revision> list = tree.getPathToRevision(revisionNumber);

		// Copy branch to temporary graph
		String number = list.pollFirst().getRevisionNumber();
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(
				"COPY GRAPH <" + RevisionManagement.getReferenceGraph(graphName, number) + "> TO GRAPH <"
						+ tempGraphName + ">");

		while (!list.isEmpty()) {
			// add- und delete-sets could be extracted from revision tree information
			// hard coded variant is faster
			String graph_removed = graphName + "-delta-removed-"+ number;
			String graph_added   = graphName + "-delta-added-"+ number;
			// Add data to temporary graph
			if (RevisionManagement.checkGraphExistence(graph_removed))
				TripleStoreInterfaceSingleton.get().executeUpdateQuery("ADD GRAPH <" + graph_removed + "> TO GRAPH <" + tempGraphName + ">");
			// Remove data from temporary graph (no opposite of SPARQL ADD available)
			if (RevisionManagement.checkGraphExistence(graph_added))
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(  "DELETE { GRAPH <" + tempGraphName+ "> { ?s ?p ?o.} }"
														+ "WHERE  { GRAPH <" + graph_added	+ "> { ?s ?p ?o.} }");

			number = list.pollFirst().getRevisionNumber();
		}

	}

	/**
	 * Get the revision URI for a given reference name or revision number
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionIdentifier
	 *            reference name or revision number
	 * @return URI of identified revision
	 * @throws InternalErrorException 
	 */
	public static String getRevisionUri(final String graphName, final String revisionIdentifier) throws InternalErrorException {
		String query = prefixes
				+ String.format(
						"SELECT ?rev WHERE { GRAPH <%s> {"
								+ "{?rev a rmo:Revision; rmo:revisionOf <%s>; rmo:revisionNumber \"%s\" .}"
								+ "UNION {?rev a rmo:Revision; rmo:revisionOf <%s>. ?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\" .}"
								+ "} }", Config.revision_graph, graphName, revisionIdentifier, graphName,
						revisionIdentifier);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				logger.error("Identifier not unique: " + revisionIdentifier);
				throw new InternalErrorException("Identifier not unique: " + revisionIdentifier);
			}
			return qs.getResource("?rev").toString();
		} else {
			logger.error("No Revision or Reference found with identifier: " + revisionIdentifier);
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ revisionIdentifier);
		}
	}

	/**
	 * Get the reference URI of a branch for a given reference name or revision number
	 * 
	 * @param graphName
	 *            the graph name
	 * @param referenceIdentifier
	 *            reference name or revision number
	 * @return URI of identified revision
	 * @throws InternalErrorException 
	 */
	public static String getBranchUri(final String graphName, final String referenceIdentifier) throws InternalErrorException {
		String query = prefixes
				+ String.format("SELECT ?ref " + "WHERE { GRAPH <%s> {"
						+ "	?ref a rmo:Branch; rmo:references ?rev."
						+ " ?rev a rmo:Revision; rmo:revisionOf <%s>."
						+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref rdfs:label \"%s\" .}" + "} }",
						Config.revision_graph, graphName, referenceIdentifier, referenceIdentifier);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalErrorException("Identifier is not unique for specified graph name: "
						+ referenceIdentifier);
			}
			return qs.getResource("?ref").toString();
		} else {
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ referenceIdentifier);
		}
	}

	/**
	 * Get the graph URI containing the full copy of a reference for a given
	 * reference name or revision number
	 * 
	 * @param graphName
	 *            the graph name
	 * @param referenceIdentifier
	 *            reference name or revision number
	 * @return first graph name of full graph for specified reference and graph
	 * @throws InternalErrorException 
	 */
	public static String getReferenceGraph(final String graphName, final String referenceIdentifier) throws InternalErrorException {
		String query = prefixes + String.format("" 
				+ "SELECT ?graph " 
				+ "WHERE { GRAPH  <%s> {" 
				+ "	?ref a rmo:Reference; "
				+ "		rmo:references ?rev;" 
				+ "		rmo:fullGraph ?graph."
				+ " ?rev a rmo:Revision; rmo:revisionOf <%s>."
				+ "	{?ref rdfs:label \"%s\"} UNION {?rev rmo:revisionNumber \"%s\"}" 
				+ "} }", Config.revision_graph, graphName, referenceIdentifier, referenceIdentifier);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			return qs.getResource("?graph").toString();
		} else {
			throw new InternalErrorException("No reference graph found for graph <"+graphName+"> and identifier \""+ referenceIdentifier+"\"");
		}
	}

	/**
	 * Get the revision number of a given reference name.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param referenceName
	 *            the reference name
	 * @return the revision number of given reference name
	 * @throws InternalErrorException 
	 */
	public static String getRevisionNumber(final String graphName, final String referenceName) throws InternalErrorException {
		String query = prefixes
				+ String.format(
						"SELECT ?revNumber WHERE { GRAPH <%s> {"
								+ "	?rev a rmo:Revision; rmo:revisionNumber ?revNumber; rmo:revisionOf <%s>."
								+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\".}"
								+ "} }", Config.revision_graph, graphName, referenceName, referenceName);
		ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (resultSet.hasNext()) {
			QuerySolution qs = resultSet.next();
			if (resultSet.hasNext()) {
				throw new InternalErrorException("Identifier not unique: " + referenceName);
			}
			return qs.getLiteral("?revNumber").toString();
		} else {
			throw new InternalErrorException("No Revision or Reference found with identifier: "
					+ referenceName);
		}
	}

	

	/**
	 * Get the MASTER revision number of a graph.
	 * 
	 * @param graphName
	 *            the graph name
	 * @return the MASTER revision number
	 */
	public static String getMasterRevisionNumber(final String graphName) {
		logger.info("Get MASTER revision number of graph " + graphName);

		String queryString = prefixes + String.format(""
				+ "SELECT ?revisionNumber "  
				+ "WHERE { GRAPH <%s> {"
				+ "	?master a rmo:Master; rmo:references ?revision . "
				+ "	?revision rmo:revisionNumber ?revisionNumber; rmo:revisionOf <%s> . " 
				+ "} }", Config.revision_graph, graphName);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryString);
		if (results.hasNext()){
			QuerySolution qs = results.next();
			return qs.getLiteral("?revisionNumber").getString();
		}
		else {
			return null;
		}
	}

	/**
	 * Checks whether the referenced reference has at least one own revision.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param referenceIdentifier
	 *            the reference identifier which was specified by the client
	 *            (branch name or tag name)
	 * @return true when it is an empty branch
	 * @throws InternalErrorException 
	 */
//	private static boolean isBranchEmpty(final String graphName, final String referenceIdentifier) throws InternalErrorException {
//		String referenceUri = getBranchUri(graphName, referenceIdentifier);
//		String queryASKBranch = prefixes
//				+ String.format("ASK { GRAPH <%s> { "
//						+ " <%s> rmo:references ?rev; prov:wasDerivedFrom ?rev ." 
//						+ " }} ",
//						Config.revision_graph, referenceUri);
//		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASKBranch);
//	}

	/**
	 * 
	 * @param graphName
	 * @param revisionIdentifier
	 * @return
	 * @throws InternalErrorException
	 */
	public static String getNextRevisionNumber(final String graphName, final String revisionIdentifier) throws InternalErrorException {

//		String revisionNumber = getRevisionNumber(graphName, revisionIdentifier);
//		if (isBranchEmpty(graphName, revisionIdentifier)) {
//			return getRevisionNumberForNewBranch(graphName, revisionNumber);
//		} else {
//			return getNextRevisionNumberForLastRevisionNumber(graphName, revisionNumber);
//		}
		
		// create UID and check whether the uid number already in named graph exist, if yes , than create it once again,
		// if not , return this one
		
		UID nextNumberUid = new UID();
		String nextNumber = nextNumberUid.toString();
		
		while (checkExistUID(nextNumber,graphName)){
			UID newNextNumberUid = new UID();
			nextNumber = newNextNumberUid.toString();		
		}
		
		return nextNumber;
	}
	
	/**
	 * check whether the uid number already exist
	 * @param revisionUid
	 * @return boolean*/
	
	public static boolean checkExistUID(final String revisionUid, final String graphName) {
		String queryASK = prefixes
				+ String.format(""
						+ "ASK { GRAPH <%s> { " 
						+ " ?rev a rmo:Revision; rmo:revisionOf <%s>; rmo:revisionNumber \"%s\". }} ",
						Config.revision_graph, graphName, revisionUid);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}
	
	
	/**
	 * Get the next revision number for specified revision number of any branch.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionNumber
	 *            the revision number of the last revision
	 * @return the next revision number for specified revision of branch
	 */
//	public static String getNextRevisionNumberForLastRevisionNumber(final String graphName,
//			final String revisionNumber) {
////		if (revisionNumber.contains("-")) {
////			return revisionNumber.substring(0, revisionNumber.lastIndexOf('-') + 1)
////					+ (Integer.parseInt(revisionNumber.substring(revisionNumber.lastIndexOf('-') + 1,
////							revisionNumber.length())) + 1);
////		} else {
////			return Integer.toString((Integer.parseInt(revisionNumber) + 1));
////		}
//		// UID Hash code
//		UID nextNumber = new UID();
//		return nextNumber.toString();
//		
//	}

	/**
	 * Get the revision number for a new branch.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param revisionNumber
	 *            the revision number of the revision which should be branched
	 * @return the revision number of the new branch
	 */
//	public static String getRevisionNumberForNewBranch(final String graphName, final String revisionNumber) {
////		logger.info("Get the revision number for a new branch of graph " + graphName
////				+ " and revision number " + revisionNumber);
////		int ii = 0;
////		String newRevisionNumber;
////		final int MAX_TRIES = 99;
////		while (ii < MAX_TRIES) {
////			newRevisionNumber = revisionNumber + "." + ii + "-0";
////			String queryASK = prefixes	+ String.format("" 
////					+ "ASK { GRAPH <%s> { " 
////					+ " ?rev a rmo:Revision;"
////					+ "		rmo:revisionOf <%s>;" 
////					+ "		rmo:revisionNumber \"%s\"}}",
////							Config.revision_graph, graphName, newRevisionNumber);
////			boolean resultASK = TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
////			if (resultASK == false) {
////				return newRevisionNumber;
////			}
////			ii++;
////		}
////		return null;
//		// UID Hash code
//			UID newBranchNumber = new UID();
//			return newBranchNumber.toString();
//		
//	}

	/**
	 * Split huge INSERT statements into separate queries of up to fifty triple
	 * statements.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param dataSetAsNTriples
	 *            the data to insert as N-Triples
	 */
	public static void executeINSERT(final String graphName, final String dataSetAsNTriples) {

		String insertQueryTemplate =  "INSERT DATA { GRAPH <%s> { %n"
				+ "	%s %n"
				+ "} }";
		
		final int MAX_STATEMENTS = 200;
		String[] lines = dataSetAsNTriples.split("\\.\\s*<");
		int counter = 0;
		StringBuilder insert = new StringBuilder();
		
		for (int i=0; i < lines.length; i++) {
			// Remove whitespace characters
			String sub = lines[i].replaceAll("# Empty NT", "").trim();
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
					TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(insertQueryTemplate, graphName, insert));
					counter = 0;
					insert = new StringBuilder();
				}
			}
		}

		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(insertQueryTemplate, graphName, insert));
	}
	
	
	/**
	 * Split huge DELETE statements into separate queries of up to fifty triple statements.
	 * 
	 * @param graphName the graph name
	 * @param dataSetAsNTriples the data to insert as N-Triples 
	 */
	public static void executeDELETE(final String graphName, final String dataSetAsNTriples) {

		String deleteQueryTemplate =  "DELETE DATA { GRAPH <%s> { %s } }";
		
		final int MAX_STATEMENTS = 200;
		String[] lines = dataSetAsNTriples.split("\\.\\s*<");
		int counter = 0;
		StringBuilder delete = new StringBuilder();
		
		for (int i=0; i < lines.length; i++) {
			// Remove whitespace characters
			String sub = lines[i].replaceAll("# Empty NT", "").trim();
			if (!sub.equals("") && !sub.startsWith("#")) {
				if (!sub.startsWith("<")) {
					sub = "<" + sub;
				}
				if (i < lines.length - 1) {
					sub = sub + ".";
				}
				delete.append('\n').append(sub);
				counter++;
				if (counter == MAX_STATEMENTS-1) {
					TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(deleteQueryTemplate, graphName, delete));
					counter = 0;
					delete = new StringBuilder();
				}
			}
		}
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(deleteQueryTemplate, graphName, delete));
	}
	
	

	/**
	 * Download complete revision information of R43ples from SPARQL endpoint.
	 * Provide only information from specified graph if not null
	 * 
	 * @param graphName
	 *            provide only information from specified graph (if not NULL)
	 * @param format
	 *            serialization of the RDF model
	 * @return String containing the RDF model in the specified serialization
	 */
	public static String getRevisionInformation(final String graphName, final String format) {
		String sparqlQuery;
		
		if (graphName.equals("")) {
			sparqlQuery = String.format(""
					+ "CONSTRUCT" 
					+ "	{ ?s ?p ?o} " 
					+ "WHERE { GRAPH <%s> {"
					+ "	?s ?p ?o." 
					+ "} }", Config.revision_graph);
		} else {
			sparqlQuery = prefixes + String.format(""
					+ "CONSTRUCT { " 
					+ "		?revision ?r_p ?r_o. "
					+ "		?reference ?ref_p ?ref_o. " 
					+ "		?commit	?c_p ?c_o. " 
					+ "	}" 
					+ "WHERE { GRAPH <%s> { " 
					+ "	?revision rmo:revisionOf <%s>; ?r_p ?r_o. "
					+ " OPTIONAL {?reference rmo:references ?revision; ?ref_p ?ref_o. }"
					+ " OPTIONAL {?commit prov:used|prov:generated ?revision; ?c_p ?c_o. }" 
					+ "} }",
					Config.revision_graph, graphName);
		}
		return TripleStoreInterfaceSingleton.get().executeConstructQuery(sparqlQuery, format);
	}

	/**
	 * Get revised graphs in R43ples.
	 * 
	 * @param format
	 *            serialization of the response
	 * @return String containing the SPARQL response in specified format
	 */
	public static String getRevisedGraphsSparql(final String format) {
		String sparqlQuery = prefixes
				+ String.format("" 
						+ "SELECT DISTINCT ?graph " 
						+ "WHERE {"
						+ " GRAPH <%s> { ?rev rmo:revisionOf ?graph. }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery, format);
	}
	
	
	/**
	 * Get revised graphs in R43ples.
	 * 
	 * @return result set
	 */
	public static ResultSet getRevisedGraphsSparql() {
		String sparqlQuery = prefixes
				+ String.format("" 
						+ "SELECT DISTINCT ?graph " 
						+ "WHERE {"
						+ " GRAPH <%s> { ?rev rmo:revisionOf ?graph. }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
	}
	

	/**
	 * Get revised graphs in R43ples as list of string.
	 * 
	 * @return list of strings containing the revised graphs of R43ples
	 */
	public static ArrayList<String> getRevisedGraphs() {
		ArrayList<String> list = new ArrayList<String>();
		ResultSet results = getRevisedGraphsSparql();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			list.add(qs.getResource("graph").toString());
		}
		return list;
	}
	
	

	/**
	 * Deletes all information for a specific named graph including all full
	 * graphs and information in the R43ples system.
	 * 
	 * @param graph
	 *            graph to be purged
	 */
	public static void purgeGraph(final String graph) {
		logger.info("Purge graph " + graph + " and all related R43ples information.");
		// Drop all full graphs as well as add and delete sets which are related
		// to specified graph
		String query = prefixes	+ String.format(""
				+ "SELECT DISTINCT ?graph "
				+ "WHERE { GRAPH <%s> {" 
				+ "		?rev rmo:revisionOf <%s>."
				+ " 	{?rev rmo:deltaAdded ?graph}" 
				+ " UNION {?rev rmo:deltaRemoved ?graph}"
				+ " UNION {?ref rmo:references ?rev; rmo:fullGraph ?graph}" 
				+ "} }", Config.revision_graph, graph);
				
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			if (qs.contains("?graph")) {
					String graphName = qs.getResource("graph").toString();
					TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + graphName + ">");
					logger.debug("Graph deleted: " + graphName);
			}
		}
		
		// Remove information from revision graph
		String queryDelete = prefixes + String.format(
					   	"DELETE { "
						+ "GRAPH <%s> {"
					   	+ "		?revision ?r_p ?r_o. "
						+ "		?reference ?ref_p ?ref_o. " 
						+ "		?commit	?c_p ?c_o. " 
						+ "		}"
						+ "}" 
						+ "WHERE {"
						+ "	GRAPH <%s> {" 
						+ "		?revision rmo:revisionOf <%s>; ?r_p ?r_o. "
						+ " 	OPTIONAL {?reference rmo:references ?revision; ?ref_p ?ref_o. }"
						+ " 	OPTIONAL {?commit prov:used|prov:generated ?revision; ?c_p ?c_o. }"
						+ "		}" 
						+ "}"
						, Config.revision_graph, Config.revision_graph, graph);
		
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryDelete);
	}

	/**
	 * @param user
	 *            name as string
	 * @return URI of person
	 */
	public static String getUserName(final String user) {
		// When user does not already exists - create new

		String query = prefixes
				+ String.format("SELECT ?personUri { GRAPH <%s>  { " + "?personUri a prov:Person;"
						+ "  rdfs:label \"%s\"." + "} }", Config.revision_graph, user);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		if (results.hasNext()) {
			logger.debug("User " + user + " already exists.");
			QuerySolution qs = results.next();
			return qs.getResource("?personUri").toString();
		} else {
			String personUri = null;
			try {
				personUri = "http://eatld.et.tu-dresden.de/persons/" + URLEncoder.encode(user, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			logger.debug("User does not exists. Create user " + personUri + ".");
			query = prefixes
					+ String.format("INSERT DATA { GRAPH <%s> { <%s> a prov:Person; rdfs:label \"%s\". } }",
							Config.revision_graph, personUri, user);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
			return personUri;
		}
	}

	/**
	 * @return current date formatted as xsd:DateTime
	 */
	public static String getDateString() {
		// Create current time stamp
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH:mm:ss");
		String dateString = df.format(date);
		logger.debug("Time stamp created: " + dateString);
		return dateString;
	}

	/**
	 * Check whether the branch name is already used by specified graph name.
	 * 
	 * @param graphName
	 *            the corresponding graph name
	 * @param referenceName
	 *            the branch name to check
	 * @return true when branch already exists elsewhere false
	 */
	private static boolean checkReferenceNameExistence(final String graphName, final String referenceName) {
		String queryASK = prefixes
				+ String.format("ASK { GRAPH <%s> { " + " ?ref a rmo:Reference; rdfs:label \"%s\". "
						+ " ?ref rmo:references ?rev ." + " ?rev rmo:revisionOf <%s> ." + " }} ",
						Config.revision_graph, referenceName, graphName);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}

	/**
	 * Checks if specified revision of the graph is a branch revision, meaning a
	 * terminal node in a branch.
	 * 
	 * @param graphName
	 *            name of the revisioned graph
	 * @param identifier
	 *            revision number or branch or tag name of the graph
	 * @return true if specified revision of the graph is a branch
	 */
	public static boolean isBranch(final String graphName, final String identifier) {
		String queryASK = prefixes
				+ String.format(""
						+ "ASK { GRAPH <%s> { " 
						+ " ?rev a rmo:Revision; rmo:revisionOf <%s>. "
						+ " ?ref a rmo:Reference; rmo:references ?rev ."
						+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
						Config.revision_graph, graphName, identifier, identifier);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}
	
	
	/**
	 * Get the ADD set URI of a given revision URI.
	 * 
	 * @param revisionURI the revision URI
	 * @param revisionGraph the revision graph
	 * @return the ADD set URI, returns null when the revision URI does not exists or no ADD set is referenced by the revision URI
	 */
	public static String getAddSetURI(String revisionURI, String revisionGraph) {
		String query = String.format(
			  "SELECT ?addSetURI %n"
			+ "WHERE { GRAPH <%s> {%n"
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#deltaAdded> ?addSetURI . %n"
			+ "} }", revisionGraph, revisionURI);
		
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			return qs.getResource("?addSetURI").toString();
		} else {
			return null;
		}
	}
	
	
	/**
	 * Get the DELETE set URI of a given revision URI.
	 * 
	 * @param revisionURI the revision URI
	 * @param revisionGraph the revision graph
	 * @return the DELETE set URI, returns null when the revision URI does not exists or no DELETE set is referenced by the revision URI
	 */
	public static String getDeleteSetURI(String revisionURI, String revisionGraph) {
		String query = String.format(
			  "SELECT ?deleteSetURI %n"
		    + "WHERE { GRAPH <%s> {%n"
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#deltaRemoved> ?deleteSetURI . %n"
			+ "} }", revisionGraph, revisionURI);
		
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			return qs.getResource("?deleteSetURI").toString();
		} else {
			return null;
		}
	}
	
	
	/**
	 * Get the content of a graph by execution of CONSTRUCT.
	 * 
	 * @param graphName the graphName
	 * @param format RDF serialisation format which should be returned
	 * @return the constructed graph content as specified RDF serialisation format
	 */
	public static String getContentOfGraphByConstruct(String graphName, String format) {
		String query = String.format(
				  "CONSTRUCT {?s ?p ?o} %n"
				+ "WHERE { GRAPH <%s> {?s ?p ?o} }", graphName);
		
		return TripleStoreInterfaceSingleton.get().executeConstructQuery(query, format);		
	}
	
	
	/** Creates an RDF description for the revision tree of the graphs specified in the given SPARQL query
	 * @param query SPARQL query
	 * @return RDF string containing information for graphs specified in query 
	 */
	public static String getResponseHeaderFromQuery(String query) {
		final Pattern patternGraph = Pattern.compile(
				"(GRAPH|FROM|INTO)\\s*<(?<graph>[^>]*)>\\s*",
				Pattern.CASE_INSENSITIVE);
		
		StringBuilder graphNames = new StringBuilder();
		Matcher m = patternGraph.matcher(query);
		m.find();
		while (!m.hitEnd()) {
			String graphName = m.group("graph");
			graphNames.append("<"+graphName+">");
			m.find();
			if (!m.hitEnd())
				graphNames.append(", ");			
		}
		String names = graphNames.toString();
		String result = getResponseHeader(names);
		return result;
		
	}
	
	public static String getResponseHeader(String graphList) {
		String queryConstruct = RevisionManagement.prefixes + String.format(
				  "CONSTRUCT {"
				+ " ?ref a ?type;"
				+ "		rdfs:label ?label;"
				+ "		rmo:references ?rev."
				+ " ?rev rmo:revisionOf ?graph;"
				+ "			rmo:revisionNumber ?number . %n"
				+ "} %n"
				+ "WHERE { GRAPH <%s> { "
				+ " ?ref a ?type;"
				+ "		rdfs:label ?label;%n"
				+ "		rmo:references ?rev."
				+ " ?rev rmo:revisionOf ?graph;"
				+ "			rmo:revisionNumber ?number . %n"
				+ "FILTER (?type IN (rmo:Tag, rmo:Master, rmo:Branch)) %n"
				+ "FILTER (?graph IN (%s)) %n"
				+ "} }", Config.revision_graph, graphList);
		String header = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryConstruct, FileUtils.langTurtle);
		return header;
	}
	
	public static String getFullGraphUri(String branchURI) {
		String query = String.format(
				  "SELECT ?fullGraphURI %n"
			    + "WHERE { GRAPH <%s> {%n"
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#fullGraph> ?fullGraphURI . %n"
				+ "} }", Config.revision_graph, branchURI);
			
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			return qs.getResource("?fullGraphURI").toString();
		} else {
			return null;
		}
	}


	
}
