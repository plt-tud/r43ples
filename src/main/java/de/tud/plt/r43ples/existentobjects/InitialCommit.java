package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent initial commit.
 *
 * @author Stephan Hensel
 */
public class InitialCommit extends Commit {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(InitialCommit.class);

    /** The generated revision. **/
    private Revision generatedRevision;
    /** The generated branch. */
    private Branch generatedBranch;
    /** The associated change set. **/
    private ChangeSet changeSet;


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
     * @param changeSet the associated change set
     * @throws InternalErrorException
     */
    public InitialCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision generatedRevision, Branch generatedBranch, ChangeSet changeSet) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.generatedRevision = generatedRevision;
        this.generatedBranch = generatedBranch;
        this.changeSet = changeSet;
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

    /**
     * Get the associated change set.
     *
     * @return the associated change set
     */
    public ChangeSet getChangeSet() {
        //TODO Implement method - autogenerate if necessary
        return changeSet;
    }

}
