package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionGraph;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent initial commit.
 *
 * @author Stephan Hensel
 */
public class InitialCommit extends Commit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(InitialCommit.class);

    /** The generated revision. **/
    private Revision generatedRevision;
    /** The generated branch. */
    private Branch generatedBranch;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public InitialCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
    public InitialCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
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
     * @param generatedRevision the generated revision
     * @param generatedBranch the generated branch
     * @throws InternalErrorException
     */
    public InitialCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision generatedRevision, Branch generatedBranch) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.generatedRevision = generatedRevision;
        this.generatedBranch = generatedBranch;
    }

    /**
     * Get the generated revision.
     *
     * @return the generated revision
     * @throws InternalErrorException
     */
    public Revision getGeneratedRevision() throws InternalErrorException {
        //TODO Implement method - autogenerate the revision if necessary
        return generatedRevision;
    }

    /**
     * Get the generated branch.
     *
     * @return the generated branch
     * @throws InternalErrorException
     */
    public Branch getGeneratedBranch() throws InternalErrorException {
        //TODO Implement method - autogenerate the revision if necessary
        return generatedBranch;
    }
}
