package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterface;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

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
    protected RevisionManagement() {
        // Dependencies
        this.tripleStoreInterface = TripleStoreInterfaceSingleton.get();

    }

    /**
     * Get all named graphs URIs by querying the main revision graph.
     *
     * @return the list of named graphs URIs
     */
    private ArrayList<String> getAllNamedGraphsURIs() {
        // TODO Create the corresponding SPARQL query
        return null;
    }

    /**
     * Checks if a named graph URI is already used by R43ples.
     *
     * @param namedGraphURI the named graph URI to check
     * @return true if the named graph URI is already in use
     */
    protected boolean checkNamedGraphExistence(String namedGraphURI) {
        //TODO return getAllNamedGraphsURIs().contains(namedGraphURI);
        return false;
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
     * Get a new add set URI.
     *
     * @param revisionGraph      the corresponding revision graph
     * @param revisionIdentifier the revision identifier of the corresponding revision
     * @return the new add set URI
     * @throws InternalErrorException
     */
    protected String getNewAddSetURI(RevisionGraph revisionGraph, String revisionIdentifier) throws InternalErrorException {
        String addSetURI = revisionGraph.getGraphName() + "-addSet-" + revisionIdentifier;
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
     * @param revisionIdentifier the revision identifier of the corresponding revision
     * @return the new delete set URI
     * @throws InternalErrorException
     */
    protected String getNewDeleteSetURI(RevisionGraph revisionGraph, String revisionIdentifier) throws InternalErrorException {
        String deleteSetURI = revisionGraph.getGraphName() + "-deleteSet-" + revisionIdentifier;
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
        String branchURI = revisionGraph.getGraphName() + "-branch-" + branchIdentifier;
        if (!checkNamedGraphExistence(branchURI)) {
            return branchURI;
        } else {
            throw new InternalErrorException("The calculated master URI is already in use.");
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
        String tagURI = revisionGraph.getGraphName() + "-tag-" + tagIdentifier;
        if (!checkNamedGraphExistence(tagURI)) {
            return tagURI;
        } else {
            throw new InternalErrorException("The calculated master URI is already in use.");
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

}