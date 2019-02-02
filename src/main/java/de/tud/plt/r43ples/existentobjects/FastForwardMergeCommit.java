package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent fast forward merge commit.
 *
 * @author Stephan Hensel
 */
public class FastForwardMergeCommit extends MergeCommit {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(FastForwardMergeCommit.class);

    /** The used source branch. **/
    private Branch usedSourceBranch;
    /** The used source revision. **/
    private Revision usedSourceRevision;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public FastForwardMergeCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
        super(revisionGraph, commitURI);
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @param user the user
     * @param timeStamp the time stamp
     * @param message the message
     * @param usedSourceRevision the used source revision
     * @param usedSourceBranch the used source branch
     * @param usedTargetRevision the used target revision
     * @param usedTargetBranch the used target branch
     * @throws InternalErrorException
     */
    public FastForwardMergeCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message,
                                  Revision usedSourceRevision, Branch usedSourceBranch, Revision usedTargetRevision, Branch usedTargetBranch) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message, usedTargetRevision, usedTargetBranch);
        this.usedSourceBranch = usedSourceBranch;
        this.usedSourceRevision = usedSourceRevision;
    }

    /**
     * Get the used source branch.
     *
     * @return the used source branch
     */
    public Branch getUsedSourceBranch() {
        return usedSourceBranch;
    }

    /**
     * Get the used source revision.
     *
     * @return the used source revision
     */
    public Revision getUsedSourceRevision() {
        return usedSourceRevision;
    }

}