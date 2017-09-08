package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.UpdateCommit;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
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
	protected UpdateCommitDraft(R43plesRequest request) throws OutdatedException {
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
	 * @param branch the branch were the new revision should be created
	 * @throws InternalErrorException
	 */
	protected UpdateCommitDraft(String graphName, String addSet, String deleteSet, String user, String message, Branch branch) throws InternalErrorException {
		super(null);
		this.revisionDraft = new RevisionDraft(getRevisionManagement(), new RevisionGraph(graphName), branch, addSet, deleteSet, false);
		this.setUser(user);
		this.setMessage(message);
		this.isCreatedWithRequest = false;
	}

	/**
	 * Creates the commit draft as a new commit in the triplestore and creates the corresponding revisions.
	 *
	 * @return the list of created commits
	 */
	protected ArrayList<UpdateCommit> createInTripleStore() throws InternalErrorException {
		if (!isCreatedWithRequest) {
			Revision generatedRevision = revisionDraft.createInTripleStore();
			ArrayList<UpdateCommit> commitList = new ArrayList<>();
			UpdateCommit updateCommit = addMetaInformation(generatedRevision);
			commitList.add(updateCommit);
			updateReferencedFullGraph(generatedRevision.getAssociatedBranch().getFullGraphURI(), generatedRevision.getChangeSet());
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
			String updateClause = getStringEnclosedInBraces(getRequest().query_sparql, m.end());

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
					d = null;//TODO new RevisionDraft(getRevisionManagement(), graph, revisionName);
					revList.add(d);
				}
				String graphClause = getStringEnclosedInBraces(updateClause, m2a.end());

				if (action.equalsIgnoreCase("INSERT")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.getChangeSet().getAddSetURI(), graphClause);
				} else if (action.equalsIgnoreCase("DELETE")) {
					queryRewritten += String.format("GRAPH <%s> { %s }", d.getChangeSet().getDeleteSetURI(), graphClause);
				}
			}
		}
		queryRewritten += "}";

		// III. Rewrite where clause
		Matcher m1 = patternWhere.matcher(getRequest().query_sparql);
		if (m1.find()) {
			queryRewritten += "WHERE {";
			String whereClause = getStringEnclosedInBraces(getRequest().query_sparql, m1.end());

			Matcher m1a = patternGraphWithRevision.matcher(whereClause);
			while (m1a.find()) {
				String graphName = m1a.group("graph");
				String revisionName = m1a.group("revision").toLowerCase();
				// TODO: replace generateFullGraphOfRevision with query
				// rewriting option
				String tempGraphName = graphName + "-temp";
				RevisionGraph revisionGraph = new RevisionGraph(graphName);
                // Create full graph for this branch
				FullGraph fullGraph = new FullGraph(revisionGraph, revisionGraph.getRevision(revisionName));
				String GraphClause = getStringEnclosedInBraces(whereClause, m1a.end());
				queryRewritten += String.format("GRAPH <%s> { %s }", fullGraph.getFullGraphUri(), GraphClause);
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
			//TODO commitList.add(addMetaInformation(draft));
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
				draft.getChangeSet().getAddSetURI(), draft.getReferenceFullGraph()));
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
				draft.getChangeSet().getDeleteSetURI(), draft.getChangeSet().getDeleteSetURI(), draft.getReferenceFullGraph()));

		// merge change sets into reference graph
		// (copy add set to reference graph; remove delete set from reference graph)
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"INSERT { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getReferenceFullGraph(), draft.getChangeSet().getAddSetURI()));
		getTripleStoreInterface().executeUpdateQuery(String.format(
				"DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
				draft.getReferenceFullGraph(), draft.getChangeSet().getDeleteSetURI()));
	}

	/**
	 * Adds meta information for commit and revision to the revision graph.
	 *
	 * @param generatedRevision the generated revision
	 * @return the created commit
	 * @throws InternalErrorException
	 */
	private UpdateCommit addMetaInformation(Revision generatedRevision) throws InternalErrorException {
		String personUri = RevisionManagementOriginal.getUserURI(getUser());

		String revisionUri = generatedRevision.getRevisionURI();
		String commitUri = getRevisionManagement().getNewCommitURI(generatedRevision.getRevisionGraph(), generatedRevision.getRevisionIdentifier());
		String revUriOld = generatedRevision.getDerivedFromRevision().getRevisionURI();

		// Create a new commit (activity)
		StringBuilder queryContent = new StringBuilder(1000);
		queryContent.append(String.format(
				"<%s> a rmo:RevisionCommit, rmo:Commit ; "
						+ "	rmo:wasAssociatedWith <%s>;"
						+ "	rmo:generated <%s>;"
						+ "	rmo:commitMessage \"%s\";"
						+ " rmo:used <%s> ;"
						+ "	rmo:atTime \"%s\"^^xsd:dateTime. %n", commitUri,
				personUri, revisionUri, getMessage(), revUriOld, getTimeStamp()));

		String query = Config.prefixes
				+ String.format("INSERT DATA { GRAPH <%s> { %s } }", generatedRevision.getRevisionGraph().getRevisionGraphUri(),
				queryContent.toString());

		getTripleStoreInterface().executeUpdateQuery(query);

		// Move branch to new revision
		moveBranchReference(generatedRevision.getRevisionGraph().getRevisionGraphUri(), generatedRevision.getAssociatedBranch().getReferenceURI(), generatedRevision.getDerivedFromRevision().getRevisionURI(), revisionUri);

		return new UpdateCommit(generatedRevision.getRevisionGraph(), commitUri, getUser(), getTimeStamp(), getMessage(), generatedRevision.getDerivedFromRevision(), generatedRevision);
	}

	/**
	 * Get a string enclosed in braces.
	 *
	 * @param string the original string
	 * @param start_pos the start position
	 * @return the enclosed in braces string
	 */
	private String getStringEnclosedInBraces(final String string, int start_pos){
		int end_pos = start_pos;
		int count_parenthesis = 1;
		while (count_parenthesis>0) {
			end_pos++;
			char ch = string.charAt(end_pos);
			if (ch=='{') count_parenthesis++;
			if (ch=='}') count_parenthesis--;
		}
		return string.substring(start_pos, end_pos);
	}

}