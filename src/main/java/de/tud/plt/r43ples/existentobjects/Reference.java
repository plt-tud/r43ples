package de.tud.plt.r43ples.existentobjects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent reference.
 *
 * @author Stephan Hensel
 */
public class Reference {

    /** The logger. **/
    private Logger logger = Logger.getLogger(Reference.class);

    /** The reference identifier. */
    private String referenceIdentifier;
    /** The reference URI. */
    private String referenceURI;
    /** The full graph URI. **/
    private String fullGraphURI;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param referenceInformation the reference information (identifier or URI of the reference)
     * @param isIdentifier identifies if the identifier or the URI of the reference is specified (identifier => true; URI => false)
     * @throws InternalErrorException
     */
    public Reference(RevisionGraph revisionGraph, String referenceInformation, boolean isIdentifier) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();

        if (isIdentifier) {
            this.referenceIdentifier = referenceInformation;
            this.referenceURI = calculateReferenceURI(this.referenceIdentifier);
        } else {
            this.referenceURI = referenceInformation;
            this.referenceIdentifier = calculateReferenceIdentifier(this.referenceURI);
        }
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
    public Reference(RevisionGraph revisionGraph, String referenceIdentifier, String referenceURI, String fullGraphURI) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.referenceIdentifier = referenceIdentifier;
        this.referenceURI = referenceURI;
        this.fullGraphURI = fullGraphURI;
    }

    /**
     * Get the corresponding commit of the current reference. This commit created this reference.
     *
     * @return the corresponding commit
     * @throws InternalErrorException
     */
    public Commit getCorrespondingCommit() throws InternalErrorException {
        logger.info("Get corresponding commit of branch " + referenceIdentifier);
        String query = Config.prefixes + String.format(""
                + "SELECT ?com "
                + "WHERE { GRAPH  <%s> {"
                + "	?com a rmo:Commit; "
                + "	 rmo:generated <%s>. "
                + "} }", revisionGraphURI, referenceURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return new Commit(revisionGraph, qs.getResource("?com").toString());
        } else {
            throw new InternalErrorException("No corresponding commit found for reference " + referenceIdentifier + ".");
        }
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
     * Get the reference URI.
     *
     * @return the reference URI
     */
    public String getReferenceURI() {
        return referenceURI;
    }

    /**
     * Get the revision graph of the reference.
     *
     * @return revision graph of the reference
     */
    public RevisionGraph getRevisionGraph() {
        return this.revisionGraph;
    }

    /**
     * Calculate the reference URI for a given reference identifier
     *
     * @param referenceIdentifier the reference identifier
     * @return URI of identified reference
     * @throws InternalErrorException
     */
    private String calculateReferenceURI(String referenceIdentifier) throws InternalErrorException {
        logger.debug("Calculate the branch URI for current branch " + referenceIdentifier);
        String query = Config.prefixes + String.format(""
                + "SELECT ?uri "
                + "WHERE { GRAPH  <%s> {"
                + "	?uri a rmo:Reference; "
                + "	 rmo:referenceIdentifier \"%s\". "
                + "} }", revisionGraphURI, referenceIdentifier);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return qs.getResource("?uri").toString();
        } else {
            throw new InternalErrorException("No reference URI found for reference " + referenceIdentifier + ".");
        }
    }

    /**
     * Calculate the reference identifier for a given reference URI
     *
     * @param referenceURI the reference URI
     * @return the reference identifier
     * @throws InternalErrorException
     */
    private String calculateReferenceIdentifier(String referenceURI) throws InternalErrorException {
        logger.info("Calculate the reference identifier for current branch URI " + referenceURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?id "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rmo:Reference; "
                + "	 rmo:referenceIdentifier ?id. "
                + "} }", revisionGraphURI, referenceURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return qs.getLiteral("?id").toString();
        } else {
            throw new InternalErrorException("No reference identifier found for reference URI " + referenceURI + ".");
        }
    }

    /**
     * Get the full graph URI.
     *
     * @return the full graph URI
     * @throws InternalErrorException
     */
    public String getFullGraphURI() {
        if (fullGraphURI == null) {
            fullGraphURI = this.getRevisionGraph().getFullGraphUri(getReferenceURI());
        }
        return fullGraphURI;
    }

}
