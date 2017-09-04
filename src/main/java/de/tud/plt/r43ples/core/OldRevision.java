/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.LinkedList;

public class OldRevision {

    private final String fullGraphUri;
    private final Revision revision;
    private final RevisionGraph revisionGraph;

    /**
     * The logger.
     **/
    private Logger logger = Logger.getLogger(OldRevision.class);


    /**
     * Creates the whole revision from the add and delete sets of the
     * predecessors. Saved in a new graph with specified named graph URI.
     * Already existent graphs will be dropped and recreated.
     *
     * @param revisionGraph the revision graph
     * @param revision      the revision
     * @param fullGraphURI  the named graph URI where the full graph will be stored
     * @throws InternalErrorException
     */
    public OldRevision(RevisionGraph revisionGraph, Revision revision, String fullGraphURI) throws InternalErrorException {
        this.fullGraphUri = fullGraphURI;
        this.revisionGraph = revisionGraph;
        this.revision = revision;

        this.logger.info("Rebuild whole content of revision " + revision.getRevisionIdentifier() + " of graph <" + revisionGraph.getGraphName()
                + "> into temporary graph <" + fullGraphURI + ">");

        // Create temporary graph
        TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + fullGraphURI + ">");
        TripleStoreInterfaceSingleton.get().executeUpdateQuery("CREATE GRAPH <" + fullGraphURI + ">");

        // Create path to revision
        LinkedList<ChangeSet> list = PathCalculationSingleton.getInstance().getPathOfChangeSets(revisionGraph, revision).getRevisionPath();

        // Copy branch to temporary graph
        ChangeSet currentChangeSet = list.getFirst();
        Revision currentRevision = currentChangeSet.getSuccessorRevision();
        String copyQuery = "COPY GRAPH <" + currentRevision.getAssociatedBranch().getFullGraphURI() + "> TO GRAPH <" + fullGraphURI + ">";
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(copyQuery);

        while (!list.isEmpty()) {
            currentChangeSet = list.remove();

            String graph_deleted = currentChangeSet.getDeleteSetURI();
            String graph_added = currentChangeSet.getAddSetURI();

            // Add data to temporary graph
            String deleteQuery = String.format("ADD GRAPH <%s> TO GRAPH <%s>", graph_deleted, fullGraphURI);
            TripleStoreInterfaceSingleton.get().executeUpdateQuery(deleteQuery);

            // Remove data from temporary graph (no opposite of SPARQL ADD available)
            String addQuery = String.format("" +
                    "DELETE { GRAPH <%s> {?s ?p ?o.} } " +
                    "WHERE  { GRAPH <%s> {?s ?p ?o.} }", fullGraphURI, graph_added);
            TripleStoreInterfaceSingleton.get().executeUpdateQuery(addQuery);
        }


    }


    public String getFullGraphUri() {
        return fullGraphUri;
    }

    public Revision getRevision() {
        return revision;
    }

    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    public void purge() {
        String deleteQuery = String.format("DROP SILENT GRAPH <%s>", this.fullGraphUri);
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(deleteQuery);
    }
}
