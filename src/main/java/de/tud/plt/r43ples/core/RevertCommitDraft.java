package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
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
 * Collection of information for creating a new revert commit.
 *
 * @author Stephan Hensel
 */
public class RevertCommitDraft extends CommitDraft {

	/** The logger. **/
	private Logger logger = Logger.getLogger(RevertCommitDraft.class);

	/** The pattern modifier. **/
	private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

	/** The revision draft. **/
	private RevisionDraft revisionDraft;


	/**
	 * The constructor.
	 * Creates an revert commit draft by using the corresponding R43ples request.
	 *
	 * @param request the request received by R43ples
	 */
	protected RevertCommitDraft(R43plesRequest request) throws InternalErrorException {
		super(request);
		this.revisionDraft = extractRequestInformation();
	}

	/**
	 * The constructor.
	 * Creates an revert commit draft by using the corresponding branch.
	 *
	 * @param revisionGraph the revision graph
	 * @param user the user
	 * @param message the message
	 * @param branch the branch where the leaf revision should be reverted
	 * @throws InternalErrorException
	 */
	protected RevertCommitDraft(RevisionGraph revisionGraph, String user, String message, Branch branch) throws InternalErrorException {
		super(null);
		this.revisionDraft = new RevisionDraft(getRevisionManagement(), revisionGraph, branch, branch.getLeafRevision().getChangeSet().getDeleteSetURI(), branch.getLeafRevision().getChangeSet().getAddSetURI());
		this.setUser(user);
		this.setMessage(message);
	}

	/**
	 * Extracts the request information and returns the corresponding revision draft.
	 *
	 * @return the revision draft
	 * @throws InternalErrorException
	 */
	private RevisionDraft extractRequestInformation() throws InternalErrorException {
		final Pattern patternRevertQuery =  Pattern.compile("REVERT\\s*GRAPH\\s<(?<graph>[^>]*)>\\s*BRANCH\\s*\"(?<branch>[^\"]*)\"",
				patternModifier);

		RevisionGraph revisionGraph = null;
		Branch branch = null;

		Matcher m = patternRevertQuery.matcher(getRequest().query_sparql);

		boolean foundEntry = false;

		while (m.find()) {
			foundEntry = true;
			revisionGraph = new RevisionGraph(m.group("graph"));
			branch = revisionGraph.getBranch(m.group("branch"), true);
		}
		if (!foundEntry) {
			throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
		}

		return new RevisionDraft(getRevisionManagement(), revisionGraph, branch, branch.getLeafRevision().getChangeSet().getDeleteSetURI(), branch.getLeafRevision().getChangeSet().getAddSetURI());
	}

	/**
	 * Creates the commit draft as a new commit in the triplestore and creates the corresponding revision.
	 *
	 * @return the created commit
	 */
	protected RevertCommit createInTripleStore() throws InternalErrorException {
		Revision generatedRevision = revisionDraft.createInTripleStore();
		RevertCommit revertCommit = addMetaInformation(generatedRevision);
		updateReferencedFullGraph(generatedRevision.getAssociatedBranch().getFullGraphURI(), generatedRevision.getChangeSet());

		return revertCommit;
	}

	/**
	 * Adds meta information for commit and revision to the revision graph.
	 *
	 * @param generatedRevision the generated revision
	 * @return the created commit
	 * @throws InternalErrorException
	 */
	private RevertCommit addMetaInformation(Revision generatedRevision) throws InternalErrorException {
		String personUri = RevisionManagementOriginal.getUserURI(getUser());

		String revisionUri = generatedRevision.getRevisionURI();
		String commitUri = getRevisionManagement().getNewCommitURI(generatedRevision.getRevisionGraph(), generatedRevision.getRevisionIdentifier());
		String revUriOld = generatedRevision.getDerivedFromRevision().getRevisionURI();

		// Create a new commit (activity)
		StringBuilder queryContent = new StringBuilder(1000);
		queryContent.append(String.format(
				"<%s> a rmo:RevertCommit, rmo:Commit ; "
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

		return new RevertCommit(generatedRevision.getRevisionGraph(), commitUri, getUser(), getTimeStamp(), getMessage(), generatedRevision.getDerivedFromRevision(), generatedRevision);
	}

}