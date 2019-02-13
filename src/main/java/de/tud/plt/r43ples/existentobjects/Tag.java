package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent tag.
 *
 * @author Stephan Hensel
 */
public class Tag extends Reference {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(Tag.class);


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param tagInformation the tag information (identifier or URI of the branch)
     * @param isIdentifier identifies if the identifier or the URI of the tag is specified (identifier => true; URI => false)
     * @throws InternalErrorException
     */
    public Tag(RevisionGraph revisionGraph, String tagInformation, boolean isIdentifier) throws InternalErrorException {
        super(revisionGraph, tagInformation, isIdentifier);
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
    public Tag(RevisionGraph revisionGraph, String referenceIdentifier, String referenceURI, String fullGraphURI) throws InternalErrorException {
        super(revisionGraph, referenceIdentifier, referenceURI, fullGraphURI);
    }

}
