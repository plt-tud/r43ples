package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Collection of information for creating a new revision
 *
 * @author Markus Graube
 * @author Stephan Hensel
 *
 */
public class RevisionDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(RevisionDraft.class);

    /** The branch were the new revision should be created **/
    private Branch branch;
    /** The revision from which the new one should be derive from. **/
    private Revision derivedFromRevision;

    /** The revision identifier of the revision which should be created. **/
    private String newRevisionIdentifier;
    /** The revision URI. **/
    private String revisionURI;

    /** The referenced full graph. **/
    private String referenceFullGraph;
    /** The add set as N-Triples. **/
    private String addSet;
    /** The delete set as N-Triples. **/
    private String deleteSet;
    /** The add set URI. **/
    private String addSetURI;
    /** The delete set URI. **/
    private String deleteSetURI;

    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;
    /** The current URI calculator instance. */
    private URICalculator uriCalculator;

    /** The created change set. **/
    private ChangeSet changeSet;
    /** States if the add and delete sets were already stripped regarding the prior revision. **/
    private boolean isStripped;
    /** States if the the content of the add and delete sets will be specified by a following rewritten query (in that case add and delete set can be null but the corresponding graphs will be created anyway). **/
    private boolean isSpecifiedByRewrittenQuery;

    // Dependencies
    /** The triple store interface to use. **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor. Add and delete sets can be specified which are associated with this revision.
     *
     * @param uriCalculator the current URI calculator instance
     * @param revisionGraph the revision graph
     * @param branch the branch were the new revision should be created
     * @param addSet the add set of the revision as N-Triples
     * @param deleteSet the delete set of the revision as N-Triples
     * @param isStripped states if the add and delete sets were already stripped regarding the prior revision
     * @throws InternalErrorException
     */
    protected RevisionDraft(URICalculator uriCalculator, RevisionGraph revisionGraph, Branch branch, String addSet, String deleteSet, boolean isStripped) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.uriCalculator = uriCalculator;
        this.revisionGraph = revisionGraph;

		this.branch = branch;
        this.derivedFromRevision = revisionGraph.getRevision(branch);

     	this.newRevisionIdentifier = revisionGraph.getNextRevisionIdentifier();
     	this.revisionURI = this.uriCalculator.getNewRevisionURI(revisionGraph, newRevisionIdentifier);

		this.referenceFullGraph = branch.getFullGraphURI();

		this.addSet = addSet;
		this.deleteSet = deleteSet;

        this.addSetURI = null;
        this.deleteSetURI = null;

		this.isStripped = isStripped;
		this.isSpecifiedByRewrittenQuery = false;
	}


    /**
     * The constructor. Usage only for initial commit (there is no derived from revision/branch, initial revision identifier will be calculated).
     * Add and delete sets can be specified which are associated with this revision.
     *
     * @param uriCalculator the current URI calculator instance
     * @param revisionGraph the revision graph
     * @param addSet the add set of the revision as N-Triples
     * @param deleteSet the delete set of the revision as N-Triples
     * @throws InternalErrorException
     */
    protected RevisionDraft(URICalculator uriCalculator, RevisionGraph revisionGraph, String addSet, String deleteSet) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.uriCalculator = uriCalculator;
        this.revisionGraph = revisionGraph;

        this.branch = null;
        this.derivedFromRevision = null;

        this.newRevisionIdentifier = revisionGraph.getNextRevisionIdentifier();
        this.revisionURI = this.uriCalculator.getNewRevisionURI(revisionGraph, newRevisionIdentifier);

        this.referenceFullGraph = null;

        this.addSet = addSet;
        this.deleteSet = deleteSet;

        this.addSetURI = null;
        this.deleteSetURI = null;

        this.isStripped = true;
        this.isSpecifiedByRewrittenQuery = false;
    }


    /**
     * The constructor. Add and delete sets must be generated by commit.
     *
     * @param uriCalculator the current URI calculator instance
     * @param revisionGraph the revision graph
     * @param branch the branch were the new revision should be created
     * @throws InternalErrorException
     */
    protected RevisionDraft(URICalculator uriCalculator, RevisionGraph revisionGraph, Branch branch) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.uriCalculator = uriCalculator;
        this.revisionGraph = revisionGraph;

        this.branch = branch;
        this.derivedFromRevision = revisionGraph.getRevision(branch);

        this.newRevisionIdentifier = revisionGraph.getNextRevisionIdentifier();
        this.revisionURI = this.uriCalculator.getNewRevisionURI(revisionGraph, newRevisionIdentifier);

        this.referenceFullGraph = branch.getFullGraphURI();

        this.addSet = null;
        this.deleteSet = null;

        this.addSetURI = null;
        this.deleteSetURI = null;

        this.isStripped = true;
        this.isSpecifiedByRewrittenQuery = true;
    }


    /**
     * The constructor. Add and delete set URIs can be specified which are associated with this revision.
     *
     * @param uriCalculator the current URI calculator instance
     * @param revisionGraph the revision graph
     * @param branch the branch were the new revision should be created
     * @param addSetURI the add set URI of the revision
     * @param deleteSetURI the delete set URI of the revision
     * @throws InternalErrorException
     */
    protected RevisionDraft(URICalculator uriCalculator, RevisionGraph revisionGraph, Branch branch, String addSetURI, String deleteSetURI) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.uriCalculator = uriCalculator;
        this.revisionGraph = revisionGraph;

        this.branch = branch;
        this.derivedFromRevision = revisionGraph.getRevision(branch);

        this.newRevisionIdentifier = revisionGraph.getNextRevisionIdentifier();
        this.revisionURI = this.uriCalculator.getNewRevisionURI(revisionGraph, newRevisionIdentifier);

        this.referenceFullGraph = branch.getFullGraphURI();

        this.addSet = null;
        this.deleteSet = null;

        this.addSetURI = addSetURI;
        this.deleteSetURI = deleteSetURI;

        this.isStripped = true;
        this.isSpecifiedByRewrittenQuery = false;
    }


    /**
     * Creates the revision draft with all meta data and named graphs in the triplestore.
     * Creates change set and adds meta information.
     *
     * @return the created change set
     */
    protected Revision createInTripleStore() throws InternalErrorException {
        logger.debug("Create new revision for graph " + revisionGraph.getGraphName() + ".");

        ChangeSetDraft changeSetDraft;

        if ((addSetURI == null) && (deleteSetURI == null)) {
            changeSetDraft = new ChangeSetDraft(uriCalculator, revisionGraph, derivedFromRevision, newRevisionIdentifier, revisionURI, referenceFullGraph, addSet, deleteSet, isStripped, isSpecifiedByRewrittenQuery);
        } else {
            changeSetDraft = new ChangeSetDraft(uriCalculator, revisionGraph, derivedFromRevision, newRevisionIdentifier, revisionURI, referenceFullGraph, addSetURI, deleteSetURI);
        }
        this.changeSet = changeSetDraft.createInTripleStore();

        addMetaInformation();

        return new Revision(revisionGraph, newRevisionIdentifier, revisionURI, changeSet, branch);
    }

    /**
     * Adds meta information of the created revision to the revision graph.
     *
     * @throws InternalErrorException
     */
    private void addMetaInformation() {
        // The derived from revision could be null because of the initial commit
        String queryContent;
        if (derivedFromRevision != null) {
            queryContent = String.format(
                    "<%s> a rmo:Revision, rmo:Entity ;"
                            + "	rmo:revisionIdentifier \"%s\" ;"
                            + "	rmo:wasDerivedFrom <%s> .",
                    revisionURI, newRevisionIdentifier, derivedFromRevision.getRevisionURI());
        } else {
            queryContent = String.format(
                    "<%s> a rmo:Revision, rmo:Entity ;"
                            + "	rmo:revisionIdentifier \"%s\" .",
                    revisionURI, newRevisionIdentifier);
        }
        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisionGraph.getRevisionGraphUri(), queryContent);

        tripleStoreInterface.executeUpdateQuery(queryRevision);
    }

    /**
     * Checks if this revision draft is equal to another one.
     *
     * @param graphName the graph name
     * @param branchIdentifier the branch identifier
     * @return true if the revision is equal
     * @throws InternalErrorException
     */
    public boolean equals(final String graphName, final String branchIdentifier) {
		RevisionGraph otherGraph = new RevisionGraph(graphName);
		//String otherRevisionNumber = otherGraph.getRevisionIdentifier(revisionIdentifier);
		return ((this.revisionGraph.getGraphName().equals(graphName)) && (this.getBranch().getReferenceIdentifier().equals(branchIdentifier)));
        //(this.derivedFromRevision.getRevisionIdentifier().equals(otherRevisionNumber)));
	} //TODO

    /**
     * Get the revision graph.
     *
     * @return the revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    /**
     * Get the revision URI.
     *
     * @return the revision URI
     */
    public String getRevisionURI() {
        return revisionURI;
    }

    /**
     * Get the new revision identifier.
     *
     * @return the new revision identifier
     */
    public String getNewRevisionIdentifier() {
        return newRevisionIdentifier;
    }

    /**
     * Get the derived from revision.
     *
     * @return the derived from revision
     */
    public Revision getDerivedFromRevision() {
        return derivedFromRevision;
    }

    /**
     * Get the corresponding branch.
     *
     * @return the branch
     */
    public Branch getBranch() {
        return branch;
    }

    /**
     * Get the referenced full graph.
     *
     * @return the referenced full graph
     */
    public String getReferenceFullGraph() {
        return referenceFullGraph;
    }

    /**
     * Get the change set.
     *
     * @return the change set
     */
    public ChangeSet getChangeSet() {
        return changeSet;
    }

}
