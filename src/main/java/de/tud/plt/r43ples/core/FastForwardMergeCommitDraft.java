package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.FastForwardMergeCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.iohelper.Helper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of information for creating a new fast forward merge commit.
 *
 * @author Stephan Hensel
 */
public class FastForwardMergeCommitDraft extends MergeCommitDraft {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(FastForwardMergeCommitDraft.class);


    /**
     * The constructor.
     * Creates a fast forward merge commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param branchNameFrom the branch name (from)
     * @param branchNameInto the branch name (into)
     * @param user the user
     * @param message the message
     * @param triples the triples of the query WITH part
     * @param type the query type (FORCE, AUTO, MANUAL)
     * @param with states if the WITH part is available
     * @throws InternalErrorException
     */
    protected FastForwardMergeCommitDraft(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String triples, MergeTypes type, boolean with) throws InternalErrorException {
        super(graphName, branchNameFrom, branchNameInto, user, message, MergeActions.MERGE, triples, type, with);
    }

    /**
     * Tries to create a new commit draft as a new commit in the triple store.
     * If possible it will create the corresponding revision and the meta data.
     *
     * @return the commit (has attribute which indicates if the commit was executed or not)
     */
    protected FastForwardMergeCommit createCommitInTripleStore() throws InternalErrorException {
        String revisionUriFrom = getRevisionGraph().getRevisionUri(getBranchNameFrom());
        String revisionUriInto = getRevisionGraph().getRevisionUri(getBranchNameInto());

        Revision usedSourceRevision = new Revision(getRevisionGraph(), revisionUriFrom, false);
        Revision usedTargetRevision = new Revision(getRevisionGraph(), revisionUriInto, false);

        Branch usedSourceBranch = getRevisionGraph().getBranch(getBranchNameFrom(), true);
        Branch usedTargetBranch = getRevisionGraph().getBranch(getBranchNameInto(), true);

        fullGraphCopy(usedSourceBranch.getFullGraphURI(), usedTargetBranch.getFullGraphURI());

        return addMetaInformation(usedSourceRevision, usedSourceBranch, usedTargetRevision, usedTargetBranch);
    }

    /**
     * Adds meta information for commit and revision to the revision graph.
     *
     * <img src="{@docRoot}../../doc/revision management description/r43ples-fastforward.png" />
     *
     * @param usedSourceRevision the used source revision (from)
     * @param usedSourceBranch the used source branch (from)
     * @param usedTargetRevision the used target revision (into)
     * @param usedTargetBranch the used target branch (from)
     * @return the created commit
     * @throws InternalErrorException
     */
    private FastForwardMergeCommit addMetaInformation(Revision usedSourceRevision, Branch usedSourceBranch, Revision usedTargetRevision, Branch usedTargetBranch) throws InternalErrorException {

        String commitURI = getUriCalculator().getNewFastForwardMergeCommitURI(getRevisionGraph(), usedSourceRevision.getRevisionIdentifier(), usedTargetRevision.getRevisionIdentifier());
        String personUri = Helper.getUserURI(getUser());

        // Create a new commit (activity)
        String queryContent = String.format(
                "<%s> a rmo:FastForwardMergeCommit, rmo:MergeCommit, rmo:BasicMergeCommit, rmo:Commit; "
                        + "	rmo:wasAssociatedWith <%s> ;"
                        + "	rmo:commitMessage \"%s\" ;"
                        + "	rmo:timeStamp \"%s\"^^xsd:dateTime ; %n"
                        + " rmo:usedSourceRevision <%s> ;"
                        + " rmo:usedSourceBranch <%s> ;"
                        + " rmo:usedTargetRevision <%s> ;"
                        + " rmo:usedTargetBranch <%s> .",
                commitURI, personUri, getMessage(), getTimeStamp(),
                usedSourceRevision.getRevisionURI(), usedSourceBranch.getReferenceURI(),
                usedTargetRevision.getRevisionURI(), usedTargetBranch.getReferenceURI());
        String query = Config.prefixes
                + String.format("INSERT DATA { GRAPH <%s> { %s } }", getRevisionGraph().getRevisionGraphUri(),
                queryContent);

        getTripleStoreInterface().executeUpdateQuery(query);

        // Update the full graph of the target branch
        fullGraphCopy(getRevisionGraph().getFullGraphUri(usedSourceBranch.getReferenceURI()), getRevisionGraph().getFullGraphUri(usedTargetBranch.getReferenceURI()));

        // Move branch to new revision
        moveBranchReference(getRevisionGraph().getRevisionGraphUri(), usedTargetBranch.getReferenceURI(), usedTargetRevision.getRevisionURI(), usedSourceRevision.getRevisionURI());
        // Update the target branch object
        usedTargetBranch = getRevisionGraph().getBranch(getBranchNameInto(), true);

        return new FastForwardMergeCommit(getRevisionGraph(), commitURI, getUser(), getTimeStamp(), getMessage(),
                usedSourceRevision, usedSourceBranch, usedTargetRevision, usedTargetBranch, null,
                null, false, null, null);
    }

}




