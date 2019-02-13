package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent revert commit.
 *
 * @author Stephan Hensel
 */
public class RevertCommit extends Commit {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(RevertCommit.class);

    /** The used revision. **/
    private Revision usedRevision;
    /** The generated revision. */
    private Revision generatedRevision;
    /** The associated change set. **/
    private ChangeSet changeSet;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public RevertCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
    public RevertCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
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
     * @param generatedRevision the generated revision
     * @param changeSet the associated change set
     * @throws InternalErrorException
     */
    public RevertCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision usedRevision, Revision generatedRevision, ChangeSet changeSet) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.usedRevision = usedRevision;
        this.generatedRevision = generatedRevision;
        this.changeSet = changeSet;
    }

    /**
     * Get the used revision.
     *
     * @return the used revision
     * @throws InternalErrorException
     */
    public Revision getUsedRevision() {
        //TODO Implement method - autogenerate the revision if necessary
        return usedRevision;
    }

    /**
     * Get the generated revision.
     *
     * @return the generated revision
     * @throws InternalErrorException
     */
    public Revision getGeneratedRevision() {
        //TODO Implement method - autogenerate the revision if necessary
        return generatedRevision;
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
