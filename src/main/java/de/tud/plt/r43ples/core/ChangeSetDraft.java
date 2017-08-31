package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.ChangeSet;
import de.tud.plt.r43ples.existentobjects.FastForwardMergeCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Collection of information for creating a new change set.
 *
 * @author Stephan Hensel
 *
 */
public class ChangeSetDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ChangeSetDraft.class);

    /** The prior revision. **/
    private Revision priorRevision;
    /** The new revision identifier. **/
    private String newRevisionIdentifier;
    /** The referenced full graph URI. **/
    private String referencedFullGraphURI;

    /** The ADD set URI. */
    private String addSetURI;
    /** The DELETE set URI. */
    private String deleteSetURI;
    /** The add set as N-Triples. **/
    private String addSet;
    /** The delete set as N-Triples. **/
    private String deleteSet;

    /** The new change set URI. **/
    private String changeSetURI;

    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;
    /** The current revision management instance. */
    private RevisionManagement revisionManagement;

    /** States if the add and delete sets were already stripped regarding the prior revision. **/
    private boolean isStripped;

    // Dependencies
    /** The triple store interface to use. **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor. Add and delete sets can be specified which are associated with this revision.
     *
     * @param revisionManagement the current revision management instance
     * @param revisionGraph the revision graph
     * @param priorRevision the prior revision
     * @param newRevisionIdentifier the new revision identifier
     * @param referencedFullGraphURI the referenced full graph URI
     * @param addSet the add set of the revision as N-Triples
     * @param deleteSet the delete set of the revision as N-Triples
     * @param isStripped states if the add and delete sets were already stripped regarding the prior revision
     * @throws InternalErrorException
     */
    protected ChangeSetDraft(RevisionManagement revisionManagement, RevisionGraph revisionGraph, Revision priorRevision, String newRevisionIdentifier, String referencedFullGraphURI, String addSet, String deleteSet, boolean isStripped) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.revisionManagement = revisionManagement;
        this.revisionGraph = revisionGraph;

        this.priorRevision = priorRevision;
        this.newRevisionIdentifier = newRevisionIdentifier;
        this.referencedFullGraphURI = referencedFullGraphURI;

        this.changeSetURI = this.revisionManagement.getNewChangeSetURI(revisionGraph, priorRevision, newRevisionIdentifier);
        this.addSetURI = this.revisionManagement.getNewAddSetURI(revisionGraph, priorRevision, newRevisionIdentifier);
        this.deleteSetURI = this.revisionManagement.getNewDeleteSetURI(revisionGraph, priorRevision, newRevisionIdentifier);

        this.isStripped = isStripped;

        this.addSet = addSet;
        this.deleteSet = deleteSet;
	}

    /**
     * Creates the change set draft with all meta data and named graphs in the triplestore.
     * Creates add and delete sets, strips the resulting sets if necessary and adds meta information.
     *
     * @return the created change set
     */
    protected ChangeSet createInTripleStore() throws InternalErrorException {
        logger.info("Create new change set for graph " + revisionGraph.getGraphName() + ".");

        createAddAndDeleteSetsInTripleStore();
        if (!isStripped) {
            stripSets();
        }
        addMetaInformation();

        return new ChangeSet(revisionGraph, priorRevision, addSetURI, deleteSetURI, changeSetURI);
    }

    /**
     * Creates the add and delete sets in the triplestore.
     */
    private void createAddAndDeleteSetsInTripleStore() throws InternalErrorException {
        if (addSet!=null && !addSet.isEmpty()) {
            // Create new named graph for add set
            logger.debug("Create new graph with name " + addSetURI + ".");
            tripleStoreInterface.executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n", addSetURI));
            RevisionManagementOriginal.executeINSERT(addSetURI, addSet);
        }

        if (deleteSet!=null && !deleteSet.isEmpty()) {
            // Create new named graph for delete set
            logger.debug("Create new graph with name " + deleteSetURI + ".");
            tripleStoreInterface.executeUpdateQuery(String.format("CREATE SILENT GRAPH <%s>%n", deleteSetURI));
            RevisionManagementOriginal.executeINSERT(deleteSetURI, deleteSet);
        }
    }

    /**
     * Strips the add and delete sets. Removes duplicated data regarding the full graph.
     */
    private void stripSets() {
        // remove doubled data
        // (already existing triples in add set; not existing triples in delete set)
        tripleStoreInterface.executeUpdateQuery(String.format(
                "DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } }",
                addSetURI, referencedFullGraphURI));
        tripleStoreInterface.executeUpdateQuery(String.format(
                "DELETE { GRAPH <%s> { ?s ?p ?o. } } WHERE { GRAPH <%s> { ?s ?p ?o. } MINUS { GRAPH <%s> { ?s ?p ?o. } } }",
                deleteSetURI, deleteSetURI, referencedFullGraphURI));
    }

    /**
     * Adds meta information of the created revision to the revision graph.
     *
     * @throws InternalErrorException
     */
    private void addMetaInformation() throws InternalErrorException {
        // The prior revision could be null because of the initial commit
        String queryContent;
        if (priorRevision != null) {
            // Create new revision
            queryContent = String.format(
                    "<%s> a rmo:ChangeSet;"
                            + "	rmo:addSet <%s> ;"
                            + "	rmo:deleteSet <%s> ;"
                            + "	rmo:priorRevision <%s> .",
                    changeSetURI, addSetURI, deleteSetURI, priorRevision.getRevisionURI());
        } else {
            // Create new revision withou prior revision
            queryContent = String.format(
                    "<%s> a rmo:ChangeSet;"
                            + "	rmo:addSet <%s> ;"
                            + "	rmo:deleteSet <%s> .",
                    changeSetURI, addSetURI, deleteSetURI);
        }

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisionGraph.getRevisionGraphUri(), queryContent);

        tripleStoreInterface.executeUpdateQuery(queryRevision);
    }

}
