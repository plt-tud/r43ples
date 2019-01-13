package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Provides information of an coevolution rule and the corresponding semantic change.
 *
 * @author Stephan Hensel
 */
public class CoEvoRule {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(CoEvoRule.class);

    /** The corresponding semantic change. */
    private SemanticChange semanticChange;

    /** The SPIN dependency matching query URI. */
    private String spinDependencyMatchingQueryURI;
    /** The SPIN add set insert query URI. */
    private String spinAddSetInsertQueryURI;
    /** The SPIN delete set insert query URI. */
    private String spinDeleteSetInsertQueryURI;

    /** The dependency matching query. */
    private String dependencyMatchingQuery;
    /** The add set insert query. */
    private String addSetInsertQuery;
    /** The delete set insert query. */
    private String deleteSetInsertQuery;

    /** The list of SPARQL variables associated with the dependency matching query. */
    private LinkedList<SparqlVariable> sparqlVariablesList;

    // Dependencies
    /** The triplestore interface to use. **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor.
     *
     * @param semanticChange the semantic change which builds the basis for the coevolution
     * @throws InternalErrorException
     */
    public CoEvoRule(SemanticChange semanticChange) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.semanticChange = semanticChange;

        retrieveAdditionalInformation();
    }


    /**
     * Calculate additional information of the current commit and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get associated coevolution rule information of semantic change.");
        // Get the rule of the semantic change and check if a coevolution part is specified
        String ruleURI = semanticChange.getUsedRuleURI();
        String queryCoEvoRule = String.format(
                Config.prefixes
                        + "SELECT ?dependencyQuery ?addQuery ?delQuery %n"
                        + "WHERE { GRAPH <%s> { %n"
                        + "	<%s> a aero:CoEvoRule ; %n"
                        + "   aero:dependencyMatchingQuery ?dependencyQuery; %n"
                        + "   aero:addSetInsertQuery ?addQuery; %n"
                        + "   aero:deleteSetInsertQuery ?delQuery. %n"
                        + "} } %n", Config.rules_graph, ruleURI);

        ResultSet resultSetCoEvoRule = tripleStoreInterface.executeSelectQuery(queryCoEvoRule);
        if (resultSetCoEvoRule.hasNext()) {
            QuerySolution qsSpin = resultSetCoEvoRule.next();
            this.spinDependencyMatchingQueryURI = qsSpin.getResource("?dependencyQuery").toString();
            this.spinAddSetInsertQueryURI = qsSpin.getResource("?addQuery").toString();
            this.spinDeleteSetInsertQueryURI = qsSpin.getResource("?delQuery").toString();
        } else {
            throw new InternalErrorException("No coevolution rules specified for semantic change.");
        }

        // Get the SPARQL queries from SPIN URIs
        String spinDependencyMatchingQueryN3 = Helper.getAllRelatedElementsToURI(Config.rules_graph, this.spinDependencyMatchingQueryURI);
        String spinAddSetQueryN3 = Helper.getAllRelatedElementsToURI(Config.rules_graph, this.spinAddSetInsertQueryURI);
        String spinDeleteSetQueryN3 = Helper.getAllRelatedElementsToURI(Config.rules_graph, this.spinDeleteSetInsertQueryURI);

        this.dependencyMatchingQuery = Helper.getSparqlSelectQueryFromSpin(spinDependencyMatchingQueryN3, this.spinDependencyMatchingQueryURI);
        this.addSetInsertQuery = Helper.getSparqlUpdateQueryFromSpin(spinAddSetQueryN3, this.spinAddSetInsertQueryURI);
        this.deleteSetInsertQuery = Helper.getSparqlUpdateQueryFromSpin(spinDeleteSetQueryN3, this.spinDeleteSetInsertQueryURI);

        // Replace all dependent variables within the queries
        for (SparqlVariable sparqlVariable : semanticChange.getSparqlVariableList()) {
            String name = sparqlVariable.getVariableName();
            String value = sparqlVariable.getValue();
            if (sparqlVariable.isResource()) {
                value = "<" + value + ">";
            } else {
                value = "\"" + value + "\"";
            }
            dependencyMatchingQuery = dependencyMatchingQuery.replaceAll("\\?" + name, value);
            addSetInsertQuery = addSetInsertQuery.replaceAll("\\?" + name, value);
            deleteSetInsertQuery = deleteSetInsertQuery.replaceAll("\\?" + name, value);
        }

        // Get the variables of the dependency matching query
        sparqlVariablesList = new LinkedList<>();
        HashMap<String, String> variableMapFromSpin = Helper.getVariableMapFromSpin(spinDependencyMatchingQueryN3, spinDependencyMatchingQueryURI);
        for (String variableName : variableMapFromSpin.keySet()) {
            String variableURI = variableMapFromSpin.get(variableName);
            SparqlVariable sparqlVariable = new SparqlVariable(null, null, variableName, variableURI, null, false);
            sparqlVariablesList.add(sparqlVariable);
        }

    }

    /**
     * Get the corresponding semantic change.
     *
     * @return the corresponding semantic change
     */
    public SemanticChange getSemanticChange() {
        return semanticChange;
    }

    /**
     * Get the SPIN dependency matching query URI.
     *
     * @return the SPIN dependency matching query URI
     */
    public String getSpinDependencyMatchingQueryURI() {
        return spinDependencyMatchingQueryURI;
    }

    /**
     * Get the SPIN add set insert query URI.
     *
     * @return the SPIN add set insert query URI
     */
    public String getSpinAddSetInsertQueryURI() {
        return spinAddSetInsertQueryURI;
    }

    /**
     * Get the SPIN delete set insert query URI.
     *
     * @return the SPIN delete set insert query URI
     */
    public String getSpinDeleteSetInsertQueryURI() {
        return spinDeleteSetInsertQueryURI;
    }

    /**
     * Get the dependency matching query.
     *
     * @return the dependency matching query
     */
    public String getDependencyMatchingQuery() {
        return dependencyMatchingQuery;
    }

    /**
     * Get the add set insert query.
     *
     * @return the add set insert query
     */
    public String getAddSetInsertQuery() {
        return addSetInsertQuery;
    }

    /**
     * Get the delete set insert query.
     *
     * @return the delete set insert query
     */
    public String getDeleteSetInsertQuery() {
        return deleteSetInsertQuery;
    }

    /**
     * Get the list of SPARQL variables associated with the dependency matching query.
     *
     * @return the list of SPARQL variables associated with the dependency matching query.
     */
    public LinkedList<SparqlVariable> getSparqlVariablesList() {
        return sparqlVariablesList;
    }

}
