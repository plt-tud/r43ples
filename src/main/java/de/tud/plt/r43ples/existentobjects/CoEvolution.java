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
 * Provides information of an already existent coevolution.
 *
 * @author Stephan Hensel
 */
public class CoEvolution {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(CoEvolution.class);

    /** The coevolution URI. */
    private String coEvolutionURI;

    /** The used target branch. */
    private Branch usedTargetBranch;
    /** The used target revision graph. */
    private RevisionGraph usedTargetRevisionGraph;
    /** The generated revision. */
    private Revision generatedRevision;

    /** The list of applied coevolution rules. */
    private LinkedList<AppliedCoEvolutionRule> appliedCoEvolutionRuleList;


    /**
     * The constructor.
     *
     * @param coEvolutionURI the coevolution URI
     * @throws InternalErrorException
     */
    public CoEvolution(String coEvolutionURI) throws InternalErrorException {
        this.coEvolutionURI = coEvolutionURI;
        appliedCoEvolutionRuleList = new LinkedList<>();

        retrieveAdditionalInformation();
    }


    /**
     * Calculate additional information of the current coevolution and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current coevolution URI " + coEvolutionURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?usedTargetBranchURI ?usedTargetRevisionGraphURI ?generatedRevisionURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a rmo:CoEvolution; %n"
                + "	 rmo:usedTargetBranch ?usedTargetBranchURI; %n"
                + "  rmo:usedTargetRevisionGraph ?usedTargetRevisionGraphURI; %n"
                + "  rmo:generated ?generatedRevisionURI. %n"
                + "} }", Config.evolution_graph, coEvolutionURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            usedTargetRevisionGraph = new RevisionGraph(qs.getResource("?usedTargetRevisionGraphURI").toString());
            usedTargetBranch = usedTargetRevisionGraph.getBranch(qs.getResource("?usedTargetBranchURI").toString(), false);
            generatedRevision = new Revision(usedTargetRevisionGraph, qs.getResource("?generatedRevisionURI").toString(), false);

        } else {
            throw new InternalErrorException("No additional information found for coevolution URI " + coEvolutionURI + ".");
        }

        query = Config.prefixes + String.format(""
                + "SELECT ?appliedCoEvolutionRuleURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a rmo:CoEvolution; %n"
                + "	 aero:appliedCoEvolutionRule ?appliedCoEvolutionRuleURI. %n"
                + "} }", Config.evolution_graph, coEvolutionURI);
        this.logger.debug(query);
        resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                AppliedCoEvolutionRule appliedCoEvolutionRule = new AppliedCoEvolutionRule(qs.getResource("?appliedCoEvolutionRuleURI").toString());
                appliedCoEvolutionRuleList.add(appliedCoEvolutionRule);
            }
        } else {
            throw new InternalErrorException("No additional information (appliedCoEvolutionRule) found for coevolution URI " + coEvolutionURI + ".");
        }

    }

    /**
     * Get the coevolution URI.
     *
     * @return the coevolution URI
     */
    public String getCoEvolutionURI() {
        return coEvolutionURI;
    }

    /**
     * Get the used target branch.
     *
     * @return the used target branch
     */
    public Branch getUsedTargetBranch() {
        return usedTargetBranch;
    }

    /**
     * Get the used target revision graph.
     *
     * @return the used target revision graph
     */
    public RevisionGraph getUsedTargetRevisionGraph() {
        return usedTargetRevisionGraph;
    }

    /**
     * Get the generated revision.
     *
     * @return the generated revision
     */
    public Revision getGeneratedRevision() {
        return generatedRevision;
    }

    /**
     * Get the list of applied coevolution rules.
     *
     * @return the list of applied coevolution rules
     */
    public LinkedList<AppliedCoEvolutionRule> getAppliedCoEvolutionRuleList() {
        return appliedCoEvolutionRuleList;
    }

}
