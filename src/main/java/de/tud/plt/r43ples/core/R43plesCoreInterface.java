package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.R43plesRequest;

import java.util.ArrayList;

/**
 * This interface provides methods to access the core functions of R43ples.
 *
 * @author Stephan Hensel
 *
 */
public interface R43plesCoreInterface {

    /**
     * Create a new initial commit.
     *
     * @param request the request received by R43ples
     * @return the created initial commit
     * @throws InternalErrorException
     */
    InitialCommit createInitialCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new initial commit.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @return the created update commit
     * @throws InternalErrorException
     */
    InitialCommit createInitialCommit(String graphName, String addSet, String deleteSet, String user, String message) throws InternalErrorException;


    /**
     * Create a new update commit.
     *
     * @param request the request received by R43ples
     * @return the list of created update commits
     * @throws InternalErrorException
     */
    ArrayList<UpdateCommit> createUpdateCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new update commit.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @param derivedFromIdentifier the revision identifier of the revision or the reference identifier from which the new revision should be derive from
     * @return the created update commit
     * @throws InternalErrorException
     */
    UpdateCommit createUpdateCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifier) throws InternalErrorException;

    /**
     * Create a new revert commit. Reverts the last revision of specified branch.
     *
     * @param request the request received by R43ples
     * @return the created revert commit
     * @throws InternalErrorException
     */
    RevertCommit createRevertCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new revert commit. Reverts the leaf revision of specified branch.
     *
     * @param revisionGraph the revision graph
     * @param branch the branch (new revision derive from leaf of branch)
     * @param user the user
     * @param message the message
     * @return the created revert commit
     * @throws InternalErrorException
     */
    RevertCommit createRevertCommit(RevisionGraph revisionGraph, Branch branch, String user, String message) throws InternalErrorException;

    /**
     * Create a new reference commit.
     *
     * @param request the request received by R43ples
     * @return the created reference commit
     * @throws InternalErrorException
     */
    ReferenceCommit createReferenceCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Create a new reference commit.
     *
     * @param revisionGraph the revision graph
     * @param referenceName the reference name
     * @param baseRevision the base revision (this revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @param isBranch states if the created reference is a branch or a tag. (branch => true; tag => false)
     * @return the created reference commit
     * @throws InternalErrorException
     */
    ReferenceCommit createReferenceCommit(RevisionGraph revisionGraph, String referenceName, Revision baseRevision, String user, String message, boolean isBranch) throws InternalErrorException;

    /**
     * Create a new branch commit.
     *
     * @param revisionGraph the revision graph
     * @param referenceName the reference name
     * @param baseRevision the base revision (this revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @return the created branch commit
     * @throws InternalErrorException
     */
    BranchCommit createBranchCommit(RevisionGraph revisionGraph, String referenceName, Revision baseRevision, String user, String message) throws InternalErrorException;

    /**
     * Create a new tag commit.
     *
     * @param revisionGraph the revision graph
     * @param referenceName the reference name
     * @param baseRevision the base revision (this revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @return the created tag commit
     * @throws InternalErrorException
     */
    TagCommit createTagCommit(RevisionGraph revisionGraph, String referenceName, Revision baseRevision, String user, String message) throws InternalErrorException;

    /**
     * Create a new merge commit.
     *
     * @param request the request received by R43ples
     * @return the created merge commit
     * @throws InternalErrorException
     */
    MergeCommit createMergeCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Creates a three way merge commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param branchNameFrom the branch name (from)
     * @param branchNameInto the branch name (into)
     * @param user the user
     * @param message the message
     * @param sdd the SDD URI to use
     * @param triples the triples of the query WITH part
     * @param type the query type (FORCE, AUTO, MANUAL)
     * @param with states if the WITH part is available
     * @throws InternalErrorException
     */
    ThreeWayMergeCommit createThreeWayMergeCommit(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String sdd, String triples, MergeTypes type, boolean with) throws InternalErrorException;

    /**
     * Create a new pick commit.
     *
     * @param request the request received by R43ples
     * @return the created pick commit
     * @throws InternalErrorException
     */
    PickCommit createPickCommit(R43plesRequest request) throws InternalErrorException;

    /**
     * Aggregate atomic changes to high level ones.
     *
     * @param request the request received by R43ples
     * @return the aggregated high level changes
     * @throws InternalErrorException
     */
    HighLevelChanges aggregate(R43plesRequest request) throws InternalErrorException;

    // TODO Create objects for drop and select
    /**
     * Drop graph query. This query will delete the whole graph and all corresponding revision information.
     *
     * @param query the query
     * @throws QueryErrorException
     */
    void sparqlDropGraph(final String query) throws QueryErrorException;

    /**
     * Get the response of a SPARQL query (SELECT, CONSTRUCT, ASK).
     *
     * @param request the request
     * @param query_rewriting option if query rewriting should be enabled (true => enabled)
     * @return the query response
     * @throws InternalErrorException
     */
    String getSparqlSelectConstructAskResponse(final R43plesRequest request, final boolean query_rewriting) throws InternalErrorException;
}