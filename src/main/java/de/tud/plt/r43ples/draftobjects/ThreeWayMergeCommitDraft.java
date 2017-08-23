package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.management.R43plesRequest;

/**
 * Collection of information for creating a new three way merge commit.
 *
 * @author Stephan Hensel
 */
public class ThreeWayMergeCommitDraft extends MergeCommitDraft {


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     */
    public ThreeWayMergeCommitDraft(R43plesRequest request) throws OutdatedException {
        super(request);
    }

    // TODO

}


//	/**
//	 * Create a new revision with multiple prior revisions
//	 *
//	 * @param graphName
//	 *            the graph name
//	 * @param addedAsNTriples
//	 *            the data set of added triples as N-Triples
//	 * @param removedAsNTriples
//	 *            the data set of removed triples as N-Triples
//	 * @param user
//	 *            the user name who creates the revision
//	 * @param commitMessage
//	 *            the title of the revision
//	 * @param usedRevisionNumber
//	 *            the number of the revision which is used for creation of the
//	 *            new revision
//	 *            (for creation of merged maximal two revision are  allowed
//	 *            - the first revision in array list specifies the branch where the merged revision will be created)
//	 * @return new revision number
//	 * @throws InternalErrorException
//	 */
//	public static String createNewRevision(final String graphName, final String addedAsNTriples,
//			final String removedAsNTriples, final String user, final String timeStamp, final String commitMessage,
//			final ArrayList<String> usedRevisionNumber) throws InternalErrorException {
//		logger.info("Create new revision for graph " + graphName);
//
//		// General variables
//		RevisionDraft draft = new RevisionDraft(graphName, usedRevisionNumber.get(0) );
//
//		// Add Meta Information
//		addMetaInformationForNewRevision(draft, user, timeStamp, commitMessage);
//
//		// Update full graph of branch
//		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
//			RevisionManagementOriginal.executeDELETE(draft.referenceFullGraph, removedAsNTriples);
//		}
//		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
//			RevisionManagementOriginal.executeINSERT(draft.referenceFullGraph, addedAsNTriples);
//		}
//
//		// Create new graph with addSet-newRevisionNumber
//		if (addedAsNTriples!=null && !addedAsNTriples.isEmpty()) {
//			logger.debug("Create new graph with name " + draft.addSetURI);
//			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n",
//					draft.addSetURI));
//			RevisionManagementOriginal.executeINSERT(draft.addSetURI, addedAsNTriples);
//		}
//
//		// Create new graph with deleteSet-newRevisionNumber
//		if (removedAsNTriples!=null && !removedAsNTriples.isEmpty()) {
//			logger.debug("Create new graph with name " + draft.deleteSetURI);
//			TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n",
//					draft.deleteSetURI));
//			RevisionManagementOriginal.executeINSERT(draft.deleteSetURI, removedAsNTriples);
//		}
//
//		// Remove branch from which changes were merged, if available
////		if (usedRevisionNumber.size() > 1) {
////			String oldRevision2 = graphName + "-revision-" + usedRevisionNumber.get(1).toString();
////			String queryBranch2 = prefixes
////					+ String.format(
////							"SELECT ?branch ?graph WHERE{ ?branch a rmo:Branch; rmo:references <%s>; rmo:fullGraph ?graph. }",
////							oldRevision2);
////			QuerySolution sol2 = ResultSetFactory.fromXML(
////					TripleStoreInterfaceSingleton.get().executeQueryWithAuthorization(queryBranch2, "XML")).next();
////			String removeBranchUri = sol2.getResource("?branch").toString();
////			String removeBranchFullGraph = sol2.getResource("?graph").toString();
////			String query = String.format(
////					"DELETE { GRAPH <%s> { <%s> ?p ?o. } } WHERE { GRAPH <%s> { <%s> ?p ?o. }}%n",
////					Config.revision_graph, removeBranchUri, Config.revision_graph, removeBranchUri);
////			query += String.format("DROP SILENT GRAPH <%s>%n", removeBranchFullGraph);
////			TripleStoreInterfaceSingleton.get().executeQueryWithAuthorization(query);
////		}
//
//		return draft.newRevisionNumber;
//	}