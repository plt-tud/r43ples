package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent reference commit.
 *
 * @author Stephan Hensel
 */
public class ReferenceCommit extends Commit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ReferenceCommit.class);

    /** The used revision. **/
    private Revision usedRevision;
    /** The generated reference. */
    private Reference generatedReference;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public ReferenceCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
    public ReferenceCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
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
     * @param generatedReference the generated branch
     * @throws InternalErrorException
     */
    public ReferenceCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision usedRevision, Reference generatedReference) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.usedRevision = usedRevision;
        this.generatedReference = generatedReference;
    }

    /**
     * Get the generated revision.
     *
     * @return the generated revision
     * @throws InternalErrorException
     */
    public Revision getUsedRevision() throws InternalErrorException {
        //TODO Implement method - autogenerate the revision if necessary
        return usedRevision;
    }

    /**
     * Get the generated reference.
     *
     * @return the generated reference
     * @throws InternalErrorException
     */
    public Reference getGeneratedReference() throws InternalErrorException {
        //TODO Implement method - autogenerate the revision if necessary
        return generatedReference;
    }
}
