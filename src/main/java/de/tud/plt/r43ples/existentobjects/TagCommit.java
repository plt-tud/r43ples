package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent tag commit.
 *
 * @author Stephan Hensel
 */
public class TagCommit extends ReferenceCommit {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(TagCommit.class);


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public TagCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
    public TagCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message) throws InternalErrorException {
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
     * @param generatedTag the generated tag
     * @throws InternalErrorException
     */
    public TagCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message, Revision usedRevision, Reference generatedTag) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message, usedRevision, generatedTag);
    }

}
