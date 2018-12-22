package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.HighLevelChanges;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Aggregation of atomic changes to high level ones based upon the provided aggregation rules as SPIN.
 * Example query: AGG GRAPH <test> REVISION "1" TO REVISION "2"
 *
 * @author Stephan Hensel
 */
public class Aggregation {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(SelectConstructAskQuery.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
    /** The merge query pattern. **/
    private final Pattern patternAggQuery = Pattern.compile(
            "AGG\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*REVISION\\s*\"(?<startRevisionIdentifier>[^\"]*?)\"\\s*TO\\s*REVISION\\s*\"(?<endRevisionIdentifier>[^\"]*?)\"\\s*",
            patternModifier);

    /** The corresponding R43ples request. **/
    private R43plesRequest request;
    /** The start revision identifier. **/
    private String startRevisionIdentifier;
    /** The end revision identifier. **/
    private String endRevisionIdentifier;
    /** The graph name **/
    private String graphName;
    /** The revision graph. **/
    private RevisionGraph revisionGraph;

    // Dependencies
    /** The triplestore interface to use. **/
    private TripleStoreInterface tripleStoreInterface;
    /** The current URI calculator instance. */
    private URICalculator uriCalculator;


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @return the query response
     * @throws InternalErrorException
     */
    protected Aggregation(R43plesRequest request) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
        this.uriCalculator = new URICalculator();

        this.request = request;

        this.extractRequestInformation();
    }


    /**
     * //TODO
     *
     * @throws InternalErrorException
     */
    protected HighLevelChanges aggregate() throws InternalErrorException {
        return null;
    }


    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        Matcher m = patternAggQuery.matcher(this.request.query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;

            graphName = m.group("graph");
            revisionGraph = new RevisionGraph(graphName);

            startRevisionIdentifier = m.group("startRevisionIdentifier");
            endRevisionIdentifier = m.group("endRevisionIdentifier");

            logger.debug("graph: " + graphName);
            logger.debug("startRevisionIdentifier: " + startRevisionIdentifier);
            logger.debug("endRevisionIdentifier: " + endRevisionIdentifier);
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + this.request.query_sparql);
        }
    }

}
