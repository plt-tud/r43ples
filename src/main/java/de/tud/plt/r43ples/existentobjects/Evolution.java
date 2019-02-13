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
 * Provides information of an already existent evolution.
 *
 * @author Stephan Hensel
 */
public class Evolution {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(Evolution.class);

    /** The evolution URI. */
    private String evolutionURI;

    /** The start revision. */
    private Revision startRevision;
    /** The end revision. */
    private Revision endRevision;
    /** The used source revision graph. */
    private RevisionGraph usedSourceRevisionGraph;

    /** The list of associated semantic changes. */
    private LinkedList<SemanticChange> associatedSemanticChangeList;
    /** The list of performed evolutions. */
    private LinkedList<CoEvolution> performedCoEvolutionList;


    /**
     * The constructor.
     *
     * @param evolutionURI the evolution URI
     * @throws InternalErrorException
     */
    public Evolution(String evolutionURI) throws InternalErrorException {
        this.evolutionURI = evolutionURI;
        associatedSemanticChangeList = new LinkedList<>();
        performedCoEvolutionList = new LinkedList<>();

        retrieveAdditionalInformation();
    }


    /**
     * Calculate additional information of the current evolution and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current evolution URI " + evolutionURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?startRevisionURI ?endRevisionURI ?usedSourceRevisionGraphURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a rmo:Evolution; %n"
                + "	 rmo:startRevision ?startRevisionURI; %n"
                + "  rmo:endRevision ?endRevisionURI; %n"
                + "  rmo:usedSourceRevisionGraph ?usedSourceRevisionGraphURI. %n"
                + "} }", Config.evolution_graph, evolutionURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            usedSourceRevisionGraph = new RevisionGraph(qs.getResource("?usedSourceRevisionGraphURI").toString());
            startRevision = new Revision(usedSourceRevisionGraph, qs.getResource("?startRevisionURI").toString(), false);
            endRevision = new Revision(usedSourceRevisionGraph, qs.getResource("?endRevisionURI").toString(), false);
        } else {
            throw new InternalErrorException("No additional information found for evolution URI " + evolutionURI + ".");
        }

        query = Config.prefixes + String.format(""
                + "SELECT ?associatedSemanticChangeURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a rmo:Evolution; %n"
                + "	 rmo:associatedSemanticChange ?associatedSemanticChangeURI. %n"
                + "} }", Config.evolution_graph, evolutionURI);
        this.logger.debug(query);
        resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                SemanticChange semanticChange = new SemanticChange(usedSourceRevisionGraph, qs.getResource("?associatedSemanticChangeURI").toString());
                associatedSemanticChangeList.add(semanticChange);
            }
        } else {
            throw new InternalErrorException("No additional information (associatedSemanticChange) found for evolution URI " + evolutionURI + ".");
        }

        query = Config.prefixes + String.format(""
                + "SELECT ?performedCoEvolutionURI %n"
                + "WHERE { GRAPH  <%s> { %n"
                + "	<%s> a rmo:Evolution; %n"
                + "	 rmo:performedCoEvolution ?performedCoEvolutionURI. %n"
                + "} }", Config.evolution_graph, evolutionURI);
        this.logger.debug(query);
        resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                CoEvolution coEvolution = new CoEvolution(qs.getResource("?performedCoEvolutionURI").toString());
                performedCoEvolutionList.add(coEvolution);
            }
        } else {
            throw new InternalErrorException("No additional information (performedCoEvolution) found for evolution URI " + evolutionURI + ".");
        }
    }

    /**
     * Get the evolution URI.
     *
     * @return the evolution URI
     */
    public String getEvolutionURI() {
        return evolutionURI;
    }

    /**
     * Get the start revision.
     *
     * @return the start revision
     */
    public Revision getStartRevision() {
        return startRevision;
    }

    /**
     * Get the end revision.
     *
     * @return the end revision
     */
    public Revision getEndRevision() {
        return endRevision;
    }

    /**
     * Get the used source revision graph.
     *
     * @return the used source revision graph
     */
    public RevisionGraph getUsedSourceRevisionGraph() {
        return usedSourceRevisionGraph;
    }

    /**
     * Get the list of associated semantic changes.
     *
     * @return the list of associated semantic changes
     */
    public LinkedList<SemanticChange> getAssociatedSemanticChangeList() {
        return associatedSemanticChangeList;
    }

    /**
     * Get the list of performed evolutions.
     *
     * @return the list of performed evolutions
     */
    public LinkedList<CoEvolution> getPerformedCoEvolutionList() {
        return performedCoEvolutionList;
    }

}
