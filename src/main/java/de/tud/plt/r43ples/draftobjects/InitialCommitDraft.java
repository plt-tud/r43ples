package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.Branch;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.existentobjects.Revision;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new initial commit.
 *
 * @author Stephan Hensel
 */
public class InitialCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(InitialCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;

    /** The revision draft. **/
    private RevisionDraft revisionDraft;
    /** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
    private boolean isCreatedWithRequest;


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
        this.revisionDraft = new RevisionDraft(getRevisionManagement(), new RevisionGraph(graphName, getRevisionManagement().getNewRevisionGraphURI(graphName)), null, null);
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
        this.revisionDraft = new RevisionDraft(getRevisionManagement(), new RevisionGraph(graphName, getRevisionManagement().getNewRevisionGraphURI(graphName)), addSet, deleteSet);
        this.isCreatedWithRequest = false;
    }

    /**
     * Creates the commit draft as a new commit in the triplestore and creates the corresponding revisions.
     *
     * @return the list of created commits
     */
    protected InitialCommit createCommitInTripleStore() throws InternalErrorException {
        String commitUri = getRevisionManagement().getNewCommitURI(revisionDraft.getRevisionGraph(), revisionDraft.getNewRevisionIdentifier());
        String masterUri = getRevisionManagement().getNewMasterURI(revisionDraft.getRevisionGraph());

        // Create graph
        if (!getRevisionManagement().checkNamedGraphExistence(revisionDraft.getRevisionGraph().getGraphName())) {
            getTripleStoreInterface().executeCreateGraph(revisionDraft.getRevisionGraph().getGraphName());
        } else {
            throw new InternalErrorException("The calculated revision graph is already in use.");
        }

        addMetaInformation(revisionDraft, commitUri, masterUri);

        Revision generatedRevision = revisionDraft.createRevisionInTripleStore();
        Branch generatedBranch = new Branch(revisionDraft.getRevisionGraph(), masterUri, false);

        return new InitialCommit(revisionDraft.getRevisionGraph(), commitUri, getUser(), getTimeStamp(), getMessage(), generatedRevision, generatedBranch);
    }

    /**
     * Extracts the graph name out of the given request.
     *
     * @return the graph name
     * @throws InternalErrorException
     */
    private String getGraphNameOfRequest() throws InternalErrorException {
        final Pattern patternCreateGraph = Pattern.compile("CREATE\\s*(?<silent>SILENT)?\\s*GRAPH\\s*<(?<graph>[^>]*)>",
                patternModifier);

        String graphName = null;
        Matcher m = patternCreateGraph.matcher(getRequest().query_sparql);
        boolean found = false;
        while (m.find()) {
            found = true;
            graphName = m.group("graph");
            // String silent = m.group("silent");
        }
        if (!found) {
            throw new QueryErrorException("Query doesn't contain a correct CREATE query:\n" + getRequest().query_sparql);
        }

        return graphName;
    }

    /**
     * Creates a new revision graph for the new graph under revision control and adds the necessary meta data.
     *
     * @param revisionDraft the revision draft
     * @param commitUri the commit URI
     * @param branchUri the branch URI
     * @throws InternalErrorException
     */
    private void addMetaInformation(RevisionDraft revisionDraft, String commitUri, String branchUri) throws InternalErrorException {

        String queryAddRevisionGraph = Config.prefixes + String.format(
                "INSERT DATA { GRAPH <%1$s> {"
                        + "  <%2$s> a rmo:Graph;"
                        + "    rmo:hasRevisionGraph <%3$s>;"
                        + "    sddo:hasDefaultSDD sdd:defaultSDD."
                        + "} }",
                Config.revision_graph, revisionDraft.getRevisionGraph().getGraphName(), revisionDraft.getRevisionGraph().getRevisionGraphUri());
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryAddRevisionGraph);

        // Create new revision
        String queryContent = String.format(
                "<%s> a rmo:Revision;"
                        + "	rmo:revisionNumber \"%s\";"
                        + "	rmo:belongsTo <%s>. ",
                revisionDraft.getRevisionURI(), revisionDraft.getNewRevisionIdentifier(), branchUri);

        // Add MASTER branch
        queryContent += String.format(
                "<%s> a rmo:Master, rmo:Branch, rmo:Reference;"
                        + " rmo:fullGraph <%s>;"
                        + "	rmo:references <%s>;"
                        + "	rdfs:label \"master\".",
                branchUri, revisionDraft.getRevisionGraph().getGraphName(), revisionDraft.getRevisionURI());

        queryContent += String.format(
                "<%s> a rmo:RevisionCommit, rmo:ReferenceCommit; "
                        + "	prov:wasAssociatedWith <%s> ;"
                        + "	prov:generated <%s>, <%s> ;"
                        + "	dc-terms:title \"initial commit\" ;"
                        + "	prov:atTime \"%s\"^^xsd:dateTime .%n",
                commitUri,  "http://eatld.et.tu-dresden.de/user/r43ples", revisionDraft.getRevisionURI(), branchUri, getTimeStamp());

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisionDraft.getRevisionGraph().getRevisionGraphUri(), queryContent);

        getTripleStoreInterface().executeUpdateQuery(queryRevision);

    }

}