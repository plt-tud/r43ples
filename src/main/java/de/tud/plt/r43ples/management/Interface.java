package de.tud.plt.r43ples.management;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.merge.RebaseControl;
import de.tud.plt.r43ples.merging.MergeManagement;
import de.tud.plt.r43ples.merging.MergeQueryTypeEnum;
import de.tud.plt.r43ples.merging.MergeResult;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;

public class Interface {

	/** default logger for this class */
	private final static Logger logger = Logger.getLogger(Interface.class);

	private static final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

	/**
	 * 
	 * @param query
	 *            R43ples query string
	 * @param format
	 *            serialisation format of the result
	 * @param query_rewriting
	 *            option if query rewriting should be enabled
	 * @return string containing result of the query
	 * @throws InternalErrorException
	 */
	public static String sparqlSelectConstructAsk(final String query, final String format,
			final boolean query_rewriting) throws InternalErrorException {
		String result;
		if (query_rewriting) {
			String query_rewritten = SparqlRewriter.rewriteQuery(query);
			result = TripleStoreInterfaceSingleton.get()
					.executeSelectConstructAskQuery(Config.getPrefixes() + query_rewritten, format);
		} else {
			result = getSelectConstructAskResponseClassic(query, format);
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
				"(?<type>FROM|GRAPH)\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"",
				Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

		String queryM = query;

		Matcher m = patternSelectFromPart.matcher(queryM);
		while (m.find()) {
			String graphName = m.group("graph");
			String type = m.group("type");
			String revisionNumber = m.group("revision").toLowerCase();
			String newGraphName;

			// if no revision number is declared use the MASTER as default
			if (revisionNumber == null) {
				revisionNumber = "master";
			}
			if (revisionNumber.equalsIgnoreCase("master")) {
				// Respond with MASTER revision - nothing to be done - MASTER
				// revisions are already created in the named graphs
				newGraphName = graphName;
			} else {
				if (RevisionManagement.isBranch(graphName, revisionNumber)) {
					newGraphName = RevisionManagement.getReferenceGraph(graphName, revisionNumber);
				} else {
					// Respond with specified revision, therefore the revision
					// must be generated - saved in graph <RM-TEMP-graphName>
					newGraphName = graphName + "-temp";
					RevisionManagement.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
				}
			}

			queryM = m.replaceFirst(type + " <" + newGraphName + ">");
			m = patternSelectFromPart.matcher(queryM);

		}
		String response = TripleStoreInterfaceSingleton.get()
				.executeSelectConstructAskQuery(Config.getPrefixes() + queryM, format);
		return response;
	}

	/**
	 * currently not thread-safe
	 * 
	 * @param query
	 * @param user
	 * @param commitMessage
	 * @throws InternalErrorException
	 */
	public static void sparqlUpdate(final String query, final String user, final String commitMessage)
			throws InternalErrorException {

		final Pattern patternUpdateRevision = Pattern.compile("(?<action>INSERT|DELETE)(?<data>\\s*DATA){0,1}\\s*\\{",
				patternModifier);
		final Pattern patternWhere = Pattern.compile("WHERE\\s*\\{", patternModifier);

		final Pattern patternEmptyGraphPattern = Pattern.compile("GRAPH\\s*<(?<graph>[^>]*)>\\s*\\{\\s*\\}",
				patternModifier);
		final Pattern patternGraphWithRevision = Pattern
				.compile("GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*\\{", patternModifier);

		logger.debug("SPARQL Update detected");

		String queryRewritten = "";

		// I. Take over prefixes and other head stuff
		Matcher m = patternUpdateRevision.matcher(query);
		if (m.find()) {
			queryRewritten = query.substring(0, m.start());
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
		m = patternUpdateRevision.matcher(query);
		while (m.find()) {
			String action = m.group("action");
			String updateClause = QueryParser.getStringEnclosedinBraces(query, m.end());

			Matcher m2a = patternGraphWithRevision.matcher(updateClause);
			while (m2a.find()) {
				String graphName = m2a.group("graph");
				String revisionName = m2a.group("revision").toLowerCase();

				if (!RevisionManagement.isBranch(graphName, revisionName)) {
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
				String graphClause = QueryParser.getStringEnclosedinBraces(updateClause, m2a.end());

				if (action.equalsIgnoreCase("INSERT")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.addSetURI, graphClause);
				} else if (action.equalsIgnoreCase("DELETE")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.deleteSetURI, graphClause);
				}
			}
		}
		queryRewritten += "}";

		// III. Rewrite where clause
		Matcher m1 = patternWhere.matcher(query);
		if (m1.find()) {
			queryRewritten += "WHERE {";
			String whereClause = QueryParser.getStringEnclosedinBraces(query, m1.end());

			Matcher m1a = patternGraphWithRevision.matcher(whereClause);
			while (m1a.find()) {
				String graphName = m1a.group("graph");
				String revisionName = m1a.group("revision").toLowerCase();
				// TODO: replace generateFullGraphOfRevision with query
				// rewriting option
				String tempGraphName = graphName + "-temp";
				RevisionManagement.generateFullGraphOfRevision(graphName, revisionName, tempGraphName);
				String GraphClause = QueryParser.getStringEnclosedinBraces(whereClause, m1a.end());
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
		for (RevisionDraft d : revList) {
			RevisionManagement.addNewRevisionFromChangeSet(user, commitMessage, d.graphName, d.revisionName,
					d.newRevisionNumber, d.referenceFullGraph, d.addSetURI, d.deleteSetURI);
		}
	}

	public static String sparqlCreateGraph(final String query) throws QueryErrorException {
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

			if (RevisionManagement.getMasterRevisionNumber(graphName) == null) {
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
			RevisionManagement.purgeRevisionInformation(graphName);
		}
		if (!found) {
			throw new QueryErrorException("Query contain errors:\n" + query);
		}
	}

	public static void sparqlTagOrBranch(final String sparqlQuery, final String user, final String commitMessage)
			throws InternalErrorException, QueryErrorException {
		final Pattern patternBranchOrTagQuery = Pattern.compile(
				"(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
				patternModifier);
		Matcher m = patternBranchOrTagQuery.matcher(sparqlQuery);

		boolean foundEntry = false;
		while (m.find()) {
			foundEntry = true;
			String action = m.group("action");
			String graphName = m.group("graph");
			String revisionNumber = m.group("revision").toLowerCase();
			String referenceName = m.group("name").toLowerCase();
			if (action.equals("TAG")) {
				RevisionManagement.createTag(graphName, revisionNumber, referenceName, user, commitMessage);
			} else if (action.equals("BRANCH")) {
				RevisionManagement.createBranch(graphName, revisionNumber, referenceName, user, commitMessage);
			} else {
				throw new QueryErrorException("Error in query: " + sparqlQuery);
			}
		}
		if (!foundEntry) {
			throw new QueryErrorException("Error in query: " + sparqlQuery);
		}
	}

	public static MergeResult sparqlMerge(final String sparqlQuery, final String user, final String commitMessage,
			final String format) throws InternalErrorException {
		final Pattern patternMergeQuery = Pattern.compile(
				"(?<action>MERGE|REBASE|MERGE FF)\\s*(?<type>AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(?<with>\\s*WITH\\s*\\{(?<triples>.*)\\})?",
				patternModifier);
		Matcher m = patternMergeQuery.matcher(sparqlQuery);

		if (!m.find())
			throw new InternalErrorException("Error in query: " + sparqlQuery);

		final String action = m.group("action");
		final String type = m.group("type");
		final String graphName = m.group("graph");
		final String sdd = m.group("sdd");
		final String branchNameA = m.group("branchNameA").toLowerCase();
		final String branchNameB = m.group("branchNameB").toLowerCase();
		final boolean with = m.group("with")!=null;
		final String triples = m.group("triples");

		if (action.equals("MERGE"))
			return mergeThreeWay(graphName, branchNameA, branchNameB, with, triples, type, sdd, user, commitMessage,
					format);
		else if (action.equals("REBASE"))
			return mergeRebase(graphName, branchNameA, branchNameB, with, triples, type, sdd, user, commitMessage,
					format);
		else if (action.equals("MERGE FF"))
			return mergeFastForward(graphName, branchNameB, branchNameA, user, commitMessage);
		else
			throw new InternalErrorException("Merge Query has errors");
	}

	/**
	 * 
	 * 
	 * @return if fast-forward was successful
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeFastForward(final String graphName, final String branchNameA,
			final String branchNameB, final String user, final String commitMessage) throws InternalErrorException {
		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		MergeResult result = new MergeResult(graphName, branchNameA, branchNameB);
		result.hasConflict = !FastForwardControl.performFastForward(revisionGraph, branchNameA, branchNameB, user,
				RevisionManagement.getDateString(), commitMessage);

		return result;
	}

	/**
	 * 
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * @param with
	 * @param triples
	 * @param type
	 * @param sdd
	 * @param user
	 * @param commitMessage
	 * @param format
	 * @return
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeThreeWay(final String graphName, final String branchNameA, final String branchNameB,
			final boolean with, final String triples, final String type, final String sdd, final String user,
			final String commitMessage, final String format) throws InternalErrorException {

		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		String revisionUriA = RevisionManagement.getRevisionUri(revisionGraph, branchNameA);
		String revisionUriB = RevisionManagement.getRevisionUri(revisionGraph, branchNameB);

		logger.debug("type: " + type);
		logger.debug("graph: " + graphName);
		logger.debug("sdd: " + sdd);
		logger.debug("branchNameA: " + branchNameA);
		logger.debug("branchNameB: " + branchNameB);
		logger.debug("with: " + with);
		logger.debug("triples: " + triples);

		MergeResult mresult = new MergeResult(graphName, branchNameA, branchNameB);

		if (!RevisionManagement.checkGraphExistence(graphName)) {
			logger.warn("Graph <" + graphName + "> does not exist.");
			throw new InternalErrorException("Graph <" + graphName + "> does not exist.");
		}

		// Check if A and B are different revisions
		if (RevisionManagement.getRevisionNumber(revisionGraph, branchNameA)
				.equals(RevisionManagement.getRevisionNumber(revisionGraph, branchNameB))) {
			// Branches are equal - throw error
			throw new InternalErrorException("Specified branches are equal");
		}

		// Check if both are terminal nodes
		if (!(RevisionManagement.isBranch(graphName, branchNameA)
				&& RevisionManagement.isBranch(graphName, branchNameB))) {
			throw new InternalErrorException("Non terminal nodes were used");
		}

		// Differ between MERGE query with specified SDD and without SDD
		String usedSDDURI = RevisionManagement.getSDD(graphName, sdd);

		// Get the common revision with shortest path
		mresult.commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionGraph, revisionUriA,
				revisionUriB);

		// Create the revision progress for A and B
		String graphNameA = graphName + "-RM-REVISION-PROGRESS-A";
		String graphNameB = graphName + "-RM-REVISION-PROGRESS-B";
		String graphNameDiff = graphName + "-RM-DIFFERENCE-MODEL";
		mresult.graphDiff = graphNameDiff;
		String uriA = "http://eatld.et.tu-dresden.de/branch-A";
		String uriB = "http://eatld.et.tu-dresden.de/branch-B";

		MergeManagement.createRevisionProgresses(revisionGraph, graphName,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, mresult.commonRevision,revisionUriA),
				graphNameA, uriA, 
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph,	mresult.commonRevision, revisionUriB),
				graphNameB, uriB);

		// Create difference model
		MergeManagement.createDifferenceTripleModel(graphName, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
				usedSDDURI);

		// Differ between the different merge queries
		if ((type != null) && (type.equalsIgnoreCase("AUTO")) && !with) {
			logger.debug("AUTO MERGE query detected");
			// Create the merged revision
			mresult.newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user,
					commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
					MergeQueryTypeEnum.AUTO, "");
		} else if ((type != null) && (type.equalsIgnoreCase("MANUAL")) && with) {
			logger.debug("MANUAL MERGE query detected");
			// Create the merged revision
			mresult.newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user,
					commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
					MergeQueryTypeEnum.MANUAL, triples);
		} else if ((type == null) && with) {
			logger.debug("MERGE WITH query detected");
			// Create the merged revision
			mresult.newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB, user,
					commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
					MergeQueryTypeEnum.WITH, triples);
		} else if ((type == null) && !with) {
			logger.debug("MERGE query detected");
			// Check if difference model contains conflicts
			String queryASK = String.format("ASK { %n" + "	GRAPH <%s> { %n"
					+ " 	?ref <http://eatld.et.tu-dresden.de/sddo#isConflicting> \"true\"^^<http://www.w3.org/2001/XMLSchema#boolean> . %n"
					+ "	} %n" + "}", graphNameDiff);
			if (TripleStoreInterfaceSingleton.get().executeAskQuery(queryASK)) {
				// Difference model contains conflicts
				// Return the conflict model to the client
				mresult.hasConflict = true;
				mresult.conflictModel = RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format);

			} else {
				// Difference model contains no conflicts
				// Create the merged revision
				mresult.newRevisionNumber = MergeManagement.createMergedRevision(graphName, branchNameA, branchNameB,
						user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB, usedSDDURI,
						MergeQueryTypeEnum.COMMON, "");
			}
		} else {
			throw new InternalErrorException("This is not a valid MERGE query");
		}
		return mresult;
	}

	/**
	 * 
	 * @param graphName
	 * @param branchNameA
	 * @param branchNameB
	 * @param with
	 * @param triples
	 * @param type
	 * @param sdd
	 * @param user
	 * @param commitMessage
	 * @param format
	 * @return
	 * @throws InternalErrorException
	 */
	public static MergeResult mergeRebase(final String graphName, final String branchNameA, final String branchNameB,
			final boolean with, final String triples, final String type, final String sdd, final String user,
			final String commitMessage, final String format) throws InternalErrorException {

		String revisionGraph = RevisionManagement.getRevisionGraph(graphName);
		String revisionUriA = RevisionManagement.getRevisionUri(revisionGraph, branchNameA);
		String revisionUriB = RevisionManagement.getRevisionUri(revisionGraph, branchNameB);

		logger.debug("type: " + type);
		logger.debug("graph: " + graphName);
		logger.debug("sdd: " + sdd);
		logger.debug("branchNameA: " + branchNameA);
		logger.debug("branchNameB: " + branchNameB);
		logger.debug("with: " + with);
		logger.debug("triples: " + triples);

		MergeResult mresult = new MergeResult(graphName, branchNameA, branchNameB);

		RebaseControl rebaseControl =  new RebaseControl(graphName, branchNameA, branchNameB);
		rebaseControl.checkIfRebaseIsPossible();

		// Differ between MERGE query with specified SDD and without SDD
		String usedSDDURI = RevisionManagement.getSDD(graphName, sdd);

		// Get the common revision with shortest path
		String commonRevision = MergeManagement.getCommonRevisionWithShortestPath(revisionGraph, revisionUriA,
				revisionUriB);

		// create the patch and patch group
		LinkedList<String> revisionList = MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph,
				commonRevision, revisionUriA);

		rebaseControl.createPatchGroupOfBranch(revisionGraph, revisionUriB, revisionList);

		// Create the revision progress for A and B
		String graphNameA = graphName + "-RM-REVISION-PROGRESS-A";
		String graphNameB = graphName + "-RM-REVISION-PROGRESS-B";
		String graphNameDiff = graphName + "-RM-DIFFERENCE-MODEL";
		String uriA = "http://eatld.et.tu-dresden.de/branch-A";
		String uriB = "http://eatld.et.tu-dresden.de/branch-B";

		MergeManagement.createRevisionProgresses(revisionGraph, graphName,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriA),
				graphNameA, uriA,
				MergeManagement.getPathBetweenStartAndTargetRevision(revisionGraph, commonRevision, revisionUriB),
				graphNameB, uriB);

		// Create difference model
		MergeManagement.createDifferenceTripleModel(graphName, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
				usedSDDURI);
		
		if ((type != null) && (type.equalsIgnoreCase("AUTO")) && !with) {
			logger.info("AUTO REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName,
					branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.AUTO, "");
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples, user, commitMessage,
					basisRevisionNumber);
			mresult.graphStrategy = "auto-rebase";
		} else if ((type != null) && (type.equalsIgnoreCase("MANUAL")) && !with) {
			logger.info("MANUAL REBASE query detected");
			// Create the merged revision
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName,
					branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.MANUAL, triples);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples, user, commitMessage,
					basisRevisionNumber);
			mresult.graphStrategy = "manual-rebase";
		} else if ((type == null) && with) {
			logger.info("REBASE WITH query detected");
			// Create the merged revision -- newTriples
			ArrayList<String> addedAndRemovedTriples = MergeManagement.createRebaseMergedTripleList(graphName,
					branchNameA, branchNameB, user, commitMessage, graphNameDiff, graphNameA, uriA, graphNameB, uriB,
					usedSDDURI, MergeQueryTypeEnum.WITH, triples);
			String addedAsNTriples = addedAndRemovedTriples.get(0);
			String removedAsNTriples = addedAndRemovedTriples.get(1);

			String basisRevisionNumber = rebaseControl.forceRebaseProcess();
			RevisionManagement.createNewRevision(graphName, addedAsNTriples, removedAsNTriples, user, commitMessage,
					basisRevisionNumber);

			mresult.graphStrategy = "with-rebase";
		} else if ((type == null) && !with) {
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
			mresult.conflictModel = RevisionManagement.getContentOfGraphByConstruct(graphNameDiff, format);

		} else {
			throw new InternalErrorException("This is not a valid MERGE query");
		}

		return mresult;

	}

}
