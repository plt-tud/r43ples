package de.tud.plt.r43ples.management;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tud.plt.r43ples.objects.Revision;
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
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
			}

			queryM = m.replaceFirst(type + " <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);

		}
		String response = TripleStoreInterfaceSingleton.get()
				.executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + queryM, format);
		return response;
	}

	/**
	 * currently not thread-safe
	 * 
//	 * @param query
//	 * @param user
//	 * @param message
	 * @throws InternalErrorException
	 */
	public static void sparqlUpdate(R43plesCommit commit)
			throws InternalErrorException {

		final Pattern patternUpdateRevision = Pattern.compile("(?<action>INSERT|DELETE)(?<data>\\s*DATA)?\\s*\\{",
				patternModifier);
		final Pattern patternWhere = Pattern.compile("WHERE\\s*\\{", patternModifier);

		final Pattern patternEmptyGraphPattern = Pattern.compile("GRAPH\\s*<(?<graph>[^>]*)>\\s*\\{\\s*\\}",
				patternModifier);
		final Pattern patternGraphWithRevision = Pattern
				.compile("GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*\\{", patternModifier);

		logger.debug("SPARQL Update detected");


		// I. Take over prefixes and other head stuff
		String queryRewritten;
		Matcher m = patternUpdateRevision.matcher(commit.query_sparql);
		if (m.find()) {
			queryRewritten = commit.query_sparql.substring(0, m.start());
			if (m.group("data") != null)
				queryRewritten += "INSERT DATA {";
			else
				queryRewritten += "INSERT {";
		} else {
			throw new InternalErrorException("No R43ples update query detected.");
		}

		// II. Rewrite INSERT and DELETE clauses (replace graph names in query
		// with change set graph names)
		List<RevisionDraft> revList = new LinkedList<RevisionDraft>();
		m = patternUpdateRevision.matcher(commit.query_sparql);
		while (m.find()) {
			String action = m.group("action");
			String updateClause = getStringEnclosedinBraces(commit.query_sparql, m.end());

			Matcher m2a = patternGraphWithRevision.matcher(updateClause);
			while (m2a.find()) {
				String graphName = m2a.group("graph");
				String revisionName = m2a.group("revision").toLowerCase();

				RevisionGraph graph = new RevisionGraph(graphName);
				if (!graph.hasBranch(revisionName)) {
					throw new InternalErrorException("Revision is not referenced by a branch");
				}
				RevisionDraft d = null;
				for (RevisionDraft draft : revList) {
					if (draft.equals(graphName, revisionName))
						d = draft;
				}
				if (d == null) {
					d = new RevisionDraft(graphName, revisionName);
					revList.add(d);
				}
				String graphClause = getStringEnclosedinBraces(updateClause, m2a.end());

				if (action.equalsIgnoreCase("INSERT")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.addSetURI, graphClause);
				} else if (action.equalsIgnoreCase("DELETE")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.deleteSetURI, graphClause);
				}
			}
		}
		queryRewritten += "}";

		// III. Rewrite where clause
		Matcher m1 = patternWhere.matcher(commit.query_sparql);
		if (m1.find()) {
			queryRewritten += "WHERE {";
			String whereClause = getStringEnclosedinBraces(commit.query_sparql, m1.end());

			Matcher m1a = patternGraphWithRevision.matcher(whereClause);
			while (m1a.find()) {
				String graphName = m1a.group("graph");
				String revisionName = m1a.group("revision").toLowerCase();
				// TODO: replace generateFullGraphOfRevision with query
				// rewriting option
				String tempGraphName = graphName + "-temp";
				RevisionManagement.generateFullGraphOfRevision(graphName, revisionName, tempGraphName);
				String GraphClause = getStringEnclosedinBraces(whereClause, m1a.end());
				queryRewritten += String.format("GRAPH <%s> { %s }", tempGraphName, GraphClause);
			}
			queryRewritten += "}";
		}

		logger.debug("Rewritten query for update: " + queryRewritten);

		// (IIIa) Remove empty insert clauses which otherwise will lead to
		// errors
		m = patternEmptyGraphPattern.matcher(queryRewritten);
		queryRewritten = m.replaceAll("");

		// IV. Execute rewritten query (updating changesets)
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRewritten);

		// V. add changesets to full graph and add meta information in revision
		// graphs
		for (RevisionDraft draft : revList) {
			RevisionManagement.addNewRevisionFromChangeSet(commit.user, commit.message, draft);
		}
	}

	public static String sparqlCreateGraph(final String query) throws QueryErrorException, InternalErrorException {
		final Pattern patternCreateGraph = Pattern.compile("CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
				patternModifier);
		String graphName = null;
		Matcher m = patternCreateGraph.matcher(query);
		boolean found = false;
		while (m.find()) {
			found = true;
			graphName = m.group("graph");
			// String silent = m.group("silent");

			// Create graph
			TripleStoreInterfaceSingleton.get().executeCreateGraph(graphName);

			RevisionGraph graph = new RevisionGraph(graphName);
			if (graph.getMasterRevision() == null) {
				// Add R43ples information
				RevisionManagement.putGraphUnderVersionControl(graphName);
			}
		}
		if (!found) {
			throw new QueryErrorException("Query doesn't contain a correct CREATE query:\n" + query);
		}
		return graphName;
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

	public static void sparqlTagOrBranch(final R43plesCommit commit)
			throws InternalErrorException, QueryErrorException {
		final Pattern patternBranchOrTagQuery = Pattern.compile(
				"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
				patternModifier);
		Matcher m = patternBranchOrTagQuery.matcher(commit.query_sparql);

		boolean foundEntry = false;
		while (m.find()) {
			foundEntry = true;
			String action = m.group("action");
			String graphName = m.group("graph");
			String revisionNumber = m.group("revision").toLowerCase();
			String referenceName = m.group("name").toLowerCase();
			if (action.equals("TAG")) {
				RevisionManagement.createTag(graphName, revisionNumber, referenceName, commit.user, commit.message);
			} else if (action.equals("BRANCH")) {
				RevisionManagement.createBranch(graphName, revisionNumber, referenceName, commit.user, commit.message);
			} else {
				throw new QueryErrorException("Error in query: " + commit.query_sparql);
			}
		}
		if (!foundEntry) {
			throw new QueryErrorException("Error in query: " + commit.query_sparql);
		}
	}

	public static MergeResult sparqlMerge(final R43plesMergeCommit commit) throws InternalErrorException {
		if (commit.action.equals("MERGE"))
			return mergeThreeWay(commit);
		else if (commit.action.equals("REBASE"))
			return mergeRebase(commit);
		else if (commit.action.equals("MERGE FF"))
			return mergeFastForward(commit);
		else
			throw new InternalErrorException("Merge Query has errors");
	}

	/**
	 * 
	 * 
	 * @return if fast-forward was successful
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeFastForward(final R43plesMergeCommit commit) throws InternalErrorException {
		RevisionGraph graph = new RevisionGraph(commit.graphName);
		MergeResult result = new MergeResult(commit);
		result.hasConflict = !FastForwardControl.performFastForward(graph, commit.branchNameB, commit.branchNameA, commit.user,
				RevisionManagement.getDateString(), commit.message);

		return result;
	}

	/**
	 * 
//	 * @param graphName
//	 * @param branchNameA
//	 * @param branchNameB
//	 * @param with
//	 * @param triples
//	 * @param type
//	 * @param sdd
//	 * @param user
//	 * @param commitMessage
//	 * @param format
	 * @return
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeThreeWay(final R43plesMergeCommit commit) throws InternalErrorException {

		RevisionGraph graph = new RevisionGraph(commit.graphName);
		String revisionGraph = graph.getRevisionGraphUri();
		String revisionUriA = graph.getRevisionUri(commit.branchNameA);
		String revisionUriB = graph.getRevisionUri(commit.branchNameB);

		MergeResult mresult = new MergeResult(commit);

		if (!RevisionManagement.checkGraphExistence(commit.graphName)) {
			logger.warn("Graph <" + commit.graphName + "> does not exist.");
			throw new InternalErrorException("Graph <" + commit.graphName + "> does not exist.");
		}

		// Check if A and B are different revisions
		if (graph.getRevisionNumber(commit.branchNameA)
				.equals(graph.getRevisionNumber(commit.branchNameB))) {
			// Branches are equal - throw error
			throw new InternalErrorException("Specified branches are equal");
		}

		// Check if both are terminal nodes
		if (!(graph.hasBranch(commit.branchNameA)
				&& graph.hasBranch(commit.branchNameB))) {
			throw new InternalErrorException("No terminal nodes were used");
		}

		// Differ between MERGE query with specified SDD and without SDD
		String usedSDDURI = graph.getSDD(commit.sdd);

		// Get the common revision with shortest path
		//TODO change when restructured to interface - only test of interface design
		mresult.commonRevision = PathCalculationSingleton.getInstance().getCommonRevisionWithShortestPath(graph, new Revision(graph, revisionUriA, false), new Revision(graph, revisionUriB, false)).getRevisionURI();
//		mresult.commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionGraph, revisionUriA,
//				revisionUriB);

		// Create the revision progress for A and B
		String graphNameA = commit.graphName + "-RM-REVISION-PROGRESS-A";
		String graphNameB = commit.graphName + "-RM-REVISION-PROGRESS-B";
		String graphNameDiff = commit.graphName + "-RM-DIFFERENCE-MODEL";
		mresult.graphDiff = graphNameDiff;
		String uriA = "http://eatld.et.tu-dresden.de/branch-A";
		String uriB = "http://eatld.et.tu-dresden.de/branch-B";

		MergeManagement.createRevisionProgresses(revisionGraph, commit.graphName,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, mresult.commonRevision,revisionUriA),
				graphNameA, uriA, 
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph,	mresult.commonRevision, revisionUriB),
				graphNameB, uriB);

		// Create difference model
		MergeManagement.createDifferenceTripleModel(commit.graphName, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
				usedSDDURI);

		// Differ between the different merge queries
		if ((commit.type != null) && (commit.type.equalsIgnoreCase("AUTO")) && !commit.with) {
			logger.debug("AUTO MERGE query detected");
			// Create the merged revision
			mresult.newRevisionNumber = MergeManagement.createMergedRevision(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
					MergeQueryTypeEnum.AUTO);
		} else if ((commit.type != null) && (commit.type.equalsIgnoreCase("MANUAL")) && commit.with) {
			logger.debug("MANUAL MERGE query detected");
			// Create the merged revision
			mresult.newRevisionNumber = MergeManagement.createMergedRevision(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
					MergeQueryTypeEnum.MANUAL);
		} else if ((commit.type == null) && commit.with) {
			logger.debug("MERGE WITH query detected");
			// Create the merged revision
			mresult.newRevisionNumber = MergeManagement.createMergedRevision(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
					MergeQueryTypeEnum.WITH);
		} else if ((commit.type == null) && !commit.with) {
			logger.debug("MERGE query detected");
			// Check if difference model contains conflicts
			String queryASK = String.format("ASK { %n" + "	GRAPH <%s> { %n"
					+ " 	?ref <http://eatld.et.tu-dresden.de/sddo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
					+ "	} %n" + "}", graphNameDiff);
			if (TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK)) {
				// Difference model contains conflicts
				// Return the conflict model to the client
				mresult.hasConflict = true;
				mresult.conflictModel = RevisionManagement.getContentOfGraph(graphNameDiff, commit.format);

			} else {
				// Difference model contains no conflicts
				// Create the merged revision
				mresult.newRevisionNumber = MergeManagement.createMergedRevision(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
						MergeQueryTypeEnum.COMMON);
			}
		} else {
			throw new InternalErrorException("This is not a valid MERGE query");
		}
		return mresult;
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

		MergeManagement.createRevisionProgresses(revisionGraph, commit.graphName,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriA),
				graphNameA, uriA,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriB),
				graphNameB, uriB);

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
			RevisionManagement.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
					basisRevisionNumber);
			mresult.graphStrategy = "auto-rebase";
		} else if ((commit.type != null) && (commit.type.equalsIgnoreCase("MANUAL")) && !commit.with) {
			logger.info("MANUAL REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.MANUAL);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			RevisionManagement.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
					basisRevisionNumber);
			mresult.graphStrategy = "manual-rebase";
		} else if ((commit.type == null) && commit.with) {
			logger.info("REBASE WITH query detected");
			// Create the merged revision -- newTriples
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(commit, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.WITH);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			RevisionManagement.createNewRevision(commit.graphName, addedAsNTriples, removedAsNTriples, commit.user, commit.message,
					basisRevisionNumber);

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
			mresult.conflictModel = RevisionManagement.getContentOfGraph(graphNameDiff, commit.format);

		} else {
			throw new InternalErrorException("This is not a valid MERGE query");
		}

		return mresult;
	}

	private static String getStringEnclosedinBraces(final String string, int start_pos){
		int end_pos = start_pos;
		int count_parenthesis = 1;
		while (count_parenthesis>0) {
			end_pos++;
			char ch = string.charAt(end_pos);
			if (ch=='{') count_parenthesis++;
			if (ch=='}') count_parenthesis--;
		}
		String substring = string.substring(start_pos, end_pos);
		return substring;
	}

}
