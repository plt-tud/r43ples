/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SelectConstructAskQuery {

    private final String query;
    /**
     * The logger.
     **/
    private Logger logger = Logger.getLogger(SelectConstructAskQuery.class);
    private String format;

    /**
     * Initializes query of a standard R43plesL query (SELECT, CONSTRUCT, ASK). Classic way.
     *
     * @param request the request received by R43ples
     * @return the query response
     * @throws InternalErrorException
     */
    protected SelectConstructAskQuery(R43plesRequest request) throws InternalErrorException {
        query = request.query_r43ples;
        format = request.format;
    }

    /**
     * Performs R43ples query
     *
     * @return result in format specified
     * @throws InternalErrorException
     */
    protected String performQuery() throws InternalErrorException {
        final Pattern patternSelectFromPart = Pattern.compile(
                "(?<type>FROM|GRAPH)\\s*<(?<graph>[^>\\?]*)(\\?|>)(\\s*REVISION\\s*\"|revision=)(?<revision>([^\">]+))(>|\")",
                Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);
// TODO change REVISION to REVISION or BRANCH
        String queryM = query;

        FullGraph fullGraph = null;

        Matcher m = patternSelectFromPart.matcher(queryM);
        while (m.find()) {
            String graphName = m.group("graph");
            String type = m.group("type");
            String revisionNumber = m.group("revision").toLowerCase();
            String fullGraphUri;

            RevisionGraph graph = new RevisionGraph(graphName);

            // if no revision number is declared use the MASTER as default
            if (revisionNumber == null) {
                revisionNumber = "master";
            }
            if (graph.hasBranch(revisionNumber)) {
                fullGraphUri = graph.getReferenceGraph(revisionNumber);
            } else {
                // Respond with specified revision, therefore the content of the revision
                // must be generated - saved in graph <graphName-revisionNumber>

                fullGraph = new FullGraph(graph, graph.getRevision(revisionNumber));
                fullGraphUri = fullGraph.getFullGraphUri();
            }

            queryM = m.replaceFirst(type + " <" + fullGraphUri + ">");
            m = patternSelectFromPart.matcher(queryM);
        }

        String response = TripleStoreInterfaceSingleton.get()
                .executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + queryM, format);
        if (fullGraph != null)
            fullGraph.purge();
        return response;
    }

}
