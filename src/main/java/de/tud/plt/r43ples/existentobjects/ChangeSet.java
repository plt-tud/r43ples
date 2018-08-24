package de.tud.plt.r43ples.existentobjects;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Provides information of an already existent change set.
 *
 * @author Stephan Hensel
 */
public class ChangeSet {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(ChangeSet.class);

    /** The prior revision. **/
    private Revision priorRevision;
    /** The succeeding revision. **/
    private Revision succeedingRevision;

    /** The ADD set URI. */
    private String addSetURI;
    /** The DELETE set URI. */
    private String deleteSetURI;
    /** The ADD set content as N-TRIPLES. **/
    private String addSetContent;
    /** The DELETE set content as N-TRIPLES. **/
    private String deleteSetContent;

    /** The change set URI. **/
    private String changeSetURI;

    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;

    // Dependencies
    /** The triple store interface to use. **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor
     *
     * @param revisionGraph the revision graph
     * @param priorRevision the prior revision
     * @param addSetURI the add set URI
     * @param deleteSetURI the delete set URI
     * @param changeSetURI the change set URI
     * @throws InternalErrorException
     */
    public ChangeSet(RevisionGraph revisionGraph, Revision priorRevision, String addSetURI, String deleteSetURI, String changeSetURI) {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.priorRevision = priorRevision;
        this.addSetURI = addSetURI;
        this.deleteSetURI = deleteSetURI;
        this.changeSetURI = changeSetURI;

        this.revisionGraph = revisionGraph;
    }

    /**
     * The constructor
     *
     * @param revisionGraph the revision graph
     * @param changeSetURI the change set URI
     * @throws InternalErrorException
     */
    public ChangeSet(RevisionGraph revisionGraph, String changeSetURI) {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.priorRevision = null;
        this.addSetURI = null;
        this.deleteSetURI = null;
        this.changeSetURI = changeSetURI;

        this.revisionGraph = revisionGraph;
    }

    /**
     * Get the prior revision.
     *
     * @return the prior revision
     */
    public Revision getPriorRevision() {
        if (this.priorRevision == null) {
            String query = Config.prefixes + String.format(
                    "SELECT ?priorRevision %n" +
                            "WHERE { GRAPH <%s> {<%s> rmo:priorRevision ?priorRevision .} }"
                    , this.revisionGraph.getRevisionGraphUri(), this.changeSetURI);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);

            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                try {
                    this.priorRevision = new Revision(this.revisionGraph, qs.getResource("?priorRevision").toString(), false);
                } catch (InternalErrorException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.succeedingRevision;
    }

    /**
     * Get the succeeding revision.
     *
     * @return the succeeding revision
     */
    public Revision getSucceedingRevision() {
        if (this.succeedingRevision == null) {
            String query = Config.prefixes + String.format(
                    "SELECT ?succeedingRevision %n" +
                            "WHERE { GRAPH <%s> {<%s> rmo:succeedingRevision ?succeedingRevision .} }"
                    , this.revisionGraph.getRevisionGraphUri(), this.changeSetURI);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);

            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                try {
                    this.succeedingRevision = new Revision(this.revisionGraph, qs.getResource("?succeedingRevision").toString(), false);
                } catch (InternalErrorException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.succeedingRevision;
    }

    /**
     * Get the revision URI.
     *
     * @return the revision URI
     */
    public String getChangeSetURI() {
        return changeSetURI;
    }

    /**
     * Get the ADD set URI.
     *
     * @return the ADD set URI
     */
    public String getAddSetURI() {
        if (this.addSetURI == null) {
            logger.debug("Get additional information of current change set " + changeSetURI + ".");
            String query = Config.prefixes + String.format(""
                    + "SELECT ?addSetURI "
                    + "WHERE { GRAPH  <%s> {"
                    + "	<%s> a rmo:ChangeSet; "
                    + "	 rmo:addSet ?addSetURI. "
                    + "} }", revisionGraph.getRevisionGraphUri(), changeSetURI);
            this.logger.debug(query);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                this.addSetURI = qs.getResource("?addSetURI").toString();
            }
        }

        return this.addSetURI;
    }

    /**
     * Get the DELETE set URI.
     *
     * @return the DELETE set URI
     */
    public String getDeleteSetURI() {
        if (this.deleteSetURI == null) {
            logger.debug("Get additional information of current change set " + changeSetURI + ".");
            String query = Config.prefixes + String.format(""
                    + "SELECT ?deleteSetURI "
                    + "WHERE { GRAPH  <%s> {"
                    + "	<%s> a rmo:ChangeSet; "
                    + "	 rmo:deleteSet ?deleteSetURI. "
                    + "} }", revisionGraph.getRevisionGraphUri(), changeSetURI);
            this.logger.debug(query);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                this.deleteSetURI = qs.getResource("?deleteSetURI").toString();
            }
        }

        return this.deleteSetURI;
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
     * Get the add set content.
     *
     * @return the add set content
     */
    public String getAddSetContent() {
        if (addSetContent == null) {
            // Calculate the ADD set content
            this.addSetContent = Helper.getContentOfNamedGraphAsN3(this.addSetURI);
        }
        return addSetContent;
    }

    /**
     * Get the delete set content.
     *
     * @return the delete set content
     */
    public String getDeleteSetContent() {
        if (deleteSetContent == null) {
            // Calculate the ADD set content
            this.deleteSetContent = Helper.getContentOfNamedGraphAsN3(this.deleteSetURI);
        }
        return deleteSetContent;
    }

}