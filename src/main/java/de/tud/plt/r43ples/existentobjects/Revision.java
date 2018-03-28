package de.tud.plt.r43ples.existentobjects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Provides information of an already existent revision.
 *
 * @author Stephan Hensel
 */
public class Revision {

    /** The logger. **/
    private Logger logger = Logger.getLogger(Revision.class);

    /** The revision identifier. */
    private String revisionIdentifier;
    /** The revision URI. */
    private String revisionURI;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;
    /** The change sets of the revision. **/
    private ArrayList<ChangeSet> changeSets;
    /** The associated branch. **/
    private Branch associatedBranch;
    /** The associated reference. **/
    private Reference associatedReference;

    // Dependencies
    /** The triplestore interface to use. **/
    protected TripleStoreInterface tripleStoreInterface;



    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param revisionInformation the revision information (identifier or URI of the revision)
     * @param isIdentifier identifies if the identifier or the URI of the revision is specified (identifier => true; URI => false)
     * @throws InternalErrorException
     */
    public Revision(RevisionGraph revisionGraph, String revisionInformation, boolean isIdentifier) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();

        if (isIdentifier) {
            this.revisionIdentifier = revisionInformation;
            this.revisionURI = calculateRevisionURI(this.revisionIdentifier);
        } else {
            this.revisionURI = revisionInformation;
            this.revisionIdentifier = calculateRevisionIdentifier(this.revisionURI);
        }
        this.changeSets = new ArrayList<>();
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param revisionIdentifier the revision identifier
     * @param revisionURI the revision URI
     * @param changeSet the change set
     * @param branch the branch
     */
    public Revision(RevisionGraph revisionGraph, String revisionIdentifier, String revisionURI, ChangeSet changeSet, Branch branch) {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();
        this.changeSets = new ArrayList<>();

        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();

        this.revisionIdentifier = revisionIdentifier;
        this.revisionURI = revisionURI;
        this.changeSets.add(changeSet);
        this.associatedBranch = branch;
    }

    /**
     * Adds one change set to an already existing revision.
     * Only necessary for three way merged revisions which have two change sets.
     *
     * @param changeSet the change set to add
     */
    public void addChangeSet(ChangeSet changeSet) {
        // Dependencies
        this.changeSets.add(changeSet);
    }

    /**
     * Get the was derived from revision of the current revision.
     *
     * @return the derived from revision
     * @throws InternalErrorException
     */
    public Revision getDerivedFromRevision() throws InternalErrorException {
        //TODO merged revisions will have two derived from revisions
        logger.info("Get derived from revision of revision " + revisionIdentifier + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?rev "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> rmo:wasDerivedFrom ?rev. "
                + "	?rev a rmo:Revision. "
                + "} }", revisionGraphURI, revisionURI);
        this.logger.debug(query);
        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return new Revision(revisionGraph, qs.getResource("?rev").toString(), false);
        } else {
            throw new InternalErrorException("No derived from revision found for revision " + revisionIdentifier + ".");
        }
    }

    /**
     * Get the corresponding commit of the current revision. This commit created this revision.
     *
     * @return the corresponding commit
     * @throws InternalErrorException
     */
    public Commit getCorrespondingCommit() throws InternalErrorException {
        logger.info("Get corresponding commit of revision " + revisionIdentifier + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?com "
                + "WHERE { GRAPH  <%s> {"
                + "	?com a rmo:Commit; "
                + "	 rmo:generated <%s>. "
                + "} }", revisionGraphURI, revisionURI);
        this.logger.debug(query);
        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return new Commit(revisionGraph, qs.getResource("?com").toString());
        } else {
            throw new InternalErrorException("No corresponding commit found for revision " + revisionIdentifier + ".");
        }
    }

    /**
     * Get the associated reference of the current revision.
     *
     * @return the associated reference or null if revision has no directly associated reference
     * @throws InternalErrorException
     */
    public Reference getAssociatedReference() throws InternalErrorException {
        //FIXME A revision can be referenced by multiple reference - return a list of branches
        if (associatedReference == null) {
            logger.info("Get associated reference of revision " + revisionIdentifier + ".");
            String query = Config.prefixes + String.format(""
                    + "SELECT ?reference "
                    + "WHERE { GRAPH  <%s> {"
                    + "	?reference rmo:references <%s> . "
                    + "	?reference a rmo:Reference . "
                    + "} }", revisionGraphURI, revisionURI);
            this.logger.debug(query);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                associatedReference = new Reference(revisionGraph, qs.getResource("?reference").toString(), false);
            } else {
                return null;
            }
        }
        return associatedReference;
    }

    /**
     * Get the associated branch of the current revision.
     *
     * @return the associated branch or null if revision has no directly associated branch
     * @throws InternalErrorException
     */
    public Branch getAssociatedBranch() throws InternalErrorException {
        //FIXME A revision can be referenced by multiple branches - return a list of branches
        if (associatedBranch == null) {
            logger.info("Get associated branch of revision " + revisionIdentifier + ".");
            String query = Config.prefixes + String.format(""
                    + "SELECT ?branch "
                    + "WHERE { GRAPH  <%s> {"
                    + "	?branch rmo:references <%s> . "
                    + "	?branch a rmo:Branch . "
                    + "} }", revisionGraphURI, revisionURI);
            this.logger.debug(query);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                associatedBranch = new Branch(revisionGraph, qs.getResource("?branch").toString(), false);
            } else {
                return null;
            }
        }
        return associatedBranch;
    }

    /**
     * Get the revision identifier.
     *
     * @return the revision identifier
     */
    public String getRevisionIdentifier() {
        return revisionIdentifier;
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
     * Get the change set.
     *
     * @return the change set
     */
    public ChangeSet getChangeSet() {
        return getChangeSets().get(0);
    }

    /**
     * Get the change sets.
     *
     * @return the change sets
     */
    public ArrayList<ChangeSet> getChangeSets() {
        if ((changeSets == null) || (changeSets.isEmpty())) {
            logger.debug("Get additional information of current revision " + revisionIdentifier + ".");
            String query = Config.prefixes + String.format(""
                    + "SELECT ?changeSetURI "
                    + "WHERE { GRAPH  <%s> {"
                    + "	<%s> a rmo:Revision; "
                    + "	 rmo:hasChangeSet ?changeSetURI. "
                    + "} }", revisionGraphURI, revisionURI);
            this.logger.debug(query);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                changeSets.add(new ChangeSet(revisionGraph, qs.getResource("?changeSetURI").toString()));
            }
        }

        return changeSets;
    }

    /**
     * Get the corresponding revision graph.
     *
     * @return the corresponding revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    /**
     * Calculate the revision URI for a given revision identifier
     *
     * @param revisionIdentifier the revision identifier
     * @return URI of identified revision
     * @throws InternalErrorException
     */
    private String calculateRevisionURI(String revisionIdentifier) throws InternalErrorException {
        logger.info("Calculate the revision URI for current revision " + revisionIdentifier + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?uri "
                + "WHERE { GRAPH  <%s> {"
                + "	?uri a rmo:Revision; "
                + "	 rmo:revisionIdentifier \"%s\". "
                + "} }", revisionGraphURI, revisionIdentifier);
        this.logger.debug(query);
        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return qs.getResource("?uri").toString();
        } else {
            throw new InternalErrorException("No revision URI found for revision " + revisionIdentifier + ".");
        }
    }

    /**
     * Calculate the revision identifier for a given revision URI
     *
     * @param revisionURI the revision URI
     * @return the revision identifier
     * @throws InternalErrorException
     */
    private String calculateRevisionIdentifier(String revisionURI) throws InternalErrorException {
        logger.info("Calculate the revision identifier for current revision URI " + revisionURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?id "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rmo:Revision; "
                + "	 rmo:revisionIdentifier ?id. "
                + "} }", revisionGraphURI, revisionURI);
        this.logger.debug(query);
        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return qs.getLiteral("?id").toString();
        } else {
            throw new InternalErrorException("No revision identifier found for revision URI " + revisionURI + ".");
        }
    }

    /**
     * Get the content of a named graph as N-TRIPLES.
     *
     * @param namedGraphURI the named graph URI
     * @return the content of the named graph as N-TRIPLES
     */
    private String getContentOfNamedGraphAsN3(String namedGraphURI) {
        String query = Config.prefixes + String.format(
                "CONSTRUCT {?s ?p ?o} %n"
                        + "WHERE { GRAPH <%s> {?s ?p ?o} }", namedGraphURI);
        String resultAsTurtle = tripleStoreInterface.executeConstructQuery(query, "TURTLE");
        return JenaModelManagement.convertJenaModelToNTriple(JenaModelManagement.readStringToJenaModel(resultAsTurtle, "TURTLE"));
    }

}