package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent branch.
 *
 * @author Stephan Hensel
 */
public class Branch extends Reference {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(Branch.class);


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param branchInformation the branch information (identifier or URI of the branch)
     * @param isIdentifier identifies if the identifier or the URI of the branch is specified (identifier => true; URI => false)
     * @throws InternalErrorException
     */
    public Branch(RevisionGraph revisionGraph, String branchInformation, boolean isIdentifier) throws InternalErrorException {
        super(revisionGraph, branchInformation, isIdentifier);
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param referenceIdentifier the reference identifier
     * @param referenceURI the reference URI
     * @param fullGraphURI the full graph URI
     * @throws InternalErrorException
     */
    public Branch(RevisionGraph revisionGraph, String referenceIdentifier, String referenceURI, String fullGraphURI) throws InternalErrorException {
        super(revisionGraph, referenceIdentifier, referenceURI, fullGraphURI);
    }

    /**
     * Get the leaf revision of current branch.
     *
     * @return the leaf revision
     * @throws InternalErrorException
     */
    public Revision getLeafRevision() throws InternalErrorException {
        return this.getRevisionGraph().getRevision(this.getReferenceIdentifier());
    }

}
