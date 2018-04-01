package de.tud.plt.r43ples.core;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Provides access to the basic revision graph which stores all references to sub revision graphs.
 *
 * @author Stephan Hensel
 */
public class RevisionManagement {

    /**
     * The logger.
     **/
    private Logger logger = Logger.getLogger(RevisionManagement.class);

    // Dependencies
    /**
     * The triplestore interface to use.
     **/
    private TripleStoreInterface tripleStoreInterface;


    /**
     * The constructor.
     */
    public RevisionManagement() {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

    }

    /**
     * Get revised graphs in R43ples.
     *
     * @return result set
     */
    private ResultSet getRevisedGraphs() {
        String sparqlQuery = Config.prefixes
                + String.format(""
                + "SELECT DISTINCT ?graph "
                + "WHERE {"
                + " GRAPH <%s> {  ?graph a rmo:Graph }"
                + "} ORDER BY ?graph", Config.revision_graph);
        return tripleStoreInterface.executeSelectQuery(sparqlQuery);
    }


    /**
     * Get revised graphs in R43ples as list of string.
     *
     * @return list of strings containing the revised graphs of R43ples
     */
    public ArrayList<String> getRevisedGraphsList() {
        ArrayList<String> list = new ArrayList<String>();
        ResultSet results = getRevisedGraphs();
        while (results.hasNext()) {
            QuerySolution qs = results.next();
            list.add(qs.getResource("graph").toString());
        }
        return list;
    }

    /**
     * Get all named graphs URIs by querying the main revision graph.
     *
     * @return the list of named graphs URIs
     */
    private ArrayList<String> getAllNamedGraphsURIs() {
        this.logger.debug("Get all named graph URIs.");

        ArrayList<String> uriList = new ArrayList<>();

        String query = Config.prefixes + String.format(""
                + "SELECT DISTINCT ?uri\n"
                + "WHERE {\n"
                + "  GRAPH <%s> {\n"
                + "    ?graph a rmo:Graph;\n"
                + "      rmo:hasRevisionGraph ?uriGraph.\n"
                + "  }\n"
                + "  GRAPH ?uriGraph {\n"
                + "    { bind( ?uriGraph as ?uri) }\n"
                + "    UNION\n"
                + "    { ?ref rmo:fullGraph ?uri. }\n"
                + "    UNION\n"
                + "    { ?rev rmo:addSet ?uri. }\n"
                + "    UNION\n"
                + "    { ?rev rmo:deleteSet ?uri }\n"
                + "  }\n"
                + "}\n", Config.revision_graph);
        this.logger.debug(query);
        ResultSet resultSet = tripleStoreInterface.executeSelectQuery(query);
        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            uriList.add(qs.getResource("?uri").toString());
        }

        return uriList;
    }

    /**
     * Checks if a named graph URI is already used by R43ples.
     *
     * @param namedGraphURI the named graph URI to check
     * @return true if the named graph URI is already in use
     */
    protected boolean checkNamedGraphExistence(String namedGraphURI) {
        return getAllNamedGraphsURIs().contains(namedGraphURI);
    }

    /**
     * Get a new revision graph URI.
     *
     * @param revisionGraphName  the revision graph name
     * @return the new revision graph URI
     * @throws InternalErrorException
     */
    protected String getNewRevisionGraphURI(String revisionGraphName) throws InternalErrorException {
        String revisionGraphURI = revisionGraphName + "-revisiongraph";
        if (!checkNamedGraphExistence(revisionGraphURI)) {
            return revisionGraphURI;
        } else {
            throw new InternalErrorException("The calculated revision graph URI is already in use.");
        }
    }

    /**
     * Get a new full graph (named graph) URI.
     *
     * @param revisionGraph the revision graph
     * @param referenceIdentifier the reference identifier
     * @return the new full graph URI
     * @throws InternalErrorException
     */
    protected String getNewFullGraphURI(RevisionGraph revisionGraph, String referenceIdentifier) throws InternalErrorException {
        String fullGraphURI;
        try {
            fullGraphURI = revisionGraph.getGraphName() + "-" + URLEncoder.encode(referenceIdentifier, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new InternalErrorException("The specified reference identifier could not be URL encoded.");
        }

        if (!checkNamedGraphExistence(fullGraphURI)) {
            return fullGraphURI;
        } else {
            throw new InternalErrorException("The calculated full graph URI is already in use.");
        }
    }

    /**
     * Get a master full graph (named graph) URI.
     *
     * @param revisionGraph the revision graph
     * @return the new full graph URI
     * @throws InternalErrorException
     */
    protected String getMasterFullGraphURI(RevisionGraph revisionGraph) throws InternalErrorException {
        String fullGraphURI = revisionGraph.getGraphName();
        if (!checkNamedGraphExistence(fullGraphURI)) {
            return fullGraphURI;
        } else {
            throw new InternalErrorException("The calculated full graph URI is already in use.");
        }
    }

    /**
     * Get a new revision URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param revisionIdentifier the revision identifier of the corresponding revision
     * @return the new revision URI
     * @throws InternalErrorException
     */
    protected String getNewRevisionURI(RevisionGraph revisionGraph, String revisionIdentifier) throws InternalErrorException {
        String revisionURI = revisionGraph.getGraphName() + "-revision-" + revisionIdentifier;
        if (!checkNamedGraphExistence(revisionURI)) {
            return revisionURI;
        } else {
            throw new InternalErrorException("The calculated revision URI is already in use.");
        }
    }

    /**
     * Get a new change set URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param priorRevision      the prior revision
     * @param newRevisionIdentifier the new revision identifier
     * @return the new change set URI
     * @throws InternalErrorException
     */
    protected String getNewChangeSetURI(RevisionGraph revisionGraph, Revision priorRevision, String newRevisionIdentifier) throws InternalErrorException {
        String changeSetURI;
        if (priorRevision != null) {
            changeSetURI = revisionGraph.getGraphName() + "-changeset-" + priorRevision.getRevisionIdentifier() + "-" + newRevisionIdentifier;
        } else {
            changeSetURI = revisionGraph.getGraphName() + "-changeset-" + newRevisionIdentifier;
        }
        if (!checkNamedGraphExistence(changeSetURI)) {
            return changeSetURI;
        } else {
            throw new InternalErrorException("The calculated revision URI is already in use.");
        }
    }

    /**
     * Get a new add set URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param priorRevision      the prior revision
     * @param newRevisionIdentifier the new revision identifier
     * @return the new add set URI
     * @throws InternalErrorException
     */
    protected String getNewAddSetURI(RevisionGraph revisionGraph, Revision priorRevision, String newRevisionIdentifier) throws InternalErrorException {
        String addSetURI;
        if (priorRevision != null) {
            addSetURI = revisionGraph.getGraphName() + "-addSet-" + priorRevision.getRevisionIdentifier() + "-" + newRevisionIdentifier;
        } else {
            addSetURI = revisionGraph.getGraphName() + "-addSet-" + newRevisionIdentifier;
        }
        if (!checkNamedGraphExistence(addSetURI)) {
            return addSetURI;
        } else {
            throw new InternalErrorException("The calculated add set URI is already in use.");
        }
    }

    /**
     * Get a new delete set URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param priorRevision      the prior revision
     * @param newRevisionIdentifier the new revision identifier
     * @return the new delete set URI
     * @throws InternalErrorException
     */
    protected String getNewDeleteSetURI(RevisionGraph revisionGraph, Revision priorRevision, String newRevisionIdentifier) throws InternalErrorException {
        String deleteSetURI;
        if (priorRevision != null) {
            deleteSetURI = revisionGraph.getGraphName() + "-deleteSet-" + priorRevision.getRevisionIdentifier() + "-" + newRevisionIdentifier;
        } else {
            deleteSetURI = revisionGraph.getGraphName() + "-deleteSet-" + newRevisionIdentifier;
        }
        if (!checkNamedGraphExistence(deleteSetURI)) {
            return deleteSetURI;
        } else {
            throw new InternalErrorException("The calculated delete set URI is already in use.");
        }
    }

    /**
     * Get a new branch URI.
     *
     * @param revisionGraph    the corresponding revision graph
     * @param branchIdentifier the branch identifier of the corresponding revision
     * @return the new branch URI
     * @throws InternalErrorException
     */
    protected String getNewBranchURI(RevisionGraph revisionGraph, String branchIdentifier) throws InternalErrorException {
        String branchURI;
        try {
            branchURI = revisionGraph.getGraphName() + "-branch-" + URLEncoder.encode(branchIdentifier, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new InternalErrorException("The specified branch identifier could not be URL encoded.");
        }

        if (!checkNamedGraphExistence(branchURI)) {
            return branchURI;
        } else {
            throw new InternalErrorException("The calculated branch URI is already in use.");
    }
    }

    /**
     * Get a new master URI.
     *
     * @param revisionGraph    the corresponding revision graph
     * @return the new master URI
     * @throws InternalErrorException
     */
    protected String getNewMasterURI(RevisionGraph revisionGraph) throws InternalErrorException {
        String masterURI = revisionGraph.getGraphName() + "-master";
        if (!checkNamedGraphExistence(masterURI)) {
            return masterURI;
        } else {
            throw new InternalErrorException("The calculated master URI is already in use.");
        }
    }

    /**
     * Get a new tag URI.
     *
     * @param revisionGraph the corresponding revision graph
     * @param tagIdentifier the tag identifier of the corresponding revision
     * @return the new tag URI
     * @throws InternalErrorException
     */
    protected String getNewTagURI(RevisionGraph revisionGraph, String tagIdentifier) throws InternalErrorException {
        String tagURI;
        try {
            tagURI = revisionGraph.getGraphName() + "-tag-" + URLEncoder.encode(tagIdentifier, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new InternalErrorException("The specified tag identifier could not be URL encoded.");
        }

        if (!checkNamedGraphExistence(tagURI)) {
            return tagURI;
        } else {
            throw new InternalErrorException("The calculated tag URI is already in use.");
        }
    }

    /**
     * Get a new commit URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param revisionIdentifier the revision identifier of the created revision
     * @return the new commit URI
     * @throws InternalErrorException
     */
    protected String getNewCommitURI(RevisionGraph revisionGraph, String revisionIdentifier) throws InternalErrorException {
        String commitURI = revisionGraph.getGraphName() + "-commit-" + revisionIdentifier;
        if (!checkNamedGraphExistence(commitURI)) {
            return commitURI;
        } else {
            throw new InternalErrorException("The calculated commit URI is already in use.");
        }
    }

    /**
     * Get a new three way merge commit URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param revisionIdentifier the revision identifier of the created revision
     * @return the new commit URI
     * @throws InternalErrorException
     */
    protected String getNewThreeWayMergeCommitURI(RevisionGraph revisionGraph, String revisionIdentifier) throws InternalErrorException {
        String commitURI = revisionGraph.getGraphName() + "-commit-merge-" + revisionIdentifier;
        if (!checkNamedGraphExistence(commitURI)) {
            return commitURI;
        } else {
            throw new InternalErrorException("The calculated commit URI is already in use.");
        }
    }

    /**
     * Get a new fast forward merge commit URI.
     *
     * @param revisionGraph the corresponding revision graph
     * @param sourceRevisionIdentifier the source revision identifier
     * @param targetRevisionIdentifier the target revision identifier
     * @return the new commit URI
     * @throws InternalErrorException
     */
    protected String getNewFastForwardMergeCommitURI(RevisionGraph revisionGraph, String sourceRevisionIdentifier, String targetRevisionIdentifier) throws InternalErrorException {
        String commitURI = revisionGraph.getGraphName() + "-commit-ff-merge-" + sourceRevisionIdentifier + "-" + targetRevisionIdentifier;
        if (!checkNamedGraphExistence(commitURI)) {
            return commitURI;
        } else {
            throw new InternalErrorException("The calculated commit URI is already in use.");
        }
    }

    /**
     * Get a new rebase merge commit URI.
     *
     * @param revisionGraph the corresponding revision graph
     * @param startRevisionIdentifier the start revision identifier
     * @param endRevisionIdentifier the end revision identifier
     * @param targetBranchIdentifier the target branch identifier
     * @param targetRevisionIdentifier the target revision identifier
     * @return the new commit URI
     * @throws InternalErrorException
     */
    protected String getNewPickCommitURI(RevisionGraph revisionGraph, String startRevisionIdentifier, String endRevisionIdentifier, String targetBranchIdentifier, String targetRevisionIdentifier) throws InternalErrorException {
        if (endRevisionIdentifier == null) {
            endRevisionIdentifier = "";
        }
        String commitURI = revisionGraph.getGraphName() + "-commit-pick-" + startRevisionIdentifier + "-" + endRevisionIdentifier + "-" + targetBranchIdentifier + "-" + targetRevisionIdentifier;
        if (!checkNamedGraphExistence(commitURI)) {
            return commitURI;
        } else {
            throw new InternalErrorException("The calculated commit URI is already in use.");
        }
    }

    /**
     * Get a new branch commit URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param branchIdentifier the branch identifier of the created branch
     * @return the new branch commit URI
     * @throws InternalErrorException
     */
    protected String getNewBranchCommitURI(RevisionGraph revisionGraph, String branchIdentifier) throws InternalErrorException {
        String commitURI;
        try {
            commitURI = revisionGraph.getGraphName() + "-commit-branch-" + URLEncoder.encode(branchIdentifier, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new InternalErrorException("The specified branch identifier could not be URL encoded.");
        }

        if (!checkNamedGraphExistence(commitURI)) {
            return commitURI;
        } else {
            throw new InternalErrorException("The calculated branch commit URI is already in use.");
        }
    }

    /**
     * Get a new tag commit URI.
     *
     * @param revisionGraph    the corresponding revision graph
     * @param tagIdentifier the tag identifier of the created tag
     * @return the new tag commit URI
     * @throws InternalErrorException
     */
    protected String getNewTagCommitURI(RevisionGraph revisionGraph, String tagIdentifier) throws InternalErrorException {
        String commitURI;
        try {
            commitURI = revisionGraph.getGraphName() + "-commit-tag-" + URLEncoder.encode(tagIdentifier, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new InternalErrorException("The specified tag identifier could not be URL encoded.");
        }

        if (!checkNamedGraphExistence(commitURI)) {
            return commitURI;
        } else {
            throw new InternalErrorException("The calculated tag commit URI is already in use.");
        }
    }

    /**
     * Get a new temporary revision progress URI (from).
     *
     * @param revisionGraph    the corresponding revision graph
     * @return the new temporary revision progress URI (from)
     * @throws InternalErrorException
     */
    protected String getTemporaryRevisionProgressFromURI(RevisionGraph revisionGraph) throws InternalErrorException {
        // TODO Create one method which calculates a temporary random URI (Attention the methods which are using this URI will have to drop the named graph after the usage is finished!)
        String tempURI = revisionGraph.getGraphName() + "-RM-REVISION-PROGRESS-FROM";
        if (!checkNamedGraphExistence(tempURI)) {
            return tempURI;
        } else {
            throw new InternalErrorException("The calculated temporary URI is already in use.");
        }
    }

    /**
     * Get a new temporary revision progress URI (into).
     *
     * @param revisionGraph    the corresponding revision graph
     * @return the new temporary revision progress URI (into)
     * @throws InternalErrorException
     */
    protected String getTemporaryRevisionProgressIntoURI(RevisionGraph revisionGraph) throws InternalErrorException {
        // TODO Create one method which calculates a temporary random URI (Attention the methods which are using this URI will have to drop the named graph after the usage is finished!)
        String tempURI = revisionGraph.getGraphName() + "-RM-REVISION-PROGRESS-INTO";
        if (!checkNamedGraphExistence(tempURI)) {
            return tempURI;
        } else {
            throw new InternalErrorException("The calculated temporary URI is already in use.");
        }
    }

    /**
     * Get a new temporary difference model URI.
     *
     * @param revisionGraph    the corresponding revision graph
     * @return the new temporary difference model URI
     * @throws InternalErrorException
     */
    protected String getTemporaryDifferenceModelURI(RevisionGraph revisionGraph) throws InternalErrorException {
        // TODO Create one method which calculates a temporary random URI (Attention the methods which are using this URI will have to drop the named graph after the usage is finished!)
        String tempURI = revisionGraph.getGraphName() + "-RM-DIFFERENCE-MODEL";
        if (!checkNamedGraphExistence(tempURI)) {
            return tempURI;
        } else {
            throw new InternalErrorException("The calculated temporary URI is already in use.");
        }
    }

    /**
     * Get a new temporary merged URI.
     *
     * @param revisionGraph    the corresponding revision graph
     * @return the new temporary difference model URI
     * @throws InternalErrorException
     */
    protected String getTemporaryMergedURI(RevisionGraph revisionGraph) throws InternalErrorException {
        // TODO Create one method which calculates a temporary random URI (Attention the methods which are using this URI will have to drop the named graph after the usage is finished!)
        String tempURI = revisionGraph.getGraphName() + "-RM-MERGED-TEMP";
        if (!checkNamedGraphExistence(tempURI)) {
            return tempURI;
        } else {
            throw new InternalErrorException("The calculated temporary URI is already in use.");
        }
    }

}