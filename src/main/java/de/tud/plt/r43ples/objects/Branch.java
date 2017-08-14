package de.tud.plt.r43ples.objects;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.RevisionGraph;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Provides information of an already existent branch.
 *
 * @author Stephan Hensel
 */
public class Branch {

    /** The logger. **/
    private Logger logger = Logger.getLogger(Branch.class);

    /** The branch identifier. */
    private String branchIdentifier;
    /** The branch URI. */
    private String branchURI;

    /** The revision graph URI. */
    private String revisionGraphURI;
    /** The corresponding revision graph. */
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     *
     * @param revisionGraph the revision graph
     * @param branchInformation the branch information (identifier or URI of the branch)
     * @param isIdentifier identifies if the identifier or the URI of the branch is specified (identifier => true; URI => false)
     * @throws InternalErrorException
     */
    public Branch(RevisionGraph revisionGraph, String branchInformation, boolean isIdentifier) throws InternalErrorException {
        this.revisionGraph = revisionGraph;
        this.revisionGraphURI = this.revisionGraph.getRevisionGraphUri();

        if (isIdentifier) {
            this.branchIdentifier = branchInformation;
            this.branchURI = calculateBranchURI(this.branchIdentifier);
        } else {
            this.branchURI = branchInformation;
            this.branchIdentifier = calculateBranchIdentifier(this.branchURI);
        }
    }

    /**
     * Get the corresponding commit of the current branch. This commit created this branch.
     *
     * @return the corresponding commit
     * @throws InternalErrorException
     */
    public Commit getCorrespondingCommit() throws InternalErrorException {
        logger.info("Get corresponding commit of branch " + branchIdentifier);
        String query = Config.prefixes + String.format(""
                + "SELECT ?com "
                + "WHERE { GRAPH  <%s> {"
                + "	?com a rmo:Commit; "
                + "	 prov:generated <%s>. "
                + "} }", revisionGraphURI, branchURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return new Commit(revisionGraph, qs.getResource("?com").toString());
        } else {
            throw new InternalErrorException("No corresponding commit found for branch " + branchIdentifier + ".");
        }
    }

    /**
     * Get the leaf revision of current branch.
     *
     * @return the leaf revision
     * @throws InternalErrorException
     */
    public Revision getLeafRevision() throws InternalErrorException {
        //TODO Implement method
        return null;
    }

    /**
     * Get the branch identifier.
     *
     * @return the branch identifier
     */
    public String getBranchIdentifier() {
        return branchIdentifier;
    }

    /**
     * Get the branch URI.
     *
     * @return the branch URI
     */
    public String getBranchURI() {
        return branchURI;
    }

    /**
     * Calculate the branch URI for a given branch identifier
     *
     * @param branchIdentifier the branch identifier
     * @return URI of identified branch
     * @throws InternalErrorException
     */
    private String calculateBranchURI(String branchIdentifier) throws InternalErrorException {
        logger.info("Calculate the branch URI for current branch " + branchIdentifier + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?uri "
                + "WHERE { GRAPH  <%s> {"
                + "	?uri a rmo:Branch; "
                + "	 rdfs:label \"%s\". "
                + "} }", revisionGraphURI, branchIdentifier);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return qs.getResource("?uri").toString();
        } else {
            throw new InternalErrorException("No branch URI found for branch " + branchIdentifier + ".");
        }
    }

    /**
     * Calculate the branch identifier for a given branch URI
     *
     * @param branchURI the branch URI
     * @return the branch identifier
     * @throws InternalErrorException
     */
    private String calculateBranchIdentifier(String branchURI) throws InternalErrorException {
        logger.info("Calculate the branch identifier for current branch URI " + branchURI + ".");
        String query = Config.prefixes + String.format(""
                + "SELECT ?id "
                + "WHERE { GRAPH  <%s> {"
                + "	<%s> a rmo:Branch; "
                + "	 rdfs:label ?id. "
                + "} }", revisionGraphURI, branchURI);
        this.logger.debug(query);
        ResultSet resultSet = TripleStoreInterfaceSingleton.get().executeSelectQuery(query);
        if (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            return qs.getLiteral("?id").toString();
        } else {
            throw new InternalErrorException("No branch identifier found for branch URI " + branchURI + ".");
        }
    }

}
