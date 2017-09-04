/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.optimization.ChangeSetPath;
import de.tud.plt.r43ples.optimization.PathCalculationSingleton;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.LinkedList;

public class FullGraph {

    private final String fullGraphUri;
    private final Revision revision;
    private final RevisionGraph revisionGraph;

    /**
     * The logger.
     **/
    private Logger logger = Logger.getLogger(FullGraph.class);


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
    public FullGraph(RevisionGraph revisionGraph, Revision revision, String fullGraphURI) throws InternalErrorException {
        this.fullGraphUri = fullGraphURI;
        this.revisionGraph = revisionGraph;
        this.revision = revision;

        this.logger.info("Rebuild whole content of revision " + revision.getRevisionIdentifier() + " of graph <" + revisionGraph.getGraphName()
                + "> into temporary graph <" + fullGraphURI + ">");

        // Create temporary graph
        TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + fullGraphURI + ">");
        TripleStoreInterfaceSingleton.get().executeUpdateQuery("CREATE GRAPH <" + fullGraphURI + ">");

        // Create path to revision
        ChangeSetPath changeSetPath = PathCalculationSingleton.getInstance().getPathOfChangeSets(revisionGraph, revision);

        // Copy branch to temporary graph
        Revision currentRevision = changeSetPath.getTargetRevision();
        String copyQuery = "COPY GRAPH <" + currentRevision.getAssociatedBranch().getFullGraphURI() + "> TO GRAPH <" + fullGraphURI + ">";
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(copyQuery);

        // Apply changesets
        LinkedList<ChangeSet> list = changeSetPath.getRevisionPath();
        while (!list.isEmpty()) {
            ChangeSet currentChangeSet = list.remove();

            // Add data to temporary graph
            String graph_deleted = currentChangeSet.getDeleteSetURI();
            this.applyDeleteSet(graph_deleted);

            // Remove data from temporary graph (no opposite of SPARQL ADD available)
            String graph_added = currentChangeSet.getAddSetURI();
            this.applyAddSet(graph_added);
        }

    }

    /**
     * Applies delete set stored in named graph graph_deleted
     *
     * @param graph_deleted Uri of named graph containing delete set
     */
    protected void applyDeleteSet(String graph_deleted) {
        String deleteQuery = String.format("ADD GRAPH <%s> TO GRAPH <%s>", graph_deleted, this.fullGraphUri);
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(deleteQuery);
    }


    /**
     * Applies add set stored in named graph graph_added
     *
     * @param graph_added Uri of named graph containing add set
     */
    protected void applyAddSet(String graph_added) {
        String addQuery = String.format("" +
                "DELETE { GRAPH <%s> {?s ?p ?o.} } " +
                "WHERE  { GRAPH <%s> {?s ?p ?o.} }", this.fullGraphUri, graph_added);
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(addQuery);
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
