package de.tud.plt.r43ples.management;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tud.plt.r43ples.draftobjects.R43plesCoreInterface;
import de.tud.plt.r43ples.draftobjects.R43plesCoreSingleton;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.merging.FastForwardControl;
import de.tud.plt.r43ples.merging.MergeManagement;
import de.tud.plt.r43ples.merging.MergeQueryTypeEnum;
import de.tud.plt.r43ples.merging.MergeResult;
import de.tud.plt.r43ples.merging.RebaseControl;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class Interface {

	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Interface.class);

	private static final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

	/**
	 * 
//	 * @param query
//	 *            R43ples query string
//	 * @param format
//	 *            serialization format of the result
	 * @param query_rewriting
	 *            option if query rewriting should be enabled
	 * @return string containing result of the query
	 * @throws InternalErrorException
	 */
	public static String sparqlSelectConstructAsk(final R43plesRequest request,
			final boolean query_rewriting) throws InternalErrorException {
		String result;
		if (query_rewriting) {
			String query_rewritten = SparqlRewriter.rewriteQuery(request.query_sparql);
			result = TripleStoreInterfaceSingleton.get()
					.executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + query_rewritten, request.format);
		} else {
			result = getSelectConstructAskResponseClassic(request.query_sparql, request.format);
		}
		return result;
	}

	/**
	 * @param query
	 * @param format
	 * @return
	 * @throws InternalErrorException
	 */
	private static String getSelectConstructAskResponseClassic(final String query, final String format)
			throws InternalErrorException {
		final Pattern patternSelectFromPart = Pattern.compile(
				"(?<type>FROM|GRAPH)\\s*<(?<graph>[^>\\?]*)(\\?|>)(\\s*REVISION\\s*\"|revision=)(?<revision>([^\">]+))(>|\")",
				Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

		String queryM = query;

		Matcher m = patternSelectFromPart.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String type = m.group("type");
			String revisionNumber = m.group("revision").toLowerCase();
			String newGraphName;

			RevisionGraph graph = new RevisionGraph(graphName);
					
			// if no revision number is declared use the MASTER as default
			if (revisionNumber == null) {
				revisionNumber = "master";
			}
			if (revisionNumber.equalsIgnoreCase("master")) {
				// Respond with MASTER revision - nothing to be done - MASTER
				// revisions are already created in the named graphs
				newGraphName = graphName;
			} else {
				if (graph.hasBranch(revisionNumber)) {
					newGraphName = graph.getReferenceGraph(revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision
					// must be generated - saved in graph <graphName-revisionNumber>
					newGraphName = graphName + "-" + revisionNumber;
					RevisionManagementOriginal.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
			}

			queryM = m.replaceFirst(type + " <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);

		}
		String response = TripleStoreInterfaceSingleton.get()
				.executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + queryM, format);
		return response;
	}



	public static void sparqlDropGraph(final String query) throws QueryErrorException {
		final Pattern patternDropGraph = Pattern.compile("DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
				patternModifier);
		Matcher m = patternDropGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
			String graphName = m.group("graph");
			RevisionGraph graph = new RevisionGraph(graphName);
			graph.purgeRevisionInformation();
		}
		if (!found) {
			throw new QueryErrorException("Query contain errors:\n" + query);
		}
	}
//
//	public static MergeResult sparqlMerge(final R43plesMergeCommit commit) throws InternalErrorException {
//		if (commit.action.equals("MERGE"))
//			return mergeThreeWay(commit);
//		else if (commit.action.equals("REBASE"))
//			return mergeRebase(commit);
//		else if (commit.action.equals("MERGE FF"))
//			return mergeFastForward(commit);
//		else
//			throw new InternalErrorException("Merge Query has errors");
//	}

	/**
	 * 
	 * 
	 * @return if fast-forward was successful
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeFastForward(final R43plesMergeCommit commit) throws InternalErrorException {
		RevisionGraph graph = new RevisionGraph(commit.graphName);
		MergeResult result = new MergeResult(commit);
		// TODO Create date by using commit information
		String dateString = "2017-08-17T13:57:11";
		result.hasConflict = !FastForwardControl.performFastForward(graph, commit.branchNameB, commit.branchNameA, commit.user,
				dateString, commit.message);

		return result;
	}

	/**
	 * 
	 * @param commit
	 * @return
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeRebase(final R43plesMergeCommit commit) throws InternalErrorException {

		RevisionGraph graph = new RevisionGraph(commit.graphName);
		String revisionGraph = graph.getRevisionGraphUri();
		Revision revisionA = graph.getRevision(commit.branchNameA);
		Revision revisionB = graph.getRevision(commit.branchNameB);
		String revisionUriA = revisionA.getRevisionURI();
		String revisionUriB = revisionB.getRevisionURI();

		MergeResult mresult = new MergeResult(commit);

		RebaseControl rebaseControl =  new RebaseControl(commit);
		rebaseControl.checkIfRebaseIsPossible();

		// Differ between MERGE query with specified SDD and without SDD
		String usedSDDURI = graph.getSDD(commit.sdd);

		// Get the common revision with shortest path
		//TODO change when restructured to interface - only test of interface design
		String commonRevision = PathCalculationSingleton.getInstance()
                .getCommonRevisionWithShortestPath(graph, revisionA, revisionB)
                .getRevisionURI();

//		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionGraph, revisionUriA,
//				revisionUriB);

		// create the patch and patch group
		LinkedList<String> revisionList = MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph,
				commonRevision, revisionUriA);

		rebaseControl.createPatchGroupOfBranch(revisionGraph, revisionUriB, revisionList);

		// Create the revision progress for A and B
		String graphNameA = commit.graphName + "-RM-REVISION-PROGRESS-A";
		String graphNameB = commit.graphName + "-RM-REVISION-PROGRESS-B";
		String graphNameDiff = commit.graphName + "-RM-DIFFERENCE-MODEL";
		String uriA = "http://eatld.et.tu-dresden.de/branch-A";
		String uriB = "http://eatld.et.tu-dresden.de/branch-B";

		//TODO Change parameter to Path
//		MergeManagement.createRevisionProgresses(revisionGraph, commit.graphName,
//				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriA),
//				graphNameA, uriA,
//				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriB),
//				graphNameB, uriB);

		// Create difference model
		MergeManagement.createDifferenceTripleModel(commit.graphName, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
				usedSDDURI);
		
		if ((commit.type != null) && (commit.type.equalsIgnoreCase("AUTO")) && !commit.with) {
			logger.info("AUTO REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.AUTO);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			//TODO This will change because of the new Commit classes
//			RevisionManagementOriginal.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,	basisRevisionNumber);
			mresult.graphStrategy = "auto-rebase";
		} else if ((commit.type != null) && (commit.type.equalsIgnoreCase("MANUAL")) && !commit.with) {
			logger.info("MANUAL REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.MANUAL);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			//TODO This will change because of the new Commit classes
//			RevisionManagementOriginal.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
//					basisRevisionNumber);
			mresult.graphStrategy = "manual-rebase";
		} else if ((commit.type == null) && commit.with) {
			logger.info("REBASE WITH query detected");
			// Create the merged revision -- newTriples
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.WITH);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			//TODO This will change because of the new Commit classes
//			RevisionManagementOriginal.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
//					basisRevisionNumber);

			mresult.graphStrategy = "with-rebase";
		} else if ((commit.type == null) && !commit.with) {
			logger.info("COMMON REBASE query detected");
			// Check if difference model contains conflicts
			String queryASK = String.format("ASK { %n" + "	GRAPH <%s> { %n"
					+ " 	?ref <http://eatld.et.tu-dresden.de/sddo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
					+ "	} %n" + "}", graphNameDiff);
			if (TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK)) {
				mresult.hasConflict = true;
			} else {
				rebaseControl.forceRebaseProcess();
			}
			mresult.conflictModel = RevisionManagementOriginal.getContentOfGraph(graphNameDiff, commit.format);

		} else {
			throw new InternalErrorException("This is not a valid MERGE query");
		}

		return mresult;
	}


}
