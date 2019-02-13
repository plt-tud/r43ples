package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * Provides information of an already existent pick merge commit.
 *
 * @author Stephan Hensel
 */
public class PickCommit extends MergeCommit {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(PickCommit.class);

    /** The used source revisions. **/
    private ArrayList<Revision> usedSourceRevisions;
    /** The generated revisions. **/
    private ArrayList<Revision> generatedRevisions;

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public PickCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
     * @param usedSourceRevisions the used source revisions
     * @param usedTargetRevision the used target revision
     * @param generatedRevisions the generated revisions
     * @throws InternalErrorException
     */
    public PickCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message,
                      ArrayList<Revision> usedSourceRevisions, Revision usedTargetRevision, Branch usedTargetBranch, ArrayList<Revision> generatedRevisions) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message, usedTargetRevision, usedTargetBranch);
        this.usedSourceRevisions = usedSourceRevisions;
        this.generatedRevisions = generatedRevisions;
    }

    /**
     * Get the used source revisions.
     *
     * @return the used source revisions
     */
    public ArrayList<Revision> getUsedSourceRevisions() {
        return usedSourceRevisions;
    }

    /**
     * Get the generated revisions.
     *
     * @return the generated revisions
     */
    public ArrayList<Revision> getGeneratedRevisions() {
        return generatedRevisions;
    }
}