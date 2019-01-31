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
 * Provides information of an already existent applied coevolution rule.
 *
 * @author Stephan Hensel
 */
public class AppliedCoEvolutionRule {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(AppliedCoEvolutionRule.class);

    /** The applied coevolution rule URI. */
    private String appliedCoEvolutionRuleURI;

    /** The used rule. */
    private CoEvoRule usedRule;
    /** The used semantic change. */
    private SemanticChange usedSemanticChange;

    /** The list of SPARQL variable groups (grouped per matching). */
    private LinkedList<SparqlVariableGroup> sparqlVariableGroupList;


    /**
     * The constructor.
     *
     * @param appliedCoEvolutionRuleURI the applied coevolution rule URI
     * @throws InternalErrorException
     */
    public AppliedCoEvolutionRule(String appliedCoEvolutionRuleURI) throws InternalErrorException {
        this.appliedCoEvolutionRuleURI = appliedCoEvolutionRuleURI;
        sparqlVariableGroupList = new LinkedList<>();

        retrieveAdditionalInformation();
    }


    /**
     * Calculate additional information of the current applied coevolution rule and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current applied coevolution rule URI " + appliedCoEvolutionRuleURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?usedRuleURI ?usedSemanticChangeURI ?sourceRevisionGraph %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a aero:AppliedCoEvolutionRule; %n"
                + "	 aero:usedRule ?usedRuleURI; %n"
                + "	 aero:usedSemanticChange ?usedSemanticChangeURI. %n"
                + " ?coEvolutionURI aero:appliedCoEvolutionRule <%s>. %n"
                + " ?evolutionURI rmo:performedCoEvolution ?coEvolutionURI. %n"
                + " ?evolutionURI rmo:usedSourceRevisionGraph ?sourceRevisionGraph. %n"
                + "} }", Config.evolution_graph, appliedCoEvolutionRuleURI, appliedCoEvolutionRuleURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            RevisionGraph usedSourceRevisionGraph = new RevisionGraph(qs.getResource("?sourceRevisionGraph").toString());
            usedSemanticChange = new SemanticChange(usedSourceRevisionGraph, qs.getResource("?usedSemanticChangeURI").toString());
            usedRule = new CoEvoRule(usedSemanticChange);
        } else {
            throw new InternalErrorException("No additional information found for applied coevolution rule URI " + appliedCoEvolutionRuleURI + ".");
        }

        query = Config.prefixes + String.format(""
                + "SELECT ?variableGroupURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a aero:AppliedCoEvolutionRule; %n"
                + "	 aero:hasVariableGroup ?variableGroupURI. %n"
                + "} }", Config.evolution_graph, appliedCoEvolutionRuleURI);
        this.logger.debug(query);
        resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                SparqlVariableGroup sparqlVariableGroup = new SparqlVariableGroup(qs.getResource("?variableGroupURI").toString());
                sparqlVariableGroupList.add(sparqlVariableGroup);
            }
        } else {
            throw new InternalErrorException("No additional information (hasVariableGroup) found for applied coevolution rule URI " + appliedCoEvolutionRuleURI + ".");
        }

    }

    /**
     * Get the applied coevolution rule URI.
     *
     * @return the applied coevolution rule URI
     */
    public String getAppliedCoEvolutionRuleURI() {
        return appliedCoEvolutionRuleURI;
    }

    /**
     * Get the used rule.
     *
     * @return the used rule
     */
    public CoEvoRule getUsedRule() {
        return usedRule;
    }

    /**
     * Get the used semantic change.
     *
     * @return the used semantic change
     */
    public SemanticChange getUsedSemanticChange() {
        return usedSemanticChange;
    }

    /**
     * Get the list of SPARQL variable groups (grouped per matching).
     *
     * @return the list of SPARQL variable groups (grouped per matching)
     */
    public LinkedList<SparqlVariableGroup> getSparqlVariableGroupList() {
        return sparqlVariableGroupList;
    }

}
