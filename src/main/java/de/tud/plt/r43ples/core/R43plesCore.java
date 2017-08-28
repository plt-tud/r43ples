package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.*;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.management.RevisionManagementOriginal;
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
        return initialCommitDraft.createCommitInTripleStore();
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
        return initialCommitDraft.createCommitInTripleStore();
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
        return updateCommitDraft.createCommitInTripleStore();
    }

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
    @Override
    public UpdateCommit createUpdateCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifier) throws InternalErrorException {
        UpdateCommitDraft updateCommitDraft = new UpdateCommitDraft(graphName, addSet, deleteSet, user, message, derivedFromIdentifier);
        return updateCommitDraft.createCommitInTripleStore().get(0);
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
        ReferenceCreationCommitDraft referenceCreationCommitDraft = new ReferenceCreationCommitDraft(request);
        return referenceCreationCommitDraft.createCommitInTripleStore();
    }

    /**
     * Create a new reference commit.
     *
     * @param graphName the graph name
     * @param referenceName the reference name
     * @param revisionIdentifier the revision identifier (the corresponding revision will be the current base for the reference)
     * @param user the user
     * @param message the message
     * @param isBranch states if the created reference is a branch or a tag. (branch => true; tag => false)
     * @return the created reference commit
     * @throws InternalErrorException
     */
    @Override
    public ReferenceCommit createReferenceCommit(String graphName, String referenceName, String revisionIdentifier, String user, String message, boolean isBranch) throws InternalErrorException {
        ReferenceCreationCommitDraft referenceCreationCommitDraft = new ReferenceCreationCommitDraft(graphName, referenceName, revisionIdentifier, user, message, isBranch);
        return referenceCreationCommitDraft.createCommitInTripleStore();
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


    @Override
    public ThreeWayMergeCommit createThreeWayMergeCommit(String graphName, String addSet, String deleteSet, String user, String message, String derivedFromIdentifierSource, String derivedFromIdentifierTarget) throws InternalErrorException {
        //TODO
        return null;
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
            result = getSparqlSelectConstructAskResponseClassic(request.query_sparql, request.format);
        }
        return result;
    }

    /**
     * Get the response of a SPARQL query (SELECT, CONSTRUCT, ASK). Classic way.
     *
     * @param query the query
     * @param format the result format
     * @return the query response
     * @throws InternalErrorException
     */
    private String getSparqlSelectConstructAskResponseClassic(final String query, final String format)
            throws InternalErrorException {
        final Pattern patternSelectFromPart = Pattern.compile(
                "(?<type>FROM|GRAPH)\\s*<(?<graph>[^>\\?]*)(\\?|>)(\\s*REVISION\\s*\"|revision=)(?<revision>([^\">]+))(>|\")",
                Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE);

        String queryM = query;

        Matcher m = patternSelectFromPart.matcher(queryM);
        while (m.find()) {
            String graphName = m.group("graph");
            String type = m.group("type");
            String revisionNumber = m.group("revision").toLowerCase();
            String newGraphName;

            RevisionGraph graph = new RevisionGraph(graphName);

            // if no revision number is declared use the MASTER as default
            if (revisionNumber == null) {
                revisionNumber = "master";
            }
            if (revisionNumber.equalsIgnoreCase("master")) {
                // Respond with MASTER revision - nothing to be done - MASTER
                // revisions are already created in the named graphs
                newGraphName = graphName;
            } else {
                if (graph.hasBranch(revisionNumber)) {
                    newGraphName = graph.getReferenceGraph(revisionNumber);
                } else {
                    // Respond with specified revision, therefore the revision
                    // must be generated - saved in graph <graphName-revisionNumber>
                    newGraphName = graphName + "-" + revisionNumber;
                    RevisionManagementOriginal.generateFullGraphOfRevision(graphName, revisionNumber, newGraphName);
                }
            }

            queryM = m.replaceFirst(type + " <" + newGraphName + ">");
            m = patternSelectFromPart.matcher(queryM);

        }
        String response = TripleStoreInterfaceSingleton.get()
                .executeSelectConstructAskQuery(Config.getUserDefinedSparqlPrefixes() + queryM, format);
        return response;
    }

}