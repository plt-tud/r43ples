package de.tud.plt.r43ples.existentobjects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.iohelper.JenaModelManagement;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent change set.
 *
 * @author Stephan Hensel
 */
public class ChangeSet {

    /** The logger. **/
    private Logger logger = Logger.getLogger(ChangeSet.class);

    /** The prior revision. **/
    private Revision priorRevision;
    /**
     * The succesor revision.
     **/
    private Revision succesorRevision;

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
    public ChangeSet(RevisionGraph revisionGraph, Revision priorRevision, String addSetURI, String deleteSetURI, String changeSetURI) throws InternalErrorException {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

        this.priorRevision = priorRevision;
        this.addSetURI = addSetURI;
        this.deleteSetURI = deleteSetURI;
        this.changeSetURI = changeSetURI;

        this.revisionGraph = revisionGraph;
    }

    /**
     * Get the prior revision.
     *
     * @return the prior revision
     */
    public Revision getPriorRevision() {
        return priorRevision;
    }

    /**
     * Get the successor revision.
     *
     * @return the successor revision
     */
    public Revision getSuccessorRevision() {
        if (this.succesorRevision == null) {
            String query = Config.prefixes + String.format(
                    "SELECT ?successor %n" +
                            "WHERE { GRAPH <%s> {?successor rmo:hasChangeSet <%s> .} }"
                    , this.revisionGraph.getRevisionGraphUri(), this.changeSetURI);
            ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);

            if (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                try {
                    this.succesorRevision = new Revision(this.revisionGraph, qs.getResource("?successor").toString(), false);
                } catch (InternalErrorException e) {
                    e.printStackTrace();
                }
            }
        }
        return this.succesorRevision;
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
        return addSetURI;
    }

    /**
     * Get the DELETE set URI.
     *
     * @return the DELETE set URI
     */
    public String getDeleteSetURI() {
        return deleteSetURI;
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
            this.addSetContent = getContentOfNamedGraphAsN3(this.addSetURI);
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
            this.deleteSetContent = getContentOfNamedGraphAsN3(this.deleteSetURI);
        }
        return deleteSetContent;
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