package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.management.SparqlRewriter;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements the R43ples core interface and provides methods to access the core functions of R43ples.
 *
 * @author Stephan Hensel
 *
 */
public class R43plesCore implements R43plesCoreInterface {

    /** The logger. **/
    private Logger logger = Logger.getLogger(R43plesCore.class);


    /**
     * The constructor.
     */
    protected R43plesCore() {

    }

    /**
     * Create a new initial commit.
     *
     * @param request the request received by R43ples
     * @return the created initial commit
     * @throws InternalErrorException
     */
    @Override
    public InitialCommit createInitialCommit(R43plesRequest request) throws InternalErrorException {
        InitialCommitDraft initialCommitDraft = new InitialCommitDraft(request);
        return initialCommitDraft.createInTripleStore();
    }

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
    @Override
    public InitialCommit createInitialCommit(String graphName, String addSet, String deleteSet, String user, String message) throws InternalErrorException {
        InitialCommitDraft initialCommitDraft = new InitialCommitDraft(graphName, addSet, deleteSet, user, message);
        return initialCommitDraft.createInTripleStore();
    }

    /**
     * Create a new update commit.
     *
     * @param request the request received by R43ples
     * @return the list of created update commits
     * @throws InternalErrorException
     */
    @Override
    public ArrayList<UpdateCommit> createUpdateCommit(R43plesRequest request) throws InternalErrorException {
        UpdateCommitDraft updateCommitDraft = new UpdateCommitDraft(request);
        return updateCommitDraft.createInTripleStore();
    }

    /**
     * Create a new update commit.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @param branchIdentifier the branch identifier (new revision derive from leaf of branch)
     * @return the created update commit
     * @throws InternalErrorException
     */
    @Override
    public UpdateCommit createUpdateCommit(String graphName, String addSet, String deleteSet, String user, String message, String branchIdentifier) throws InternalErrorException {
        RevisionGraph revisionGraph = new RevisionGraph(graphName);
        Branch branch = new Branch(revisionGraph, branchIdentifier, true);
        UpdateCommitDraft updateCommitDraft = new UpdateCommitDraft(graphName, addSet, deleteSet, user, message, branch);
        return updateCommitDraft.createInTripleStore().get(0);
    }

    /**
     * Create a new revert commit. Reverts the last revision of specified branch.
     *
     * @param request the request received by R43ples
     * @return the created revert commit
     * @throws InternalErrorException
     */
    @Override
    public RevertCommit createRevertCommit(R43plesRequest request) throws InternalErrorException {
        RevertCommitDraft revertCommitDraft = new RevertCommitDraft(request);
        return revertCommitDraft.createInTripleStore();
    }

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
    @Override
    public RevertCommit createRevertCommit(RevisionGraph revisionGraph, Branch branch, String user, String message) throws InternalErrorException {
        RevertCommitDraft revertCommitDraft = new RevertCommitDraft(revisionGraph, user, message, branch);
        return revertCommitDraft.createInTripleStore();
    }

    /**
     * Create a new reference commit.
     *
     * @param request the request received by R43ples
     * @return the created reference commit
     * @throws InternalErrorException
     */
    @Override
    public ReferenceCommit createReferenceCommit(R43plesRequest request) throws InternalErrorException {
        ReferenceCommitDraft referenceCommitDraft = new ReferenceCommitDraft(request);
        return referenceCommitDraft.createInTripleStore();
    }

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
    @Override
    public ReferenceCommit createReferenceCommit(RevisionGraph revisionGraph, String referenceName, Revision baseRevision, String user, String message, boolean isBranch) throws InternalErrorException {
        ReferenceCommitDraft referenceCommitDraft = new ReferenceCommitDraft(revisionGraph, referenceName, baseRevision, user, message, isBranch);
        return referenceCommitDraft.createInTripleStore();
    }

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
    @Override
    public BranchCommit createBranchCommit(RevisionGraph revisionGraph, String referenceName, Revision baseRevision, String user, String message) throws InternalErrorException {
        BranchCommitDraft branchCommitDraft = new BranchCommitDraft(revisionGraph, referenceName, baseRevision, user, message);
        return branchCommitDraft.createInTripleStore();
    }

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
    @Override
    public TagCommit createTagCommit(RevisionGraph revisionGraph, String referenceName, Revision baseRevision, String user, String message) throws InternalErrorException {
        TagCommitDraft tagCommitDraft = new TagCommitDraft(revisionGraph, referenceName, baseRevision, user, message);
        return tagCommitDraft.createInTripleStore();
    }

    /**
     * Create a new merge commit.
     *
     * @param request the request received by R43ples
     * @return the created merge commit
     * @throws InternalErrorException
     */
    @Override
    public MergeCommit createMergeCommit(R43plesRequest request) throws InternalErrorException {
        MergeCommitDraft mergeCommitDraft = new MergeCommitDraft(request);
        return mergeCommitDraft.createCommitInTripleStore();
    }

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
    @Override
    public ThreeWayMergeCommit createThreeWayMergeCommit(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String sdd, String triples, MergeTypes type, boolean with) throws InternalErrorException {
        ThreeWayMergeCommitDraft threeWayMergeCommit = new ThreeWayMergeCommitDraft(graphName, branchNameFrom, branchNameInto, user, message, sdd, triples, type, with);
        return threeWayMergeCommit.createCommitInTripleStore();
    }

    /**
     * Create a new pick commit.
     *
     * @param request the request received by R43ples
     * @return the created pick commit
     * @throws InternalErrorException
     */
    @Override
    public PickCommit createPickCommit(R43plesRequest request) throws InternalErrorException {
        PickCommitDraft pickCommitDraft = new PickCommitDraft(request);
        return pickCommitDraft.createCommitInTripleStore();
    }

    /**
     * Drop graph query. This query will delete the whole graph and all corresponding revision information.
     *
     * @param query the query
     * @throws QueryErrorException
    */
    @Override
    public void sparqlDropGraph(final String query) throws QueryErrorException {
        /* The pattern modifier. */
        int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
        final Pattern patternDropGraph = Pattern.compile("DROP\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
                patternModifier);
        Matcher m = patternDropGraph.matcher(query);
        boolean found = false;
        while (m.find()) {
            found = true;
            String graphName = m.group("graph");
            RevisionGraph graph = new RevisionGraph(graphName);
            graph.purgeRevisionInformation();
        }
        if (!found) {
            throw new QueryErrorException("Query contain errors:\n" + query);
        }
    }

    /**
     * Get the response of a SPARQL query (SELECT, CONSTRUCT, ASK).
     *
     * @param request the request
     * @param query_rewriting option if query rewriting should be enabled (true => enabled)
     * @return the query response
     * @throws InternalErrorException
     */
    @Override
    public String getSparqlSelectConstructAskResponse(final R43plesRequest request, final boolean query_rewriting) throws InternalErrorException {
        String result;
        if (query_rewriting) {
            String query_rewritten = SparqlRewriter.rewriteQuery(request.query_sparql);
            result = TripleStoreInterfaceSingleton.get()
                    .executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + query_rewritten, request.format);
        } else {
            SelectConstructAskQuery selectConstructAskQuery = new SelectConstructAskQuery(request);
            result = selectConstructAskQuery.performQuery();
        }
        return result;
    }

}