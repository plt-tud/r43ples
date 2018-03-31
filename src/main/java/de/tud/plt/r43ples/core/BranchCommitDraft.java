package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.BranchCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import org.apache.log4j.Logger;

import java.util.regex.Pattern;

/**
 * Collection of information for creating a new branch commit.
 *
 * @author Stephan Hensel
 */
public class BranchCommitDraft extends ReferenceCommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(BranchCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;


    /**
     * The constructor.
     * Creates a branch commit draft by using the corresponding meta information.
     *
     * @param revisionGraph the revision graph
     * @param referenceIdentifier the reference identifier
     * @param baseRevision the revision identifier (the corresponding revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @throws InternalErrorException
     */
    protected BranchCommitDraft(RevisionGraph revisionGraph, String referenceIdentifier, Revision baseRevision, String user, String message) throws InternalErrorException {
        super(revisionGraph, referenceIdentifier, baseRevision, user, message, true);
    }

    /**
     * Creates the commit draft as a new commit in the triple store.
     *
     * @return the created branch commit
     */
    protected BranchCommit createInTripleStore() throws InternalErrorException {
        String commitURI = getRevisionManagement().getNewBranchCommitURI(getRevisionGraph(), getReferenceIdentifier());

        BranchDraft branchDraft = new BranchDraft(getRevisionManagement(), getRevisionGraph(), getBaseRevision(), getReferenceIdentifier());
        Branch generatedBranch = branchDraft.createInTripleStore();


        addMetaInformation(generatedBranch.getReferenceURI(), commitURI);

        return new BranchCommit(getRevisionGraph(), commitURI, getUser(), getTimeStamp(), getMessage(), getBaseRevision(), generatedBranch);
    }

    /**
     * Adds the necessary meta data.
     *
     * @param referenceURI the reference URI
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    private void addMetaInformation(String referenceURI, String commitURI) throws InternalErrorException {
        logger.info("Create new branch '" + getReferenceIdentifier() + "' for graph " + getRevisionGraph().getGraphName());

        // General variables
        String personUri = RevisionManagementOriginal.getUserURI(getUser());

        // Create a new commit (activity)
        String queryContent = String.format(""
                        + "<%s> a rmo:BranchCommit, rmo:ReferenceCommit, rmo:Commit ; "
                        + "	rmo:wasAssociatedWith <%s> ;"
                        + "	rmo:generated <%s> ;"
                        + " rmo:used <%s> ;"
                        + "	rmo:commitMessage \"%s\" ;"
                        + "	rmo:atTime \"%s\" .%n",
                commitURI, personUri, referenceURI, getBaseRevision().getRevisionURI(), getMessage(), getTimeStamp());

        // Execute queries
        String query = Config.prefixes
                + String.format("INSERT DATA { GRAPH <%s> { %s } } ;", getRevisionGraph().getRevisionGraphUri(), queryContent);
        getTripleStoreInterface().executeUpdateQuery(query);
    }

}
