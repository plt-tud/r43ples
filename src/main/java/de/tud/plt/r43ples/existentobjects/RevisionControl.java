package de.tud.plt.r43ples.existentobjects;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * Provides information of all revised graphs.
 *
 * @author Stephan Hensel
 */
public class RevisionControl {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(RevisionControl.class);


    /**
     * Get a hash map of all revised graphs in R43ples.
     * Key is the corresponding graph name.
     *
     * @return the hash map of revised graphs
     */
    public HashMap<String, RevisionGraph> getRevisedGraphs() {
        // As new graphs can be added there is no private variable storing the currently revised graphs
        HashMap<String, RevisionGraph> revisionGraphMap = new HashMap<>();
        String sparqlQuery = Config.prefixes
                + String.format(""
                + "SELECT DISTINCT ?graph "
                + "WHERE {"
                + " GRAPH <%s> {  ?graph a rmo:RevisionGraph }"
                + "} ORDER BY ?graph", Config.revision_graph);
        ResultSet results = TripleStoreInterfaceSingleton.get().executeSelectQuery(sparqlQuery);
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            String graphName = qs.getResource("graph").toString();
            RevisionGraph revisionGraph = new RevisionGraph(graphName);
            revisionGraphMap.put(graphName, revisionGraph);
        }

        return revisionGraphMap;
    }

}
