
package de.tud.plt.r43ples.management;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import de.tud.plt.r43ples.merging.MergeManagement;
import de.tud.plt.r43ples.merging.control.FastForwardControl;
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
	/** The SPARQL prefixes. 
	 * TODO: Add possibility to insert user defined prefixes
	 * **/
	public static final String prefixes = 
			  "PREFIX rmo:	<http://eatld.et.tu-dresden.de/rmo#> \n"
			+ "PREFIX prov: <http://www.w3.org/ns/prov#> \n"
			+ "PREFIX dc-terms:	<http://purl.org/dc/terms/> \n" 
			+ "PREFIX xsd:	<http://www.w3.org/2001/XMLSchema#> \n"
			+ "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> \n"
			+ "PREFIX sdd:	<http://eatld.et.tu-dresden.de/sdd#> \n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
			+ "PREFIX rdf:	<http://www.w3.org/1999/02/22-rdf-syntax-ns#>  \n"
			+ "PREFIX owl:	<http://www.w3.org/2002/07/owl#> \n"
			+ "PREFIX test: <http://test.com/> \n"
			+ "PREFIX mso: <http://eatld.et.tu-dresden.de/mso/> \n";


	/**
	 * Put existing graph under version control. Existence of graph is not checked. Current date is used for commit timestamp
	 * 
	 * @param graphName
	 *            the graph name of the existing graph
	 * @param datetime
	 * 			time stamp to be inserted in commit
	 */
	protected static String putGraphUnderVersionControl(final String graphName, final String datetime) {

		logger.info("Put existing graph under version control with the name " + graphName);

		String revisiongraph = graphName + "-revisiongraph";
		
		while (checkGraphExistence(revisiongraph)){
			revisiongraph += "x";
		}
		
		String queryAddRevisionGraph = String.format(prefixes
				+ "INSERT DATA { GRAPH <%1$s> {"
				+ "  <%2$s> a rmo:Graph;"
				+ "    rmo:hasRevisionGraph <%3$s>;"
				+ "    sddo:hasDefaultSDD sdd:defaultSDD."
				+ "} }",
				Config.revision_graph, graphName, revisiongraph);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryAddRevisionGraph);
		 		
		 		
		String revisionNumber = "1";
		String revisionUri = graphName + "-revision-" + revisionNumber;
		String commitUri = graphName + "-commit-" + revisionNumber;
		String branchUri = graphName + "-master";

		// Create new revision
		String queryContent = String.format(
				  "<%s> a rmo:Revision;"
				+ "	rmo:revisionNumber \"%s\";"
				+ "	rmo:belongsTo <%s>. ",
				revisionUri, revisionNumber, branchUri);
		
		// Add MASTER branch		
		queryContent += String.format(
				"<%s> a rmo:Master, rmo:Branch, rmo:Reference;"
				+ " rmo:fullGraph <%s>;"
				+ "	rmo:references <%s>;"
				+ "	rdfs:label \"master\".",
				branchUri, graphName, revisionUri);

		queryContent += String.format(""
				+ "<%s> a rmo:RevisionCommit, rmo:BranchCommit; "
				+ "	prov:wasAssociatedWith <%s> ;" 
				+ "	prov:generated <%s>, <%s> ;" 
				+ "	dc-terms:title \"initial commit\" ;" 
				+ "	prov:atTime \"%s\"^^xsd:dateTime .%n",
				commitUri,  "http://eatld.et.tu-dresden.de/user/r43ples", revisionUri, branchUri, datetime);
		
		String queryRevision = prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisiongraph, queryContent);
		
		//TripleStoreInterfaceSingleton.get().executeCreateGraph(graph);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRevision);
		
		return revisionNumber;
	}
	
	/**
	 * Put existing graph under version control. Existence of graph is not checked. Current date is used for commit timstamp
	 * 
	 * @param graphName
	 *            the graph name of the existing graph
	 */
	public static String putGraphUnderVersionControl(final String graphName) {
		return putGraphUnderVersionControl(graphName, getDateString());
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
	 * @param timeStamp
	 * 				time stamp of the commit as String
	 * @param commitMessage
	 *            the title of the revision
	 * @param usedRevisionNumber
	 *            the number of the revision which is used for creation of the
	 *            new revision 
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String createNewRevision(final String graphName, final String addedAsNTriples, final String removedAsNTriples,
			final String user, final String timeStamp, final String commitMessage, final String usedRevisionNumber) throws InternalErrorException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(usedRevisionNumber);
		return createNewRevision(graphName, addedAsNTriples, removedAsNTriples, user, timeStamp, commitMessage, list);
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
	 * create new revision with patch with addedUri and removedUri
	 * 
	 * @param graphName
	 *            the graph name
	 * @param addSetGraphUri
	 *           uri of the data set of added triples as N-Triples
	 * @param deleteSetGraphUri
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
	public static String createNewRevisionWithPatch(final String graphName, final String addSetGraphUri, final String deleteSetGraphUri,
			final String user, final String commitMessage, final String usedRevisionNumber) throws InternalErrorException {
		String newRevisionNumber = getNextRevisionNumber(graphName);
		String referenceGraph = getReferenceGraph(graphName, usedRevisionNumber);

		addNewRevisionFromChangeSet(user, commitMessage, graphName, usedRevisionNumber, newRevisionNumber, referenceGraph, addSetGraphUri, deleteSetGraphUri); 
		return newRevisionNumber;
	}
	
	public static String createNewRevision(final String graphName, final String addedAsNTriples,
			final String removedAsNTriples, final String user, final String commitMessage,
			final ArrayList<String> usedRevisionNumber) throws InternalErrorException {
		String timeStamp = getDateString();
		return createNewRevision(graphName, addedAsNTriples, removedAsNTriples, user, timeStamp, commitMessage, usedRevisionNumber);
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
			final String removedAsNTriples, final String user, final String timeStamp, final String commitMessage,
			final ArrayList<String> usedRevisionNumber) throws InternalErrorException {
		logger.info("Create new revision for graph " + graphName);

		// General variables
		String newRevisionNumber = getNextRevisionNumber(graphName);
		String addSetGraphUri = graphName + "-addSet-" + newRevisionNumber;
		String removeSetGraphUri = graphName + "-deleteSet-" + newRevisionNumber;
		String referenceGraph = getReferenceGraph(graphName, usedRevisionNumber.get(0));

		// Add Meta Information
		addMetaInformationForNewRevision(graphName, user, timeStamp, commitMessage, usedRevisionNumber,
				newRevisionNumber, addSetGraphUri, removeSetGraphUri);

		// Update full graph of branch
		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
			RevisionManagement.executeDELETE(referenceGraph, removedAsNTriples);
		}
		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
			RevisionManagement.executeINSERT(referenceGraph, addedAsNTriples);
		}

		// Create new graph with addSet-newRevisionNumber
		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
			logger.debug("Create new graph with name " + addSetGraphUri);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n",
				addSetGraphUri));
			RevisionManagement.executeINSERT(addSetGraphUri, addedAsNTriples);
		}

		// Create new graph with deleteSet-newRevisionNumber
		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
			logger.debug("Create new graph with name " + removeSetGraphUri);
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
	 * Add new revision from existing changeset in triplestore.
	 * Applies changeset to full graph and add meta information in revision graph
	 * 
	 * @param user
	 * @param commitMessage
	 * @param graphName
	 * @param revisionName
	 * @param newRevisionNumber
	 * @param referenceFullGraph
	 * @param addSetGraphUri
	 * @param deleteSetGraphUri
	 * @throws InternalErrorException
	 */
	protected static void addNewRevisionFromChangeSet(final String user, final String commitMessage,
			String graphName, String revisionName, String newRevisionNumber, String referenceFullGraph,
			String addSetGraphUri, String deleteSetGraphUri) throws InternalErrorException {
		// remove doubled data
		// (already existing triples in add set; not existing triples in delete set)
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }", addSetGraphUri,
				referenceFullGraph));
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
				deleteSetGraphUri, deleteSetGraphUri, referenceFullGraph));

		// merge change sets into reference graph
		// (copy add set to reference graph; remove delete set from reference graph)
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"INSERT { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				referenceFullGraph,	addSetGraphUri));
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }", 
				referenceFullGraph,	deleteSetGraphUri));

		// add meta information to R43ples
		RevisionManagement.addMetaInformationForNewRevision(graphName, user, commitMessage, revisionName,
				newRevisionNumber, addSetGraphUri, deleteSetGraphUri);
	}

	
	/**
	 * 
	 * @param graphName
	 * @param user
	 * @param commitMessage
	 * @param usedRevisionNumber
	 * @param newRevisionNumber
	 * @param addSetGraphUri
	 * @param deleteSetGraphUri
	 * @throws InternalErrorException
	 */
	public static void addMetaInformationForNewRevision(final String graphName, final String user,
			final String commitMessage, final String usedRevisionNumber,
			final String newRevisionNumber, final String addSetGraphUri, final String deleteSetGraphUri) throws InternalErrorException {
		ArrayList<String> list = new ArrayList<String>();
		list.add(usedRevisionNumber);
		addMetaInformationForNewRevision(graphName, user, getDateString(), commitMessage, list, newRevisionNumber, addSetGraphUri, deleteSetGraphUri);
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
	public static void addMetaInformationForNewRevision(final String graphName, final String user, final String timeStamp,
			final String commitMessage, final ArrayList<String> usedRevisionNumber,
			final String newRevisionNumber, final String addSetGraphUri, final String removeSetGraphUri) throws InternalErrorException {
		
		String revisionGraph = getRevisionGraph(graphName);
		String personUri = getUserName(user);
		String revisionUri = graphName + "-revision-" + newRevisionNumber;
		String commitUri = graphName + "-commit-" + newRevisionNumber;
		String branchUri = getBranchUri(revisionGraph, usedRevisionNumber.get(0));

		// Create a new commit (activity)
		StringBuilder queryContent = new StringBuilder(1000);
		queryContent.append(String.format(""
				+ "<%s> a rmo:RevisionCommit; " 
				+ "	prov:wasAssociatedWith <%s>;"
				+ "	prov:generated <%s>;" 
				+ "	dc-terms:title \"%s\";" 
				+ "	prov:atTime \"%s\"^^xsd:dateTime. %n", commitUri,
				personUri, revisionUri, commitMessage, timeStamp));

		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String revUri = getRevisionUri(revisionGraph, iterator.next());
			queryContent.append(String.format("<%s> prov:used <%s>. %n", commitUri, revUri));
		}

		// Create new revision
		queryContent.append(String.format(
				  "<%s> a rmo:Revision ; %n"
				+ "	rmo:addSet <%s> ; %n"
				+ "	rmo:deleteSet <%s> ; %n"
				+ "	rmo:revisionNumber \"%s\" ; %n"
				+ "	rmo:belongsTo <%s> . %n"
				,  revisionUri, addSetGraphUri, removeSetGraphUri, newRevisionNumber, branchUri));
		for (Iterator<String> iterator = usedRevisionNumber.iterator(); iterator.hasNext();) {
			String revUri = getRevisionUri(revisionGraph, iterator.next());
			queryContent.append(String.format("<%s> prov:wasDerivedFrom <%s> .", revisionUri, revUri));
		}
		String query = prefixes
				+ String.format("INSERT DATA { GRAPH <%s> { %s } }", revisionGraph,
						queryContent.toString());
		
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);

		// Move branch to new revision
		String branchIdentifier = usedRevisionNumber.get(0).toString();
		String oldRevisionUri = getRevisionUri(revisionGraph, branchIdentifier);

		String queryBranch = prefixes + String.format("" 
					+ "SELECT ?branch " 
					+ "WHERE { GRAPH <%s> {" 
					+ "	?branch a rmo:Branch; "
					+ "		rmo:references <%s>."
					+ "	{?branch rdfs:label \"%s\"} UNION {<%s> rmo:revisionNumber \"%s\"}" 
					+ "} }",
					revisionGraph, oldRevisionUri, branchIdentifier, oldRevisionUri,
						branchIdentifier);
		QuerySolution sol = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryBranch).next();
		String branchName = sol.getResource("?branch").toString();
		moveBranchReference(revisionGraph, branchName, oldRevisionUri, revisionUri);
	}

	/** move the reference in the specified revision graph from the old revision to the new one
	 * 
	 * @param revisionGraph revision graph in the triplestore
	 * @param branchName name of branch
	 * @param revisionOld uri of the old revision
	 * @param revisionNew uri of the new revision
	 *  */
	public static void moveBranchReference(final String revisionGraph, final String branchName, final String revisionOld, final String revisionNew){
		// delete old reference
		String query = prefixes	+ String.format(""
				+ "DELETE DATA { GRAPH <%1$s> { <%2$s> rmo:references <%3$s>. } };" 
				+ "INSERT DATA { GRAPH <%1$s> { <%2$s> rmo:references <%4$s>. } }",
				revisionGraph, branchName, revisionOld, revisionNew);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
	}
	
	
	/**
	 * @throws InternalErrorException 
	 * 
	 */
	public static boolean performFastForward(final String revisionGraph, final String branchNameA, final String branchNameB, final String user, final String dateTime, final String message) throws InternalErrorException{
		if (!FastForwardControl.fastForwardCheck(revisionGraph, branchNameA, branchNameB)) {
			return false;
		}
		String branchUriA = getBranchUri(revisionGraph, branchNameA);
		String branchUriB = getBranchUri(revisionGraph, branchNameB);
		
		String fullGraphUriA = getFullGraphUri(revisionGraph, branchUriA);
		String fullGraphUriB = getFullGraphUri(revisionGraph, branchUriB);
	
		String revisionUriA = getRevisionUri(revisionGraph, branchNameA);
		String revisionUriB = getRevisionUri(revisionGraph, branchNameB);
		
		moveBranchReference(revisionGraph, branchUriB, revisionUriB, revisionUriA);
		
		String commitUri = revisionGraph+"-ff-commit-"+branchNameA+"-"+branchNameB;
		String query = RevisionManagement.prefixes + String.format(""
				+ "INSERT DATA { GRAPH <%s> { "
				+ "  <%s> a rmo:FastForwardCommit;"
				+ "     prov:used <%s>, <%s>, <%s>;"
				+ "     prov:wasAssociatedWith <%s>;"
				+ "     prov:atTime <%s>;"
				+ "     dc-terms:title \"%s\". "
				+ "} }",
				revisionGraph, commitUri, branchUriA, revisionUriA, revisionUriB, user, dateTime, message);
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);
		updateBelongsTo(revisionGraph, branchUriB, revisionUriB, revisionUriA);	
		fullGraphCopy(fullGraphUriA, fullGraphUriB);
		return true;
	}
	
	/**
	 * updates all revisions between A and B and let them belong to the specified branch
	 * 
	 * @param revisionGraph 
	 * @param branch URI of the branch
	 * @param revisionStart uri of start revision
	 * @param revisionStop uri of last revision
	 * */
	public static void updateBelongsTo(final String revisionGraph, String branch, String revisionStart, String revisionStop ){
		LinkedList<String> revisionList =  MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, revisionStart, revisionStop);
		
		Iterator<String> riter = revisionList.iterator();
		while(riter.hasNext()) {
			String revision = riter.next();

			String query = RevisionManagement.prefixes + String.format("INSERT DATA { GRAPH <%s> { <%s> rmo:belongsTo <%s>. } };%n",
					revisionGraph, revision, branch);
			
			logger.debug("revisionlist info" + revision);
			logger.debug("updated info" + query);
			TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);			
		}
	}
	
	/** copy graph of branchA to fullgraph of branchB
	 * @param sourceGraph uri of source graph
	 * @param targetGraph uri of target graph
	 * */
	public static void fullGraphCopy(String sourceGraph, String targetGraph) {	
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(
				"COPY GRAPH <" + sourceGraph + "> TO GRAPH <"+ targetGraph + ">");
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
		logger.info("Create new " + referenceType + " '"+ newReferenceName+"' for graph " + graphName);
		
		String revisionGraph = getRevisionGraph(graphName);
		// Check branch existence
		if (checkReferenceNameExistence(revisionGraph, newReferenceName)) {
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
			String referenceTypeUri = referenceType.equals("tag") ? "rmo:Tag" : "rmo:Branch";
			String revisionUri = getRevisionUri(revisionGraph, revisionNumber);
			String personUri = getUserName(user);

			// Create a new commit (activity)
			String queryContent = String.format(""
					+ "<%s> a %sCommit, rmo:Commit; "
					+ "	prov:wasAssociatedWith <%s> ;" 
					+ "	prov:generated <%s> ;" 
					+ " prov:used <%s> ;"
					+ "	dc-terms:title \"%s\" ;" 
					+ "	prov:atTime \"%s\" .%n", 
					commitUri, referenceTypeUri, personUri, referenceUri, revisionUri, message, dateString);

			// Create new reference (branch/tag)
			queryContent += String.format(""
					+ "<%s> a %s, rmo:Reference; " 
					+ " rmo:fullGraph <%s>; "
					+ "	prov:wasDerivedFrom <%s>; " 
					+ "	rmo:references <%s>; " 
					+ "	rdfs:label \"%s\". ",
					referenceUri, referenceTypeUri, referenceUri, revisionUri, revisionUri, newReferenceName);

			// Update full graph of branch
			generateFullGraphOfRevision(graphName, revisionNumber, referenceUri);

			// Execute queries
			String query = prefixes
					+ String.format("INSERT DATA { GRAPH <%s> { %s } } ;", revisionGraph, queryContent);
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
		String revisionGraph = getRevisionGraph(graphName);
		String revisionNumber = getRevisionNumber(revisionGraph, revisionName);

		// Create temporary graph
		TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + tempGraphName + ">");
		TripleStoreInterfaceSingleton.get().executeUpdateQuery("CREATE GRAPH <" + tempGraphName + ">");

		// Create path to revision
		Tree tree =  new Tree(revisionGraph);
		LinkedList<Revision> list = tree.getPathToRevision(revisionNumber);

		// Copy branch to temporary graph
		String number = list.pollFirst().getRevisionNumber();
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(
				"COPY GRAPH <" + RevisionManagement.getReferenceGraph(graphName, number) + "> TO GRAPH <"
						+ tempGraphName + ">");

		while (!list.isEmpty()) {
			// add- und delete-sets could be extracted from revision tree information
			// hard coded variant is faster
			String graph_removed = graphName + "-deleteSet-"+ number;
			String graph_added   = graphName + "-addSet-"+ number;
			// Add data to temporary graph
			if (RevisionManagement.checkGraphExistence(graph_removed))
				TripleStoreInterfaceSingleton.get().executeUpdateQuery("ADD GRAPH <" + graph_removed + "> TO GRAPH <" + tempGraphName + ">");
			// Remove data from temporary graph (no opposite of SPARQL ADD available)
			if (RevisionManagement.checkGraphExistence(graph_added))
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(  "DELETE { GRAPH <" + tempGraphName+ "> { ?s ?p ?o.} }"
														+ "WHERE  { GRAPH <" + graph_added	+ "> { ?s ?p ?o.} }");
			Revision first = list.pollFirst();
			if (first!=null)
				number = first.getRevisionNumber();
		}

	}

	/**
	 * Get the revision URI for a given reference name or revision number
	 * 
	 * @param revisionGraph
	 *            the graph name
	 * @param revisionIdentifier
	 *            reference name or revision number
	 * @return URI of identified revision
	 * @throws InternalErrorException 
	 */
	public static String getRevisionUri(final String revisionGraph, final String revisionIdentifier) throws InternalErrorException {
		String query = prefixes
				+ String.format(
						"SELECT ?rev WHERE { GRAPH <%s> {"
							+ "{?rev a rmo:Revision; rmo:revisionNumber \"%s\" .}"
							+ "UNION {?rev a rmo:Revision. ?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\" .}"
							+ "} }", revisionGraph, revisionIdentifier, revisionIdentifier);
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
	public static String getBranchUri(final String revisionGraph, final String referenceIdentifier) throws InternalErrorException {
		String query = prefixes
				+ String.format("SELECT ?ref " 
						+ "WHERE { GRAPH <%s> {"
						+ "	?ref a rmo:Branch; rmo:references ?rev."
						+ " ?rev a rmo:Revision."
						+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref rdfs:label \"%s\" .}"
						+ "} }",
						revisionGraph, referenceIdentifier, referenceIdentifier);
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
		String revisionGraph = getRevisionGraph(graphName);
		String query = prefixes + String.format("" 
				+ "SELECT ?graph " 
				+ "WHERE { GRAPH  <%s> {" 
				+ "	?ref a rmo:Reference; "
				+ "		rmo:references ?rev;" 
				+ "		rmo:fullGraph ?graph."
				+ " ?rev a rmo:Revision."
				+ "	{?ref rdfs:label \"%s\"} UNION {?rev rmo:revisionNumber \"%s\"}" 
				+ "} }", revisionGraph, referenceIdentifier, referenceIdentifier);
		logger.debug(query);
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
	 * @param revisionGraph
	 *            the graph name
	 * @param referenceName
	 *            the reference name
	 * @return the revision number of given reference name
	 * @throws InternalErrorException 
	 */
	public static String getRevisionNumber(final String revisionGraph, final String referenceName) throws InternalErrorException {
		String query = prefixes + String.format(""
				+ "SELECT ?revNumber WHERE { GRAPH <%s> {"
				+ "	?rev a rmo:Revision; rmo:revisionNumber ?revNumber."
				+ "	{?rev rmo:revisionNumber \"%s\".} UNION {?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%s\".}"
				+ "} }", 
				revisionGraph, referenceName, referenceName);
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

		String revisionGraph = getRevisionGraph(graphName);
		String queryString = prefixes + String.format(""
				+ "SELECT ?revisionNumber "  
				+ "WHERE { GRAPH <%s> {"
				+ "	?master a rmo:Master; rmo:references ?revision . "
				+ "	?revision rmo:revisionNumber ?revisionNumber . " 
				+ "} }", revisionGraph);
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryString);
		if (results.hasNext()){
			QuerySolution qs = results.next();
			return qs.getLiteral("?revisionNumber").getString();
		}
		else {
			return null;
		}
	}

	/** Returns new unique revision number for specified graph
	 * 
	 * 
	 * @param graphName
	 * @return new revision number
	 * @throws InternalErrorException 
	 */
	public static String getNextRevisionNumber(final String graphName) throws InternalErrorException {
		// create UID and check whether the uid number already in named graph exist, if yes , than create it once again,
		// if not , return this one
		
		//UID nextNumberUid = new UID();
		//String nextNumber = nextNumberUid.toString();
		int nextNumber = 0;
		
		String revisionGraph = getRevisionGraph(graphName);
		String query = prefixes
				+ String.format(
					"SELECT ?nr "
					+ "WHERE { GRAPH <%s> {"
					+ "	?rev a rmo:Revision; rmo:revisionNumber ?nr ."
					+ " } "
					+ "}ORDER BY DESC(xsd:integer(?nr))", revisionGraph);
		try {
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
			QuerySolution qs = results.next();
			nextNumber = qs.getLiteral("?nr").getInt()+1;
		}
		catch (Exception e){
			nextNumber = 0;
		}
		
		int count = 0;
		while (existRevisionNumber(""+nextNumber, revisionGraph)){
			nextNumber++;
			count++;
			if (count==100)
				throw new InternalErrorException("No new revision number found");
		}
		
		return ""+nextNumber;
	}
	
	/**
	 * checks whether the revision number already exist
	 * @param revisionNumber
	 * @return boolean*/
	
	public static boolean existRevisionNumber(final String revisionNumber, final String revisionGraph) {
		String queryASK = prefixes
				+ String.format(""
						+ "ASK {"
						+ "	GRAPH <%1$s> { " 
						+ " 	{ ?rev a rmo:Revision; rmo:revisionNumber \"%2$s\". }"
						+ "		UNION "
						+ "		{?rev a rmo:Revision. ?ref a rmo:Reference; rmo:references ?rev; rdfs:label \"%2$s\" .}"
						+ "} } ",
						revisionGraph, revisionNumber);
		return TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK);
	}
	


	/**
	 * Split huge INSERT statements into separate queries of up to 500 triple
	 * statements.
	 * 
	 * @param graphName
	 *            the graph name
	 * @param dataSetAsNTriples
	 *            the data to insert as N-Triples
	 */
	public static void executeINSERT(final String graphName, final String dataSetAsNTriples) {

		String insertQueryTemplate = "INSERT DATA { GRAPH <%s> { %s } }";
		
		splitAndExecuteBigQuery(graphName, dataSetAsNTriples, insertQueryTemplate);
	}
	
	/**
	 * Split huge DELETE statements into separate queries of up to fifty triple statements.
	 * 
	 * @param graphName the graph name
	 * @param dataSetAsNTriples the data to insert as N-Triples 
	 */
	public static void executeDELETE(final String graphName, final String dataSetAsNTriples) {

		String deleteQueryTemplate = "DELETE DATA { GRAPH <%s> { %s } }";
		
		splitAndExecuteBigQuery(graphName, dataSetAsNTriples, deleteQueryTemplate);
	}
	
	
	
	public static void splitAndExecuteBigQuery(final String graphName, final String dataSetAsNTriples, final String template){
		final int MAX_STATEMENTS = 500;
		String[] lines = dataSetAsNTriples.split("\n");
		int counter = 0;
		StringBuilder insert = new StringBuilder();
		
		for (int i=0; i < lines.length; i++) {
			insert.append(lines[i]);
			insert.append("\n");
			counter++;
			if (counter == MAX_STATEMENTS-1) {
				TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(template, graphName, insert));
				counter = 0;
				insert = new StringBuilder();
			}
		}

		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(template, graphName, insert));
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
			sparqlQuery = prefixes + String.format(""
					+ "CONSTRUCT { ?s ?p ?o. }"
					+ "WHERE { "
					+ "	GRAPH <%s> { ?graph a rmo:Graph; rmo:hasRevisionGraph ?revisiongraph.}"
					+ "	GRAPH ?revisionGraph {?s ?p ?o.}"
					+ "}", Config.revision_graph);
		} else {
			String revisionGraph = getRevisionGraph(graphName);
			sparqlQuery = prefixes + String.format(""
					+ "CONSTRUCT { ?s ?p ?o. }"
					+ "WHERE { GRAPH <%s> { " 
					+ "	?s ?p ?o.}"
					+ "}",
					revisionGraph);
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
						+ " GRAPH <%s> { ?graph a rmo:Graph. }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery, format);
	}
	
	
	/**
	 * Get revised graphs in R43ples.
	 * 
	 * @return result set
	 */
	public static ResultSet getRevisedGraphs() {
		String sparqlQuery = prefixes
				+ String.format("" 
						+ "SELECT DISTINCT ?graph " 
						+ "WHERE {"
						+ " GRAPH <%s> {  ?graph a rmo:Graph }" 
						+ "} ORDER BY ?graph", Config.revision_graph);
		return TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
	}
	

	/**
	 * Get revised graphs in R43ples as list of string.
	 * 
	 * @return list of strings containing the revised graphs of R43ples
	 */
	public static ArrayList<String> getRevisedGraphsList() {
		ArrayList<String> list = new ArrayList<String>();
		ResultSet results = getRevisedGraphs();
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			list.add(qs.getResource("graph").toString());
		}
		return list;
	}
	
	

	/**
	 * Deletes all revision information for a specific named graph including all full
	 * graphs and information in the R43ples system.
	 * 
	 * @param graph
	 *            graph to be purged
	 */
	public static void purgeRevisionInformation(final String graphName) {
		logger.info("Purge revision information of graph " + graphName);
		// Drop all full graphs as well as add and delete sets which are related
		// to specified graph
		String revisionGraph = getRevisionGraph(graphName);
		String query = prefixes	+ String.format(""
				+ "SELECT DISTINCT ?graph "
				+ "WHERE { GRAPH <%s> {"
				+ " {?rev rmo:addSet ?graph}" 
				+ " UNION {?rev rmo:deleteSet ?graph}"
				+ " UNION {?ref rmo:fullGraph ?graph}"
				+ "} }", revisionGraph);
				
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		while (results.hasNext()) {
			QuerySolution qs = results.next();
			if (qs.get("?graph").isResource()) {
					String graph = qs.getResource("graph").toString();
					TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + graph + ">");
					logger.debug("Graph deleted: " + graph);
			}
		}
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("DROP SILENT GRAPH <%s>", revisionGraph));
		
		// Remove information from revision graph
		String queryDelete = prefixes + String.format(
					   	"DELETE { "
						+ "GRAPH <%s> {	<%s> ?p ?o.}"
						+ "}" 
						+ "WHERE {"
						+ "	GRAPH <%s> { <%s> a rmo:Graph; ?p ?o.}" 
						+ "}"
						, Config.revision_graph, graphName, Config.revision_graph, graphName);
		
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
		DateFormat df = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH:mm:ss.SSS");
		String dateString = df.format(date);
		logger.debug("Time stamp created: " + dateString);
		return dateString;
	}

	/** returns the name of the named graph which stores all revision information for the specified revised named graph
	 * 
	 * @param graphName uri of the revised named graph
	 * @return uri of the revision graph for this graph
	 */
	public static String getRevisionGraph(final String graphName) {
		String query = String.format(
				  "SELECT ?revisionGraph "
				+ "WHERE { GRAPH <%s> {"
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#hasRevisionGraph> ?revisionGraph ."
				+ "} }", Config.revision_graph, graphName);
			
			ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
			
			if (results.hasNext()) {
				QuerySolution qs = results.next();
				return qs.getResource("?revisionGraph").toString();
			} else {
				return null;
			}
	}
	
	/**
	 * Check whether the branch name is already used by specified graph name.
	 * 
	 * @param revisionGraph
	 *            the revision graph which stores information about the revised graph
	 * @param referenceName
	 *            the branch name to check
	 * @return true when branch already exists elsewhere false
	 */
	private static boolean checkReferenceNameExistence(final String revisionGraph, final String referenceName) {
		String queryASK = prefixes
				+ String.format("ASK { GRAPH <%s> { ?ref a rmo:Reference; rdfs:label \"%s\".  }} ",
						revisionGraph, referenceName);
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
		String revisionGraph = getRevisionGraph(graphName);
		String queryASK = prefixes
				+ String.format(""
						+ "ASK { GRAPH <%s> { " 
						+ " ?rev a rmo:Revision. "
						+ " ?ref a rmo:Reference; rmo:references ?rev ."
						+ " { ?rev rmo:revisionNumber \"%s\"} UNION { ?ref rdfs:label \"%s\"} }} ",
						revisionGraph, identifier, identifier);
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
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#addSet> ?addSetURI . %n"
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
			+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#deleteSet> ?deleteSetURI . %n"
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
		String query = RevisionManagement.prefixes + String.format(
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
				+ " ?rev rmo:revisionNumber ?number . %n"
				+ "} %n"
				+ "WHERE {"
				+ " GRAPH <%s> {"
				+ "   ?graph a rmo:Graph; rmo:hasRevisionGraph ?revisionGraph."
				+ "   FILTER (?graph IN (%s))"
				+ " }"
				+ " GRAPH ?revisionGraph { "
				+ " ?ref a ?type;"
				+ "		rdfs:label ?label;%n"
				+ "		rmo:references ?rev."
				+ " ?rev rmo:revisionNumber ?number . %n"
				+ "FILTER (?type IN (rmo:Tag, rmo:Master, rmo:Branch)) %n"
				+ "} }", Config.revision_graph, graphList);
		String header = TripleStoreInterfaceSingleton.get().executeConstructQuery(queryConstruct, FileUtils.langTurtle);
		return header;
	}
	
	public static String getFullGraphUri(final String revisionGraph, final String branchURI) {
		String query = String.format(
				  "SELECT ?fullGraphURI %n"
			    + "WHERE { GRAPH <%s> {%n"
				+ "	<%s> <http://eatld.et.tu-dresden.de/rmo#fullGraph> ?fullGraphURI . %n"
				+ "} }", revisionGraph, branchURI);
			
		ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
		
		if (results.hasNext()) {
			QuerySolution qs = results.next();
			return qs.getResource("?fullGraphURI").toString();
		} else {
			return null;
		}
	}
	
	/**
	 * @param graphName
	 * @param sdd
	 * @param sddURI
	 * @return
	 * @throws InternalErrorException
	 */
	public static String getSDD(String graphName, String sdd)
			throws InternalErrorException {
		if (sdd != null && sdd != "") {
			// Specified SDD
			return sdd;
		} else {
			// Default SDD
			// Query the referenced SDD
			String querySDD = String.format(
					  "PREFIX sddo: <http://eatld.et.tu-dresden.de/sddo#> %n"
					+ "PREFIX rmo: <http://eatld.et.tu-dresden.de/rmo#> %n"
					+ "SELECT ?defaultSDD %n"
					+ "WHERE { GRAPH <%s> {	%n"
					+ "	<%s> a rmo:Graph ;%n"
					+ "		sddo:hasDefaultSDD ?defaultSDD . %n"
					+ "} }", Config.revision_graph, graphName);
			
			ResultSet resultSetSDD = TripleStoreInterfaceSingleton.get().executeSelectQuery(querySDD);
			if (resultSetSDD.hasNext()) {
				QuerySolution qs = resultSetSDD.next();
				return qs.getResource("?defaultSDD").toString();
			} else {
				throw new InternalErrorException("Error in revision graph! Selected graph <" + graphName + "> has no default SDD referenced.");
			}
		}
	}


	
}

