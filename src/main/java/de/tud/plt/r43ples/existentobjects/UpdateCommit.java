package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.RevisionGraph;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent commit.
 *
 * @author Stephan Hensel
 */
public class UpdateCommit extends Commit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(UpdateCommit.class);

    /** The used revision. **/
    private Revision usedRevision;
    /** The generated revision. */
    private Revision generatedRevision;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public UpdateCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
    public UpdateCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
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
     * @throws InternalErrorException
     */
    public UpdateCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision usedRevision, Revision generatedRevision) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.usedRevision = usedRevision;
        this.generatedRevision = generatedRevision;
    }

    /**
     * Get the used revision.
     *
     * @return the used revision
     * @throws InternalErrorException
     */
    public Revision getUsedRevision() throws InternalErrorException {
        //TODO Implement method - autogenerate the revision if necessary
        return usedRevision;
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
}
