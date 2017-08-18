package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent three way merge commit.
 *
 * @author Stephan Hensel
 */
public class ThreeWayMergeCommit extends Commit {

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