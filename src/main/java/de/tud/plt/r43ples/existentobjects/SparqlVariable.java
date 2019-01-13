package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of a SPARQL variable.
 *
 * @author Stephan Hensel
 */
public class SparqlVariable {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(SparqlVariable.class);

    /** The SPARQL variable URI. */
    private String sparqlVariableURI;
    /** The variable name. */
    private String variableName;
    /** The SPIN resource URI. */
    private String spinResourceURI;
    /** The value. */
    private String value;
    /** The identification whether the value is a resource (true) or a literal (false). */
    private boolean isResource;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param sparqlVariableURI the SPARQL variable URI
     * @throws InternalErrorException
     */
    public SparqlVariable(RevisionGraph revisionGraph, String sparqlVariableURI) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.sparqlVariableURI = sparqlVariableURI;

        retrieveAdditionalInformation();
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param sparqlVariableURI the SPARQL variable URI
     * @param variableName the variable name
     * @param spinResourceURI the SPIN resource URI
     * @param value the value
     * @param isResource the identification whether the value is a resource (true) or a literal (false)
     */
    public SparqlVariable(RevisionGraph revisionGraph, String sparqlVariableURI, String variableName, String spinResourceURI, String value, boolean isResource) {
        this.revisionGraph = revisionGraph;
        // Null check is necessary because this constructor is also used with an empty revision graph as a temporary object
        if (this.revisionGraph != null) {
            this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        }
        this.sparqlVariableURI = sparqlVariableURI;
        this.variableName = variableName;
        this.spinResourceURI = spinResourceURI;
        this.value = value;
        this.isResource = isResource;
    }

    /**
     * Calculate additional information of the current SPARQL variable and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current SPARQL variable URI " + sparqlVariableURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?variableName ?spinResourceURI ?value "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a aero:SPARQLVariable; "
                + "	 sp:varName ?variableName; "
                + "  aero:spinResource ?spinResourceURI; "
                + "  aero:value ?value. "
                + "} }", revisionGraphURI, sparqlVariableURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            variableName = qs.getLiteral("?variableName").toString();
            spinResourceURI = qs.getResource("?spinResourceURI").toString();

            if (qs.getResource("?value") != null) {
                value = qs.getResource("?value").toString();
                isResource = true;
            } else {
                if (qs.getLiteral("?value") != null) {
                    value = qs.getLiteral("?value").toString();
                    isResource = false;
                }
            }
        } else {
            throw new InternalErrorException("No additional information found for SPARQL variable URI " + sparqlVariableURI + ".");
        }
    }

    /**
     * Get the SPARQL variable URI.
     *
     * @return the SPARQL variable URI
     */
    public String getSparqlVariableURI() {
        return sparqlVariableURI;
    }

    /**
     * Get the variable name.
     *
     * @return the variable name
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * Get the SPIN resource URI.
     *
     * @return the SPIN resource URI
     */
    public String getSpinResourceURI() {
        return spinResourceURI;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Get the identification whether the value is a resource (true) or a literal (false).
     *
     * @return true if the value is a resource and false if the value is a literal
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
