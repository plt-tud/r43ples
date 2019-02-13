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
 * Provides information of an already existent set of statements.
 *
 * @author Stephan Hensel
 */
public class StatementSet {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(StatementSet.class);

    /** The statement set URI. */
    private String statementSetURI;
    /** The list of statements. */
    private LinkedList<Statement> statementList;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param statementSetURI the statement set URI
     * @throws InternalErrorException
     */
    public StatementSet(RevisionGraph revisionGraph, String statementSetURI) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.statementSetURI = statementSetURI;
        this.statementList = new LinkedList<>();

        retrieveAdditionalInformation();
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param statementSetURI the statement set URI
     * @param statementList the list of statements
     */
    public StatementSet(RevisionGraph revisionGraph, String statementSetURI, LinkedList<Statement> statementList) {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.statementSetURI = statementSetURI;
        this.statementList = statementList;
    }

    /**
     * Calculate additional information of the current commit and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current statement set URI " + statementSetURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?statement "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rmo:Set; "
                + "	 rmo:statements ?statement. "
                + "} }", revisionGraphURI, statementSetURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                Statement statement = new Statement(revisionGraph, qs.getResource("?statement").toString());
                statementList.add(statement);
            }
        } else {
            throw new InternalErrorException("No additional information found for statement set URI " + statementSetURI + ".");
        }
    }

    /**
     * Get the statement set URI.
     *
     * @return the statement set URI
     */
    public String getStatementSetURI() {
        return statementSetURI;
    }

    /**
     * Get the list of statements.
     *
     * @return the list of statements
     */
    public LinkedList<Statement> getStatementList() {
        return statementList;
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
