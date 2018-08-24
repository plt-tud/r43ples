package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Collection of information for creating a new branch.
 *
 * @author Stephan Hensel
 *
 */
public class BranchDraft extends ReferenceDraft {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(BranchDraft.class);


    /**
     * The constructor.
     *
     * @param uriCalculator the current URI calculator instance
     * @param revisionGraph the revision graph
     * @param referencedRevision the referenced revision
     * @param referenceIdentifier the reference identifier
     * @throws InternalErrorException
     */
    protected BranchDraft(URICalculator uriCalculator, RevisionGraph revisionGraph, Revision referencedRevision, String referenceIdentifier) throws InternalErrorException {
        super(uriCalculator, revisionGraph, referencedRevision, referenceIdentifier, uriCalculator.getNewFullGraphURI(revisionGraph, referenceIdentifier), uriCalculator.getNewBranchURI(revisionGraph, referenceIdentifier));
	}


    /**
     * Creates the change set draft with all meta data and named graphs in the triplestore.
     * Creates add and delete sets, strips the resulting sets if necessary, updates the referenced full graph and adds meta information.
     *
     * @return the created change set
     */
    protected Branch createInTripleStore() throws InternalErrorException {
        logger.info("Create new branch for graph " + getRevisionGraph().getGraphName() + ".");


        // Create full graph
        getTripleStoreInterface().executeCreateGraph(getReferencedFullGraphURI());
        // Create full graph for this branch
        FullGraph fullGraph = new FullGraph(getRevisionGraph(), this.getReferencedRevision(), this.getReferencedFullGraphURI());

        addMetaInformation();

        return new Branch(getRevisionGraph(), getReferenceIdentifier(), getReferenceURI(), getReferencedFullGraphURI());
    }

    /**
     * Adds meta information of the created revision to the revision graph.
     *
     * @throws InternalErrorException
     */
    private void addMetaInformation() {
        String queryContent = String.format(
                    "<%s> a rmo:Branch, rmo:Reference, rmo:Entity ;"
                            + "	rmo:references <%s> ;"
                            + "	rmo:fullContent <%s> ;"
                            + "	rmo:referenceIdentifier \"%s\" .",
                    getReferenceURI(), getReferencedRevision().getRevisionURI(), getReferencedFullGraphURI(), getReferenceIdentifier());

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", getRevisionGraph().getRevisionGraphUri(), queryContent);

        getTripleStoreInterface().executeUpdateQuery(queryRevision);
    }

}
