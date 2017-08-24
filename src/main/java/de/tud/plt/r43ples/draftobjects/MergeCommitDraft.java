package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.exception.QueryErrorException;
import de.tud.plt.r43ples.existentobjects.MergeCommit;
import de.tud.plt.r43ples.existentobjects.RevisionGraph;
import de.tud.plt.r43ples.existentobjects.ThreeWayMergeCommit;
import de.tud.plt.r43ples.management.R43plesCommit;
import de.tud.plt.r43ples.management.R43plesMergeCommit;
import de.tud.plt.r43ples.management.R43plesRequest;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Collection of information for creating a new merge commit.
 *
 * @author Stephan Hensel
 */
public class MergeCommitDraft extends CommitDraft {

    /** The logger. **/
    private Logger logger = Logger.getLogger(MergeCommitDraft.class);

    /** The pattern modifier. **/
    private final int patternModifier = Pattern.DOTALL + Pattern.MULTILINE + Pattern.CASE_INSENSITIVE;
    /** The merge query pattern. **/
    private final Pattern patternMergeQuery = Pattern.compile(
            "(?<action>MERGE|REBASE|MERGE FF)\\s*(?<type>FORCE|AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameFrom>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameInto>[^\"]*?)\"(?<with>\\s*WITH\\s*\\{(?<triples>.*)\\})?",
            patternModifier); //TODO add COUNT for advanced rebase

    /** The triples of the query WITH part. **/
    private String triples;
    /** The branch name (from). **/
    private String branchNameFrom;
    /** The branch name (into). **/
    private String branchNameInto;
    /** The SDD URI to use. **/
    private String sdd;
    /** The graph name **/
    private String graphName;
    /** The revision graph. **/
    private RevisionGraph revisionGraph;
    /** The query type (FORCE, AUTO, MANUAL). **/
    private MergeTypes type;
    /** The query action (MERGE, REBASE, MERGE FF). **/
    private MergeActions action;
    /** States if the WITH part is available. **/
    private boolean with;

    /** States if this commit draft was created by a request or add and delete sets. (true => request, false => add/delete sets) **/
    private boolean isCreatedWithRequest;


    /**
     * The constructor.
     *
     * @param request the request received by R43ples
     * @throws InternalErrorException
     */
    public MergeCommitDraft(R43plesRequest request) throws InternalErrorException {
        super(request);
        this.extractRequestInformation();
        this.isCreatedWithRequest = true;
    }

    /**
     * The constructor.
     * Creates an merge commit draft by using the corresponding meta information.
     *
     * @param graphName the graph name
     * @param branchNameFrom the branch name (from)
     * @param branchNameInto the branch name (into)
     * @param user the user
     * @param message the message
     * @param sdd the SDD URI to use
     * @param action the query action (MERGE, REBASE, MERGE FF)
     * @param triples the triples of the query WITH part
     * @param type the query type (FORCE, AUTO, MANUAL)
     * @param with states if the WITH part is available
     * @throws InternalErrorException
     */
    protected MergeCommitDraft(String graphName, String branchNameFrom, String branchNameInto, String user, String message, String sdd, MergeActions action, String triples, MergeTypes type, boolean with) throws InternalErrorException {
        super(null);
        this.setUser(user);
        this.setMessage(message);

        this.graphName = graphName;
        this.revisionGraph = new RevisionGraph(graphName);
        this.branchNameFrom = branchNameFrom;
        this.branchNameInto = branchNameInto;
        this.sdd = sdd;
        this.action = action;
        this.triples = triples;
        this.type = type;
        this.with = with;

        this.isCreatedWithRequest = false;
    }

    /**
     * Extracts the request information and stores it to local variables.
     *
     * @throws InternalErrorException
     */
    private void extractRequestInformation() throws InternalErrorException {
        Matcher m = patternMergeQuery.matcher(getRequest().query_sparql);

        boolean foundEntry = false;

        while (m.find()) {
            foundEntry = true;

            switch (m.group("action").toUpperCase()) {
                case "MERGE":
                    action = MergeActions.MERGE;
                    break;
                case "REBASE":
                    action = MergeActions.REBASE;
                    break;
                case "MERGE FF":
                    action = MergeActions.MERGE_FF;
                    break;
                default:
                    action = null;
                    break;
            }
            String typeID = m.group("type");
            if (typeID != null) {
                switch (typeID.toUpperCase()) {
                    case "AUTO":
                        type = MergeTypes.AUTO;
                        break;
                    case "MANUAL":
                        type = MergeTypes.MANUAL;
                        break;
                    case "FORCE":
                        type = MergeTypes.FORCE;
                        break;
                    default:
                        type = null;
                        break;
                }
            } else {
                type = null;
            }

            graphName = m.group("graph");
            revisionGraph = new RevisionGraph(graphName);
            sdd = m.group("sdd");
            branchNameFrom = m.group("branchNameFrom").toLowerCase();
            branchNameInto = m.group("branchNameInto").toLowerCase();
            with = m.group("with") != null;
            triples = m.group("triples");

            logger.debug("type: " + type);
            logger.debug("graph: " + graphName);
            logger.debug("sdd: " + sdd);
            logger.debug("branchNameFrom: " + branchNameFrom);
            logger.debug("branchNameInto: " + branchNameInto);
            logger.debug("with: " + with);
            logger.debug("triples: " + triples);
        }
        if (!foundEntry) {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }

    }

    /**
     * Creates the commit draft as a new commit in the triple store and creates the corresponding revisions.
     *
     * @return the created commit
     */
    protected MergeCommit createCommitInTripleStore() throws InternalErrorException {
        // Select the right child element and create a corresponding commit using the createCommitInTripleStore method.
        if (action.equals(MergeActions.MERGE) && ((type == null) || !type.equals(MergeTypes.FORCE))) {
            ThreeWayMergeCommitDraft threeWayMergeCommit = new ThreeWayMergeCommitDraft(graphName, branchNameFrom, branchNameInto, getUser(), getMessage(), sdd, triples, type, with);
            return threeWayMergeCommit.createCommitInTripleStore();
        } else if (action.equals(MergeActions.MERGE_FF) && type.equals(null)) {
            //TODO Merge FF
            throw new InternalErrorException("Fast forward merge currently not implemented.");
        } else if (action.equals(MergeActions.REBASE)) {
            // TODO Rebase
            // TODO Advanced rebase
            throw new InternalErrorException("Rebase and advanced rebase currently not implemented.");
        } else {
            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
        }
    }

    /**
     * Get the triples of the query WITH part.
     *
     * @return the triples of the query WITH part
     */
    protected String getTriples() {
        return triples;
    }

    /**
     * Get the branch name (from).
     *
     * @return the branch name
     */
    protected String getBranchNameFrom() {
        return branchNameFrom;
    }

    /**
     * Get the branch name (into).
     *
     * @return the branch name
     */
    protected String getBranchNameInto() {
        return branchNameInto;
    }

    /**
     * Get the SDD URI to use.
     *
     * @return the SDD URI to use
     */
    protected String getSdd() {
        return sdd;
    }

    /**
     * The revision graph.
     *
     * @return the revision graph
     */
    protected RevisionGraph getRevisionGraph() {
        return revisionGraph;
    }

    /**
     * The graph name.
     *
     * @return the graph name
     */
    protected String getGraphName() {
        return graphName;
    }

    /**
     * Get the query type (FORCE, AUTO, MANUAL).
     *
     * @return the query type
     */
    protected MergeTypes getType() {
        return type;
    }

    /**
     * Get the query action (MERGE, REBASE, MERGE FF).
     *
     * @return the query action
     */
    protected MergeActions getAction() {
        return action;
    }

    /**
     * Get the boolean indicator if the WITH part os available.
     *
     * @return true if the WITH part is available
     */
    protected boolean isWith() {
        return with;
    }

}