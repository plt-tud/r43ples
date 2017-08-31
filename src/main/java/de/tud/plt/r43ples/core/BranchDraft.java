package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Collection of information for creating a new branch.
 *
 * @author Stephan Hensel
 *
 */
public class BranchDraft extends ReferenceDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(BranchDraft.class);


    /**
     * The constructor.
     *
     * @param revisionManagement the current revision management instance
     * @param revisionGraph the revision graph
     * @param referencedRevision the referenced revision
     * @param referenceIdentifier the reference identifier
     * @throws InternalErrorException
     */
    protected BranchDraft(RevisionManagement revisionManagement, RevisionGraph revisionGraph, Revision referencedRevision, String referenceIdentifier) throws InternalErrorException {
        super(revisionManagement, revisionGraph, referencedRevision, referenceIdentifier, revisionManagement.getNewFullGraphURI(revisionGraph, referenceIdentifier), revisionManagement.getNewBranchURI(revisionGraph, referenceIdentifier));
	}


    /**
     * Creates the change set draft with all meta data and named graphs in the triplestore.
     * Creates add and delete sets, strips the resulting sets if necessary, updates the referenced full graph and adds meta information.
     *
     * @return the created change set
     */
    protected Branch createInTripleStore() throws InternalErrorException {
        logger.info("Create new branch for graph " + getRevisionGraph().getGraphName() + ".");

        addMetaInformation();

        // Create full graph
        getTripleStoreInterface().executeCreateGraph(getReferencedFullGraphURI());

        return new Branch(getRevisionGraph(), getReferenceIdentifier(), getReferenceURI(), getReferencedFullGraphURI());
    }

    /**
     * Adds meta information of the created revision to the revision graph.
     *
     * @throws InternalErrorException
     */
    private void addMetaInformation() throws InternalErrorException {
        String queryContent = String.format(
                    "<%s> a rmo:Branch, rmo:Reference, rmo:Entity ;"
                            + "	rmo:references <%s> ;"
                            + "	rmo:fullGraph <%s> ;"
                            + "	rmo:referenceIdentifier <%s> .",
                    getReferenceURI(), getReferencedRevision().getRevisionURI(), getReferencedFullGraphURI(), getReferenceIdentifier());

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", getRevisionGraph().getRevisionGraphUri(), queryContent);

        getTripleStoreInterface().executeUpdateQuery(queryRevision);
    }

}
