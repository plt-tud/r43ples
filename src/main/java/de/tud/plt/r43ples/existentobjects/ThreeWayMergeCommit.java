package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent three way merge commit.
 *
 * @author Stephan Hensel
 */
public class ThreeWayMergeCommit extends MergeCommit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ThreeWayMergeCommit.class);

//    /** The used revision. **/
//    private Revision usedRevision;
    /** The generated revision. */
    private Revision generatedRevision;

    //TODO


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param commitURI the commit URI
     * @throws InternalErrorException
     */
    public ThreeWayMergeCommit(RevisionGraph revisionGraph, String commitURI) throws InternalErrorException {
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
     * @param generatedRevision the generated revision
     * @param commonRevision the common revision
     * @param hasConflict identifies if there is a conflict
     * @param conflictModel the conflict model as TURTLE
     * @param differenceModelURI the URI of the difference model graph
     * @throws InternalErrorException
     */
    public ThreeWayMergeCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message,
                       Revision usedSourceRevision, Branch usedSourceBranch, Revision usedTargetRevision, Branch usedTargetBranch, Revision generatedRevision,
                       Revision commonRevision, boolean hasConflict, String conflictModel, String differenceModelURI) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message, usedSourceRevision, usedSourceBranch, usedTargetRevision, usedTargetBranch, generatedRevision,
                commonRevision, hasConflict, conflictModel, differenceModelURI);
    }

//    //TODO add getter
//
//    /**
//     * Get the generated revision.
//     *
//     * @return the generated revision
//     * @throws InternalErrorException
//     */
//    public Revision getGeneratedRevision() throws InternalErrorException {
//        //TODO Implement method - autogenerate the revision if necessary
//        return generatedRevision;
//    }
}