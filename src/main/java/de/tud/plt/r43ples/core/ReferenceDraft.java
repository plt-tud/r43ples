package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Collection of information for creating a new reference.
 *
 * @author Stephan Hensel
 *
 */
public class ReferenceDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ReferenceDraft.class);

    /** The referenced revision. **/
    private Revision referencedRevision;
    /** The reference identifier. **/
    private String referenceIdentifier;
    /** The referenced full graph URI. **/
    private String referencedFullGraphURI;

    /** The new reference URI. **/
    private String referenceURI;

    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;
    /** The current revision management instance. */
    private RevisionManagement revisionManagement;

    // Dependencies
    /** The triple store interface to use. **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor.
     *
     * @param revisionManagement the current revision management instance
     * @param revisionGraph the revision graph
     * @param referencedRevision the referenced revision
     * @param referenceIdentifier the reference identifier
     * @param referencedFullGraphURI the referenced full graph URI
     * @param referenceURI the reference URI
     * @throws InternalErrorException
     */
    protected ReferenceDraft(RevisionManagement revisionManagement, RevisionGraph revisionGraph, Revision referencedRevision, String referenceIdentifier, String referencedFullGraphURI, String referenceURI) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.revisionManagement = revisionManagement;
        this.revisionGraph = revisionGraph;

        this.referencedRevision = referencedRevision;
        this.referenceIdentifier = referenceIdentifier;
        this.referencedFullGraphURI = referencedFullGraphURI;

        this.referenceURI = referenceURI;
	}

    /**
     * Get the referenced revision.
     *
     * @return the referenced revision
     */
    public Revision getReferencedRevision() {
        return referencedRevision;
    }

    /**
     * Get the reference identifier.
     *
     * @return the reference identifier
     */
    public String getReferenceIdentifier() {
        return referenceIdentifier;
    }

    /**
     * Get the referenced full graph URI.
     *
     * @return the referenced full graph URI
     */
    public String getReferencedFullGraphURI() {
        return referencedFullGraphURI;
    }

    /**
     * Get the reference URI.
     *
     * @return the reference URI
     */
    public String getReferenceURI() {
        return referenceURI;
    }

    /**
     * Get the revision graph.
     *
     * @return the revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    /**
     * Get the revision management.
     *
     * @return the revision management
     */
    public RevisionManagement getRevisionManagement() {
        return revisionManagement;
    }

    /**
     * Get the triple store interface.
     *
     * @return the triple store interface
     */
    public TripleStoreInterface getTripleStoreInterface() {
        return tripleStoreInterface;
    }

}
