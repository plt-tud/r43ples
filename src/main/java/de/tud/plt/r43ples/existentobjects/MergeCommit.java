package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an merge commit.
 *
 * @author Stephan Hensel
 */
public class MergeCommit extends Commit {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(MergeCommit.class);

    /** The used target revision. **/
    private Revision usedTargetRevision;
    /** The used target branch. **/
    private Branch usedTargetBranch;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public MergeCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
     * @throws InternalErrorException
     */
    public MergeCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @param user the user
     * @param timeStamp the time stamp
     * @param message the message
     * @param usedTargetRevision the used target revision
     * @param usedTargetBranch the used target branch
     * @throws InternalErrorException
     */
    public MergeCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message,
                       Revision usedTargetRevision, Branch usedTargetBranch) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.usedTargetRevision = usedTargetRevision;
        this.usedTargetBranch = usedTargetBranch;
    }

    /**
     * Get the used target revision.
     *
     * @return the used target revision
     */
    public Revision getUsedTargetRevision() {
        return usedTargetRevision;
    }

    /**
     * Get the used target branch.
     *
     * @return the used target branch
     */
    public Branch getUsedTargetBranch() {
        return usedTargetBranch;
    }



}
