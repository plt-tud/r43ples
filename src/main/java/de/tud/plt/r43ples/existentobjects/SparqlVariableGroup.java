package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

/**
 * Provides information of an already existent SPARQL variable group rule.
 *
 * @author Stephan Hensel
 */
public class SparqlVariableGroup {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(SparqlVariableGroup.class);

    /** The SPARQL variable group URI. */
    private String sparqlVariableGroupURI;

    /** The list of SPARQL variables. */
    private LinkedList<SparqlVariable> sparqlVariableList;


    /**
     * The constructor.
     *
     * @param sparqlVariableGroupURI SPARQL variable group URI
     * @throws InternalErrorException
     */
    public SparqlVariableGroup(String sparqlVariableGroupURI) throws InternalErrorException {
        this.sparqlVariableGroupURI = sparqlVariableGroupURI;
        sparqlVariableList = new LinkedList<>();

        retrieveAdditionalInformation();
    }


    /**
     * Calculate additional information of the current SPARQL variable group and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current SPARQL variable group URI " + sparqlVariableGroupURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?variableURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a aero:SPARQLVariableGroup; %n"
                + "	 aero:hasVariables ?variableURI. %n"
                + "} }", Config.evolution_graph, sparqlVariableGroupURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();

                SparqlVariable sparqlVariable = new SparqlVariable(qs.getResource("?variableURI").toString());
                sparqlVariableList.add(sparqlVariable);
            }
        } else {
            throw new InternalErrorException("No additional information (hasVariables) found for SPARQL variable group URI " + sparqlVariableGroupURI + ".");
        }

    }

    /**
     * Get the SPARQL variable group URI.
     *
     * @return the SPARQL variable group URI
     */
    public String getSparqlVariableGroupURI() {
        return sparqlVariableGroupURI;
    }

    /**
     * Get the list of SPARQL variables.
     *
     * @return the list of SPARQL variables
     */
    public LinkedList<SparqlVariable> getSparqlVariableList() {
        return sparqlVariableList;
    }

}
