package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.existentobjects.InitialCommit;
import de.tud.plt.r43ples.management.Config;
import de.tud.plt.r43ples.management.R43plesRequest;
import de.tud.plt.r43ples.triplestoreInterface.TripleStoreInterfaceSingleton;
import org.apache.log4j.Logger;

/**
 * Collection of information for creating a new initial commit.
 *
 * @author Stephan Hensel
 */
public class InitialCommitDraft extends CommitDraft {

    /**
     * The logger.
     **/
    private Logger logger = Logger.getLogger(InitialCommitDraft.class);

//    /** The pattern modifier. **/
//    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
//
//    /** The revision draft. **/
//    private RevisionDraft revisionDraft;
    /**
     * States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets)
     **/
    private boolean isCreatedWithRequest;

    /**
     * The constructor.
     * Creates an initial commit draft by using the corresponding R43ples request.
     *
     * @param request the request received by R43ples
     */
    public InitialCommitDraft(R43plesRequest request) {
        super(request);
        this.isCreatedWithRequest = true;
    }

    // TODO


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
//        this.revisionDraft = new RevisionDraft(new RevisionGraph(graphName), derivedFromIdentifier, addSet, deleteSet);
        this.setUser(user);
        this.setMessage(message);
        this.isCreatedWithRequest = false;
    }

    /**
     * Creates the commit draft as a new commit in the triplestore and creates the corresponding revisions.
     *
     * @return the list of created commits
     */
    protected InitialCommit createCommitInTripleStore() throws InternalErrorException {
        if (!isCreatedWithRequest) {
//            revisionDraft.createRevisionInTripleStore();
//            ArrayList<UpdateCommit> commitList = new ArrayList<>();
//            commitList.add(addMetaInformation(revisionDraft));
//            return commitList;
        } else {
//            return this.updateChangeSetsByRewrittenQuery();
        }
        return null;
    }



    /**
     * Put existing graph under version control. Existence of graph is not checked. Current date is used for commit timestamp
     *
     * @param graphName the graph name of the existing graph
     */
    public String putGraphUnderVersionControl(final String graphName, final String datetime) {
//        getTripleStoreInterface()
        // TODO Move to initial commit
        logger.debug("Put existing graph under version control with the name " + graphName);

        String revisiongraph = graphName + "-revisiongraph";
//TODO
//        while (checkGraphExistence(revisiongraph)){
//            revisiongraph += "x";
//        }

        String queryAddRevisionGraph = Config.prefixes + String.format(
                "INSERT DATA { GRAPH <%1$s> {"
                        + "  <%2$s> a rmo:Graph;"
                        + "    rmo:hasRevisionGraph <%3$s>;"
                        + "    sddo:hasDefaultSDD sdd:defaultSDD."
                        + "} }",
                Config.revision_graph, graphName, revisiongraph);
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryAddRevisionGraph);


        String revisionNumber = "1";
        String revisionUri = graphName + "-revision-" + revisionNumber;
        String commitUri = graphName + "-commit-" + revisionNumber;
        String branchUri = graphName + "-master";

        // Create new revision
        String queryContent = String.format(
                "<%s> a rmo:Revision;"
                        + "	rmo:revisionNumber \"%s\";"
                        + "	rmo:belongsTo <%s>. ",
                revisionUri, revisionNumber, branchUri);

        // Add MASTER branch
        queryContent += String.format(
                "<%s> a rmo:Master, rmo:Branch, rmo:Reference;"
                        + " rmo:fullGraph <%s>;"
                        + "	rmo:references <%s>;"
                        + "	rdfs:label \"master\".",
                branchUri, graphName, revisionUri);

        queryContent += String.format(
                "<%s> a rmo:RevisionCommit, rmo:BranchCommit; "
                        + "	prov:wasAssociatedWith <%s> ;"
                        + "	prov:generated <%s>, <%s> ;"
                        + "	dc-terms:title \"initial commit\" ;"
                        + "	prov:atTime \"%s\"^^xsd:dateTime .%n",
                commitUri,  "http://eatld.et.tu-dresden.de/user/r43ples", revisionUri, branchUri, datetime);

        String queryRevision = Config.prefixes + String.format("INSERT DATA { GRAPH <%s> {%s} }", revisiongraph, queryContent);

        //TripleStoreInterfaceSingleton.get().executeCreateGraph(graph);
        TripleStoreInterfaceSingleton.get().executeUpdateQuery(queryRevision);

        return revisionNumber;
    }


}