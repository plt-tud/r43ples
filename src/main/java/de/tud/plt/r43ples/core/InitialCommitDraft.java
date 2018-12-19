package de.tud.plt.r43ples.core;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.iohelper.Helper;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new initial commit.
 *
 * @author Stephan Hensel
 */
public class InitialCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = LogManager.getLogger(InitialCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

    /** The revision draft. **/
    private RevisionDraft revisionDraft;
    /** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
    private boolean isCreatedWithRequest;
    /** The revision graph. **/
    private RevisionGraph revisionGraph;


    /**
     * The constructor.
     * Creates an initial commit draft by using the corresponding R43ples request.
     *
     * @param request the request received by R43ples
     * @throws InternalErrorException
     */
    public InitialCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
        String graphName = getGraphNameOfRequest();
        this.revisionGraph = new RevisionGraph(graphName, getUriCalculator().getNewRevisionGraphURI(graphName));
        this.revisionDraft = new RevisionDraft(getUriCalculator(), revisionGraph, null, null);
        this.isCreatedWithRequest = true;
    }

    /**
     * The constructor.
     * Creates an initial commit draft by using the corresponding add and delete sets.
     *
     * @param graphName the graph name
     * @param addSet the add set as N-Triples
     * @param deleteSet the delete set as N-Triples
     * @param user the user
     * @param message the message
     * @throws InternalErrorException
     */
    protected InitialCommitDraft(String graphName, String addSet, String deleteSet, String user, String message) throws InternalErrorException {
        super(null);
        this.setUser(user);
        this.setMessage(message);
        this.revisionGraph = new RevisionGraph(graphName, getUriCalculator().getNewRevisionGraphURI(graphName));
        this.revisionDraft = new RevisionDraft(getUriCalculator(), revisionGraph, addSet, deleteSet);
        this.isCreatedWithRequest = false;
    }

    /**
     * Creates the commit draft as a new commit in the triplestore and creates the corresponding revisions.
     *
     * @return the list of created commits
     * @throws InternalErrorException
     */
    protected InitialCommit createInTripleStore() throws InternalErrorException {
        String commitUri = getUriCalculator().getNewCommitURI(revisionDraft.getRevisionGraph(), revisionDraft.getNewRevisionIdentifier());

        // Create graph
        if (!getUriCalculator().checkNamedGraphExistence(revisionDraft.getRevisionGraph().getGraphName())) {
            getTripleStoreInterface().executeCreateGraph(revisionDraft.getRevisionGraph().getGraphName());
        } else {
            throw new InternalErrorException("The calculated revision graph is already in use.");
        }

        Revision generatedRevision = revisionDraft.createInTripleStore();

        MasterDraft masterDraft = new MasterDraft(getUriCalculator(), revisionGraph, generatedRevision);
        Branch generatedBranch = masterDraft.createInTripleStore();

        updateReferencedFullGraph(generatedBranch.getFullGraphURI(), generatedRevision.getChangeSet());

        addMetaInformation(generatedRevision, commitUri, generatedBranch.getReferenceURI());

        return new InitialCommit(revisionDraft.getRevisionGraph(), commitUri, getUser(), getTimeStamp(), getMessage(), generatedRevision, generatedBranch);
    }

    /**
     * Extracts the graph name out of the given request.
     *
     * @return the graph name
     * @throws InternalErrorException
     */
    private String getGraphNameOfRequest() throws InternalErrorException {
        final Pattern patternCreateGraph = Pattern.compile("CREATE\\s*(SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
                patternModifier);

        String graphName = null;
        Matcher m = patternCreateGraph.matcher(getRequest().query_sparql);
        boolean found = false;
        while (m.find()) {
            found = true;
            graphName = m.group("graph");
        }
        if (!found) {
            throw new QueryErrorException("Query doesn't contain a correct CREATE query:\n" + getRequest().query_sparql);
        }

        return graphName;
    }

    /**
     * Creates a new revision graph for the new graph under revision control and adds the necessary meta data.
     *
     * @param generatedRevision the generated revision
     * @param commitUri the commit URI
     * @param branchUri the branch URI
     */
    private void addMetaInformation(Revision generatedRevision, String commitUri, String branchUri) {

        String queryAddRevisionGraph = Config.prefixes + String.format(
                "INSERT DATA { GRAPH <%1$s> {"
                        + "  <%2$s> a rmo:Graph, rmo:Entity ;"
                        + "    rmo:hasRevisionGraph <%3$s>;"
                        + "    sddo:hasDefaultSDD sdd:defaultSDD."
                        + "} }",
                Config.revision_graph, revisionGraph.getGraphName(), revisionGraph.getRevisionGraphUri());
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryAddRevisionGraph);

        // Create new revision
        String queryContent = String.format(
                "<%s> a rmo:InitialCommit, rmo:Commit; "
                        + "	rmo:wasAssociatedWith <%s> ;"
                        + "	rmo:generated <%s>, <%s> ;"
                        + " rmo:hasChangeSet <%s> ;"
                        + "	rmo:commitMessage \"%s\" ;"
                        + "	rmo:timeStamp \"%s\"^^xsd:dateTime .%n",
                commitUri, Helper.getUserURI(getUser()), generatedRevision.getRevisionURI(), branchUri, generatedRevision.getChangeSet().getChangeSetURI(), getMessage(), getTimeStamp());

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisionGraph.getRevisionGraphUri(), queryContent);

        getTripleStoreInterface().executeUpdateQuery(queryRevision);
    }

}