package de.tud.plt.r43ples.existentobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

/**
 * Provides information of an already existent semantic change.
 *
 * @author Stephan Hensel
 */
public class SemanticChange {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(Commit.class);

    /** The semantic change URI. */
    private String semanticChangeURI;
    /** The used rule URI. */
    private String usedRuleURI;
    /** The list of SPARQL variables. */
    private LinkedList<SparqlVariable> sparqlVariableList;
    /** The additions as a statement set. */
    private StatementSet additions;
    /** The deletions as a statement set. */
    private StatementSet deletions;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param semanticChangeURI the semantic change URI
     * @throws InternalErrorException
     */
    public SemanticChange(RevisionGraph revisionGraph, String semanticChangeURI) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.semanticChangeURI = semanticChangeURI;
        this.sparqlVariableList = new LinkedList<>();

        retrieveAdditionalInformation();
    }

    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param semanticChangeURI semantic change URI
     * @param usedRuleURI used rule URI
     * @param sparqlVariableList list of SPARQL variables
     * @param additions the additions as a statement set
     * @param deletions the deletions as a statement set
     */
    public SemanticChange(RevisionGraph revisionGraph, String semanticChangeURI, String usedRuleURI, LinkedList<SparqlVariable> sparqlVariableList, StatementSet additions, StatementSet deletions) {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();
        this.semanticChangeURI = semanticChangeURI;
        this.usedRuleURI = usedRuleURI;
        this.sparqlVariableList = sparqlVariableList;
        this.additions = additions;
        this.deletions = deletions;
    }

    /**
     * Calculate additional information of the current commit and store this information to local variables.
     *
     * @throws InternalErrorException
     */
    private void retrieveAdditionalInformation() throws InternalErrorException {
        logger.info("Get additional information of current semantic change URI " + semanticChangeURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?usedRule ?variable ?additionsURI ?deletionsURI "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rmo:SemanticChange; "
                + "	 aero:usedRule ?usedRule; "
                + "  aero:hasVariables ?variable; "
                + "  rmo:additions ?additionsURI; "
                + "  rmo:deletions ?deletionsURI. "
                + "} }", revisionGraphURI, semanticChangeURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            usedRuleURI = qs.getResource("?usedRule").toString();

            additions = new StatementSet(revisionGraph, qs.getResource("?additionsURI").toString());
            additions = new StatementSet(revisionGraph, qs.getResource("?deletionsURI").toString());

            SparqlVariable sparqlVariable = new SparqlVariable(revisionGraph, qs.getResource("?variable").toString());
            sparqlVariableList.add(sparqlVariable);

            while (resultSet.hasNext()) {
                qs = resultSet.next();
                SparqlVariable furtherSparqlVariable = new SparqlVariable(revisionGraph, qs.getResource("?variable").toString());
                sparqlVariableList.add(furtherSparqlVariable);
            }
        } else {
            throw new InternalErrorException("No additional information found for semantic change URI " + semanticChangeURI + ".");
        }
    }

    /**
     * Get the semantic change URI.
     *
     * @return the semantic change URI
     */
    public String getSemanticChangeURI() {
        return semanticChangeURI;
    }

    /**
     * Get the used rule URI.
     *
     * @return the used rule URI
     */
    public String getUsedRuleURI() {
        return usedRuleURI;
    }

    /**
     * Get the list of SPARQL variables.
     *
     * @return the list of SPARQL variables
     */
    public LinkedList<SparqlVariable> getSparqlVariableList() {
        return sparqlVariableList;
    }

    /**
     * Get additions as a statement set.
     *
     * @return the additions as a statement set
     */
    public StatementSet getAdditons() {
        return additions;
    }

    /**
     * Get deletions as a statement set.
     *
     * @return the deletions as a statement set
     */
    public StatementSet getDeletions() {
        return deletions;
    }

    /**
     * Get the revision graph.
     *
     * @return the revision graph
     */
    public RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

}
