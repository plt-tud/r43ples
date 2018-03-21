package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.ReferenceCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.R43plesRequest;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new reference commit.
 *
 * @author Stephan Hensel
 */
public class ReferenceCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ReferenceCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

    /** The revision graph. **/
    private RevisionGraph revisionGraph;
    /** The reference identifier. **/
    private String referenceIdentifier;
    /** The base revision. **/
    private Revision baseRevision;
    /** States if the created reference is a branch or a tag. (branch => true; tag => false) **/
    private boolean isBranch;

    /** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
    private boolean isCreatedWithRequest;


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @throws InternalErrorException
     */
    public ReferenceCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
        this.extractRequestInformation();
        this.isCreatedWithRequest = true;
    }

    /**
     * The constructor.
     * Creates a reference commit draft by using the corresponding meta information.
     *
     * @param revisionGraph the revision graph
     * @param referenceIdentifier the reference identifier
     * @param baseRevision the revision identifier (the corresponding revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @param isBranch states if the created reference is a branch or a tag. (branch => true; tag => false)
     * @throws InternalErrorException
     */
    protected ReferenceCommitDraft(RevisionGraph revisionGraph, String referenceIdentifier, Revision baseRevision, String user, String message, boolean isBranch) throws InternalErrorException {
        super(null);
        this.setUser(user);
        this.setMessage(message);
        this.revisionGraph = revisionGraph;
        this.referenceIdentifier = referenceIdentifier.toLowerCase();
        this.baseRevision = baseRevision;
        this.isBranch = isBranch;
        this.isCreatedWithRequest = false;
    }

    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        final Pattern patternBranchOrTagQuery = Pattern.compile("(?<action>TAG|BRANCH)\\s*GRAPH\\s*<(?<graph>[^>]*)>\\s*REVISION\\s*\"(?<revision>[^\"]*)\"\\s*TO\\s*\"(?<name>[^\"]*)\"",
                patternModifier);
        Matcher m = patternBranchOrTagQuery.matcher(getRequest().query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;
            String action = m.group("action");
            this.revisionGraph = new RevisionGraph(m.group("graph"));
            this.baseRevision = new Revision(this.revisionGraph, m.group("revision").toLowerCase(), true);
            this.referenceIdentifier = m.group("name").toLowerCase();
            if (action.equals("TAG")) {
                this.isBranch = false;
            } else if (action.equals("BRANCH")) {
                this.isBranch = true;
            } else {
                throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
            }
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }
    }

    /**
     * Creates the commit draft as a new commit in the triple store.
     *
     * @return the created reference commit
     */
    protected ReferenceCommit createInTripleStore() throws InternalErrorException {
        if (isBranch) {
            BranchCommitDraft branchCommitDraft = new BranchCommitDraft(revisionGraph, referenceIdentifier, baseRevision, getUser(), getMessage());
            return branchCommitDraft.createInTripleStore();
        } else {
            TagCommitDraft tagCommitDraft = new TagCommitDraft(revisionGraph, referenceIdentifier, baseRevision, getUser(), getMessage());
            return tagCommitDraft.createInTripleStore();
        }
    }

    /**
     * Get the revision graph.
     *
     * @return the revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    /**
     * Get the referenceIdentifier.
     *
     * @return the referenceIdentifier
     */
    public String getReferenceIdentifier() {
        return referenceIdentifier;
    }

    /**
     * Get the base revision.
     *
     * @return the base revision
     */
    public Revision getBaseRevision() {
        return baseRevision;
    }

}
