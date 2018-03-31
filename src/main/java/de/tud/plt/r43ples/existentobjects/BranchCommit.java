package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent branch commit.
 *
 * @author Stephan Hensel
 */
public class BranchCommit extends ReferenceCommit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(BranchCommit.class);


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public BranchCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
    public BranchCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
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
     * @param usedRevision the used revision
     * @param generatedBranch the generated branch
     * @throws InternalErrorException
     */
    public BranchCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision usedRevision, Reference generatedBranch) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message, usedRevision, generatedBranch);
    }

}
