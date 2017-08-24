package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.log4j.Logger;

/**
 * Provides information of an merge commit.
 *
 * @author Stephan Hensel
 */
public class MergeCommit extends Commit {

    /** The logger. **/
    private Logger logger = Logger.getLogger(MergeCommit.class);

    /** The used source revision. **/
    private Revision usedSourceRevision;
    /** The used source branch. **/
    private Branch usedSourceBranch;
    /** The used target revision. **/
    private Revision usedTargetRevision;
    /** The used target branch. **/
    private Branch usedTargetBranch;
    /** The generated revision. */
    private Revision generatedRevision;
    /** The common revision. **/
    private Revision commonRevision;
    /** Identifies if there is a conflict. **/
    private boolean hasConflict;
    /** The conflict model as TURTLE. **/
    private String conflictModel;
    /** The URI of the difference model graph. **/
    private String differenceModelURI;


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
    public MergeCommit(RevisionGraph revisionGraph, String commitURI, String user, String timeStamp, String message,
                       Revision usedSourceRevision, Branch usedSourceBranch, Revision usedTargetRevision, Branch usedTargetBranch, Revision generatedRevision,
                       Revision commonRevision, boolean hasConflict, String conflictModel, String differenceModelURI) throws InternalErrorException {
        super(revisionGraph, commitURI, user, timeStamp, message);
        this.usedSourceRevision = usedSourceRevision;
        this.usedSourceBranch = usedSourceBranch;
        this.usedTargetRevision = usedTargetRevision;
        this.usedTargetBranch = usedTargetBranch;
        this.generatedRevision = generatedRevision;
        this.commonRevision = commonRevision;
        this.hasConflict = hasConflict;
        this.conflictModel = conflictModel;
        this.differenceModelURI = differenceModelURI;
    }

    /**
     * Get the used source revision.
     *
     * @return the used source revision
     */
    public Revision getUsedSourceRevision() {
        return usedSourceRevision;
    }

    /**
     * Get the used source branch.
     *
     * @return the used source branch
     */
    public Branch getUsedSourceBranch() {
        return usedSourceBranch;
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

    /**
     * Get the generated revision.
     *
     * @return the generated revision
     */
    public Revision getGeneratedRevision() {
        return generatedRevision;
    }

    /**
     * Get the common revision.
     *
     * @return the common revision
     */
    public Revision getCommonRevision() {
        return commonRevision;
    }

    /**
     * Identifies if there is a conflict.
     *
     * @return the boolean which identifies if there is a conflict
     */
    public boolean isHasConflict() {
        return hasConflict;
    }

    /**
     * Get the conflict model as TURTLE.
     *
     * @return the conflict model as TURTLE
     */
    public String getConflictModel() {
        return conflictModel;
    }

    /**
     * Get the URI of the difference model graph.
     *
     * @return the URI of the difference model graph
     */
    public String getDifferenceModelURI() {
        return differenceModelURI;
    }

}
