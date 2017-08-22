package de.tud.plt.r43ples.draftobjects;

import com.hp.hpl.jena.query.QuerySolution;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.UpdateCommit;
import de.tud.plt.r43ples.management.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new update commit.
 *
 * @author Stephan Hensel
 * @author Markus Graube
 */
public class UpdateCommitDraft extends CommitDraft {

	/** The logger. **/
	private Logger logger = Logger.getLogger(UpdateCommitDraft.class);

	/** The pattern modifier. **/
	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

	/** The revision draft. **/
	private RevisionDraft revisionDraft;
	/** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
	private boolean isCreatedWithRequest;


	/**
	 * The constructor.
	 * Creates an update commit draft by using the corresponding R43ples request.
	 *
	 * @param request the request received by R43ples
	 */
	protected UpdateCommitDraft(R43plesRequest request){
		super(request);
		this.isCreatedWithRequest = true;
	}

	/**
	 * The constructor.
	 * Creates an update commit draft by using the corresponding add and delete sets.
	 *
	 * @param graphName the graph name
	 * @param addSet the add set as N-Triples
	 * @param deleteSet the delete set as N-Triples
	 * @param user the user
	 * @param message the message
	 * @param derivedFromIdentifier the revision identifier of the revision or the reference identifier from which the new revision should be derive from
	 * @throws InternalErrorException
	 */
	protected UpdateCommitDraft(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifier) throws InternalErrorException {
		super(null);
		this.revisionDraft = new RevisionDraft(getRevisionManagement(), new RevisionGraph(graphName), derivedFromIdentifier, addSet, deleteSet);
		this.setUser(user);
		this.setMessage(message);
		this.isCreatedWithRequest = false;
	}

	/**
	 * Creates the commit draft as a new commit in the triplestore and creates the corresponding revisions.
	 *
	 * @return the list of created commits
	 */
	protected ArrayList<UpdateCommit> createCommitInTripleStore() throws InternalErrorException {
		if (!isCreatedWithRequest) {
			revisionDraft.createRevisionInTripleStore();
			ArrayList<UpdateCommit> commitList = new ArrayList<>();
			commitList.add(addMetaInformation(revisionDraft));
			return commitList;
		} else {
			return this.updateChangeSetsByRewrittenQuery();
		}
	}

	/**
	 * Updates the change sets by a rewritten SPARQL query of the request.
	 *
	 * @return the list of created commits
	 * @throws InternalErrorException
	 */
	private ArrayList<UpdateCommit> updateChangeSetsByRewrittenQuery() throws InternalErrorException {

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
		Matcher m = patternUpdateRevision.matcher(getRequest().query_sparql);
		if (m.find()) {
			queryRewritten = getRequest().query_sparql.substring(0, m.start());
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
		m = patternUpdateRevision.matcher(getRequest().query_sparql);
		while (m.find()) {
			String action = m.group("action");
			String updateClause = getStringEnclosedinBraces(getRequest().query_sparql, m.end());

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
					d = new RevisionDraft(getRevisionManagement(), graph, revisionName);
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
		Matcher m1 = patternWhere.matcher(getRequest().query_sparql);
		if (m1.find()) {
			queryRewritten += "WHERE {";
			String whereClause = getStringEnclosedinBraces(getRequest().query_sparql, m1.end());

			Matcher m1a = patternGraphWithRevision.matcher(whereClause);
			while (m1a.find()) {
				String graphName = m1a.group("graph");
				String revisionName = m1a.group("revision").toLowerCase();
				// TODO: replace generateFullGraphOfRevision with query
				// rewriting option
				String tempGraphName = graphName + "-temp";
				RevisionManagementOriginal.generateFullGraphOfRevision(graphName, revisionName, tempGraphName);
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
		getTripleStoreInterface().executeUpdateQuery(queryRewritten);

		// V. add changesets to full graph and add meta information in revision
		// graphs
		ArrayList<UpdateCommit> commitList = new ArrayList<>();
		for (RevisionDraft draft : revList) {
			addNewRevisionFromChangeSet(draft);
			// add meta information to R43ples
			commitList.add(addMetaInformation(draft));
		}
		return commitList;
	}

	/**
	 * Add new revision from existing changeset in triplestore.
	 * Applies changeset to full graph.
	 *
	 * @param draft the revision draft
	 * @throws InternalErrorException
	 */
	private void addNewRevisionFromChangeSet(RevisionDraft draft) throws InternalErrorException {
		// remove doubled data
		// (already existing triples in add set; not existing triples in delete set)
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getAddSetURI(), draft.getReferenceFullGraph()));
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
				draft.getDeleteSetURI(), draft.getDeleteSetURI(), draft.getReferenceFullGraph()));

		// merge change sets into reference graph
		// (copy add set to reference graph; remove delete set from reference graph)
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"INSERT { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getReferenceFullGraph(), draft.getAddSetURI()));
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getReferenceFullGraph(), draft.getDeleteSetURI()));
	}

	/**
	 * Adds meta information for commit and revision to the revision graph.
	 *
	 * @param draft the revision draft
	 * @return the created commit
	 * @throws InternalErrorException
	 */
	private UpdateCommit addMetaInformation(RevisionDraft draft) throws InternalErrorException {
		String personUri = RevisionManagementOriginal.getUserName(getUser());

		String revisionUri = draft.getRevisionURI();

		String commitUri = draft.getRevisionGraph().getGraphName() + "-commit-" + draft.getNewRevisionIdentifier();
		String branchUri = draft.getRevisionGraph().getBranchUri(draft.getDerivedFromIdentifier());//getDerivedFromRevisionIdentifier());
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
				personUri, revisionUri, getMessage(), revUriOld, getTimeStamp()));

		// Create new revision
		queryContent.append(String.format(
				"<%s> a rmo:Revision ; %n"
						+ "	rmo:addSet <%s> ; %n"
						+ "	rmo:deleteSet <%s> ; %n"
						+ "	rmo:revisionNumber \"%s\" ; %n"
						+ "	rmo:belongsTo <%s> ; %n"
						+ " prov:wasDerivedFrom <%s> . %n"
				,  revisionUri, draft.getAddSetURI(), draft.getDeleteSetURI(), draft.getNewRevisionIdentifier(), branchUri, revUriOld));

		String query = Config.prefixes
				+ String.format("INSERT DATA { GRAPH <%s> { %s } }", draft.getRevisionGraph().getRevisionGraphUri(),
				queryContent.toString());

		getTripleStoreInterface().executeUpdateQuery(query);

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
				draft.getRevisionGraph().getRevisionGraphUri(), oldRevisionUri, branchIdentifier, oldRevisionUri,
				branchIdentifier);
		QuerySolution sol = getTripleStoreInterface().executeSelectQuery(queryBranch).next();
		String branchName = sol.getResource("?branch").toString();
		RevisionManagementOriginal.moveBranchReference(draft.getRevisionGraph().getRevisionGraphUri(), branchName, oldRevisionUri, revisionUri);


		Revision newRevision = new Revision(draft.getRevisionGraph(), draft.getNewRevisionIdentifier(), revisionUri, draft.getAddSetURI(), draft.getDeleteSetURI());
		newRevision.getDerivedFromRevision();

		return new UpdateCommit(draft.getRevisionGraph(), commitUri, getUser(), getTimeStamp(), getMessage(), newRevision.getDerivedFromRevision(), newRevision);
	}

	/**
	 * Get a string enclosed in braces.
	 *
	 * @param string the original string
	 * @param start_pos the start position
	 * @return the enclosed in braces string
	 */
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