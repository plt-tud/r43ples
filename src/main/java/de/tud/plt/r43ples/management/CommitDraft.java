package de.tud.plt.r43ples.management;

import com.hp.hpl.jena.query.QuerySolution;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.objects.Commit;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.tud.plt.r43ples.management.RevisionManagement.getDateString;

/**
 * Collection of information for creating a new commit.
 *
 * @author Markus Graube
 * @author Stephan Hensel
 */
public class CommitDraft {

	/** The logger. **/
	private Logger logger = Logger.getLogger(CommitDraft.class);

	/** The pattern modifier. **/
	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
	/** The pattern to extract the user name. **/
	private final Pattern patternUser = Pattern.compile(
			"USER\\s*\"(?<user>[^\"]*)\"",
			patternModifier);
	/** The pattern to extract the commit message. **/
	private final Pattern patternCommitMessage = Pattern.compile(
			"MESSAGE\\s*\"(?<message>[^\"]*)\"",
			patternModifier);

	/** The corresponding R43ples request. **/
	private R43plesRequest request;
	/** The revision draft. **/
	private RevisionDraft revisionDraft;
	/** The associated user name of the commit. **/
	private String user;
	/** The message of the commit. **/
	private String message;
	/** The time stamp. **/
	private String timeStamp;


	/**
	 * The constructor.
	 *
	 * @param request the request received by R43ples
	 */
	public CommitDraft(R43plesRequest request){
		this.request = request;
		this.extractUser();
		this.extractMessage();
	}

	/**
	 * The constructor.
	 *
	 *
	 */
	public CommitDraft(String graphName, String addSet, String deleteSet, String user, String timeStamp, String message, String derivedFromRevisionIdentifier) throws InternalErrorException {
		this.revisionDraft = new RevisionDraft(new RevisionGraph(graphName), derivedFromRevisionIdentifier, addSet, deleteSet);
		this.user = user;
		this.message = message;
		this.timeStamp = timeStamp;

		this.request = null;
	}

	/**
	 * Extracts the user name out of the given query.
	 *
	 * @return the extracted user name
	 */
	private String extractUser() {
		Matcher userMatcher = patternUser.matcher(request.query_sparql);
		if (userMatcher.find()) {
			user = userMatcher.group("user");
			request.query_sparql = userMatcher.replaceAll("");
		}
		return user;
	}

	/**
	 * Extracts the message out of the given query.
	 *
	 * @return the extracted message
	 */
	private String extractMessage() {
		Matcher messageMatcher = patternCommitMessage.matcher(request.query_sparql);
		if (messageMatcher.find()) {
			message = messageMatcher.group("message");
			request.query_sparql = messageMatcher.replaceAll("");
		}	
		return message;
	}


	public boolean equals(String graphName, String revisionName) {
		//TODO (draft.equals(graphName, revisionName))
		return true;
	}

	/**
	 * Creates the commit draft as a new commit in the triplestore and creates the corresponding revision.
	 *
	 * @return the created commit
	 */
	public Commit createCommitInTripleStore() throws InternalErrorException {

		if (request != null) {
			updateChangeSets();
		} else {
			revisionDraft.createRevisionInTripleStore();
			addNewRevisionFromChangeSet(revisionDraft);
		}

		// TODO multiple commits are also possible
		return null;
	}


	private void updateChangeSets() throws InternalErrorException {

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
		Matcher m = patternUpdateRevision.matcher(request.query_sparql);
		if (m.find()) {
			queryRewritten = request.query_sparql.substring(0, m.start());
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
		m = patternUpdateRevision.matcher(request.query_sparql);
		while (m.find()) {
			String action = m.group("action");
			String updateClause = getStringEnclosedinBraces(request.query_sparql, m.end());

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
					d = new RevisionDraft(graph, revisionName);
					revList.add(d);
				}
				String graphClause = getStringEnclosedinBraces(updateClause, m2a.end());

				if (action.equalsIgnoreCase("INSERT")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.getAddSetURI(), graphClause);
				} else if (action.equalsIgnoreCase("DELETE")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.getDeleteSetURI(), graphClause);
				}
			}
		}
		queryRewritten += "}";

		// III. Rewrite where clause
		Matcher m1 = patternWhere.matcher(request.query_sparql);
		if (m1.find()) {
			queryRewritten += "WHERE {";
			String whereClause = getStringEnclosedinBraces(request.query_sparql, m1.end());

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
			//TODO
			addNewRevisionFromChangeSet(draft);
		}
	}


	private void addMetaInformation(RevisionDraft draft) throws InternalErrorException {
		String personUri = RevisionManagement.getUserName(user);
		String revisionUri = draft.getRevisionGraph().getGraphName() + "-revision-" + draft.getNewRevisionIdentifier();
		String commitUri = draft.getRevisionGraph().getGraphName() + "-commit-" + draft.getNewRevisionIdentifier();
		String branchUri = draft.getRevisionGraph().getBranchUri(draft.getDerivedFromRevisionIdentifier());
		String revUriOld = draft.getRevisionGraph().getRevisionUri(draft.getDerivedFromRevisionIdentifier());

		// Create a new commit (activity)
		StringBuilder queryContent = new StringBuilder(1000);
		queryContent.append(String.format(
				"<%s> a rmo:RevisionCommit; "
						+ "	prov:wasAssociatedWith <%s>;"
						+ "	prov:generated <%s>;"
						+ "	dc-terms:title \"%s\";"
						+ " prov:used <%s> ;"
						+ "	prov:atTime \"%s\"^^xsd:dateTime. %n", commitUri,
				personUri, revisionUri, message, revUriOld, getDateString()));

		// Create new revision
		queryContent.append(String.format(
				"<%s> a rmo:Revision ; %n"
						+ "	rmo:addSet <%s> ; %n"
						+ "	rmo:deleteSet <%s> ; %n"
						+ "	rmo:revisionNumber \"%s\" ; %n"
						+ "	rmo:belongsTo <%s> ; %n"
						+ " prov:wasDerivedFrom <%s> . %n"
				,  revisionUri, draft.getAddSetURI(), draft.getDeleteSetURI(), draft.getNewRevisionIdentifier(), branchUri, revUriOld));

		// TODO Merge
		// Add second parent revision if available
//		if (draft.revisionNumber2 != null) {
//			String revUri2 = graph.getRevisionUri(draft.revisionNumber2);
//			queryContent.append(String.format(""
//					+ "<%s> prov:used <%s>. %n"
//					+ "<%s> prov:wasDerivedFrom <%s> .", commitUri, revUri2, revisionUri, revUri2));
//		}

		String query = Config.prefixes
				+ String.format("INSERT DATA { GRAPH <%s> { %s } }", draft.getRevisionGraph().getRevisionGraphUri(),
				queryContent.toString());

		TripleStoreInterfaceSingleton.get().executeUpdateQuery(query);

		// Move branch to new revision
		String branchIdentifier = draft.getDerivedFromRevisionIdentifier(); //or revisionNumber //TODO
		String oldRevisionUri = draft.getRevisionGraph().getRevisionUri(branchIdentifier);

		String queryBranch = Config.prefixes + String.format(""
						+ "SELECT ?branch "
						+ "WHERE { GRAPH <%s> {"
						+ "	?branch a rmo:Branch; "
						+ "		rmo:references <%s>."
						+ "	{?branch rdfs:label \"%s\"} UNION {<%s> rmo:revisionNumber \"%s\"}"
						+ "} }",
				draft.getRevisionGraph(), oldRevisionUri, branchIdentifier, oldRevisionUri,
				branchIdentifier);
		QuerySolution sol = TripleStoreInterfaceSingleton.get().executeSelectQuery(queryBranch).next();
		String branchName = sol.getResource("?branch").toString();
		RevisionManagement.moveBranchReference(draft.getRevisionGraph().getRevisionGraphUri(), branchName, oldRevisionUri, revisionUri);
	}


	/**
	 * Add new revision from existing changeset in triplestore.
	 * Applies changeset to full graph and add meta information in revision graph
	 *
	 * @param draft the revision draft
	 * @throws InternalErrorException
	 */
	private void addNewRevisionFromChangeSet(RevisionDraft draft) throws InternalErrorException {
		// remove doubled data
		// (already existing triples in add set; not existing triples in delete set)
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getAddSetURI(), draft.getReferenceFullGraph()));
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
				draft.getDeleteSetURI(), draft.getDeleteSetURI(), draft.getReferenceFullGraph()));

		// merge change sets into reference graph
		// (copy add set to reference graph; remove delete set from reference graph)
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"INSERT { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getReferenceFullGraph(), draft.getAddSetURI()));
		TripleStoreInterfaceSingleton.get().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getReferenceFullGraph(), draft.getDeleteSetURI()));

		// add meta information to R43ples
		addMetaInformation(draft);
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


//	/**
//	 * Add meta information to R43ples revision graph for a new revision.
//	 *
//	 * @param graphName
//	 *            the graph name
//	 * @param user
//	 *            the user name who creates the revision
//	 * @param commitMessage
//	 *            the title of the revision
//	 * @param usedRevisionNumber
//	 *            the number of the revision which is used for creation of the
//	 *            new revision
//	 * @param addSetGraphUri
//	 * 			  name of the graph which holds the add set
//	 * @param removeSetGraphUri
//	 *            name of the graph which holds the delete set
//	 * @throws InternalErrorException
//	 */
//	public static void addMetaInformationForNewRevision(final RevisionDraft draft, final String user, final String timeStamp,
//														final String commitMessage) throws InternalErrorException {
//
//
//	}


}