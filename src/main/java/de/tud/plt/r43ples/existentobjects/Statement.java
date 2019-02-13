package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent statement.
 *
 * @author Stephan Hensel
 */
public class Statement {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(Statement.class);

    /** The statement URI. */
    private String statementURI;
    /** The subject. */
    private String subject;
    /** The predicate. */
    private String predicate;
    /** The object. */
    private String object;
    /** The identification whether the object is a resource (true) or a literal (false). */
    private boolean isResource;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param statementURI the statement URI
     * @throws InternalErrorException
     */
    public Statement(RevisionGraph revisionGraph, String statementURI) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.statementURI = statementURI;

        retrieveAdditionalInformation();
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param statementURI the statement URI
     * @param subject the subject
     * @param predicate the predicate
     * @param object the object
     * @param isResource the identification whether the object is a resource (true) or a literal (false)
     */
    public Statement(RevisionGraph revisionGraph, String statementURI, String subject, String predicate, String object, boolean isResource) {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.statementURI = statementURI;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
        this.isResource = isResource;
    }

    /**
     * Calculate additional information of the current statement and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current statement URI " + statementURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?subject ?predicate ?object "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rdf:Statement; "
                + "	 rdf:subject ?subject; "
                + "  rdf:predicate ?predicate; "
                + "  rdf:object ?object. "
                + "} }", revisionGraphURI, statementURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            subject = qs.getResource("?subject").toString();
            predicate = qs.getResource("?predicate").toString();
            if (qs.getResource("?object") != null) {
                object = qs.getResource("?object").toString();
                isResource = true;
            } else {
                if (qs.getLiteral("?object") != null) {
                    object = qs.getLiteral("?object").toString();
                    isResource = false;
                }
            }
        } else {
            throw new InternalErrorException("No additional information found for statement URI " + statementURI + ".");
        }
    }

    /**
     * Get the statement URI.
     *
     * @return the statement URI
     */
    public String getStatementURI() {
        return statementURI;
    }

    /**
     * Get the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Get the predicate.
     *
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * Get the object.
     *
     * @return the object
     */
    public String getObject() {
        return object;
    }

    /**
     * Get the identification whether the object is a resource (true) or a literal (false).
     *
     * @return true if the object is a resource and false if the object is a literal
     */
    public boolean isResource() {
        return isResource;
    }

    /**
     * Get the revision graph.
     *
     * @return the revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

}
