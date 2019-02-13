/*
 * Copyright (c) 2017.  Markus Graube
 */

package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.optimization.ChangeSetPath;
import de.tud.plt.r43ples.optimization.PathCalculationFabric;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

public class FullGraph {

    private final String fullGraphUri;
    private final Revision revision;
    private final RevisionGraph revisionGraph;
    private boolean created = false;

    /**
     * The logger.
     **/
    private Logger logger = LogManager.getLogger(FullGraph.class);


    /**
     * Creates the whole revision from the add and delete sets of the
     * predecessors. Saved in a new graph.
     * Already existent graphs will be dropped and recreated.
     *
     * @param revisionGraph the revision graph
     * @param revision      the revision
     * @throws InternalErrorException
     */
    public FullGraph(RevisionGraph revisionGraph, Revision revision) throws InternalErrorException {

        this.revisionGraph = revisionGraph;
        this.revision = revision;

        if (revision.getAssociatedBranch() != null) {
            this.fullGraphUri = revision.getAssociatedBranch().getFullGraphURI();
            this.logger.info("Full graph of revision " + revision.getRevisionIdentifier() + " of graph <" + revisionGraph.getGraphName()
                    + "> already exists in <" + this.fullGraphUri + ">");
        } else {
            this.fullGraphUri = revision.getRevisionURI() + "-fullGraph";
            this.createNewFullGraph();
        }
    }

    /**
     * Creates the whole revision from the add and delete sets of the
     * predecessors. Saved in a new graph with specified name.
     * Already existent graphs will be dropped and recreated.
     *
     * @param revisionGraph the revision graph
     * @param revision      the revision
     * @param fullGraphUri  URI where full graph should be created
     * @throws InternalErrorException
     */
    public FullGraph(RevisionGraph revisionGraph, Revision revision, String fullGraphUri) throws InternalErrorException {

        this.revisionGraph = revisionGraph;
        this.revision = revision;
        this.fullGraphUri = fullGraphUri;
        this.createNewFullGraph();
    }


    protected void createNewFullGraph() throws InternalErrorException {

        this.logger.info("Rebuild whole content of revision " + revision.getRevisionIdentifier() + " of graph <" + revisionGraph.getGraphName()
                + "> into temporary graph <" + this.fullGraphUri + ">");

        // Create temporary graph
        TripleStoreInterfaceSingleton.get().executeUpdateQuery("DROP SILENT GRAPH <" + fullGraphUri + ">");
        TripleStoreInterfaceSingleton.get().executeUpdateQuery("CREATE GRAPH <" + fullGraphUri + ">");

        // Create path to revision
        ChangeSetPath changeSetPath = PathCalculationFabric.getInstance(revisionGraph).getPathOfChangeSets(revision);

        // Copy branch to temporary graph
        Revision currentRevision = changeSetPath.getTargetRevision();
        String copyQuery = "COPY GRAPH <" + currentRevision.getAssociatedReference().getFullGraphURI() + "> TO GRAPH <" + fullGraphUri + ">";
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
        this.created = true;
    }

    /**
     * Applies delete set stored in named graph graph_deleted
     *
     * @param graph_deleted Uri of named graph containing delete set
     */
    protected void applyDeleteSet(String graph_deleted) {
        String deleteQuery = String.format("ADD SILENT GRAPH <%s> TO GRAPH <%s>", graph_deleted, this.fullGraphUri);
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
        if (this.created) {
            String deleteQuery = String.format("DROP SILENT GRAPH <%s>", this.fullGraphUri);
            TripleStoreInterfaceSingleton.get().executeUpdateQuery(deleteQuery);
        }
    }
}
