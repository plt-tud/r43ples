package de.tud.plt.r43ples.draftobjects;

import de.tud.plt.r43ples.exception.InternalErrorException;
import de.tud.plt.r43ples.exception.OutdatedException;
import de.tud.plt.r43ples.exception.QueryErrorException;
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
            "(?<action>MERGE|REBASE|MERGE FF)\\s*(?<type>FORCE|AUTO|MANUAL)?\\s*GRAPH\\s*<(?<graph>[^>]*?)>\\s*(SDD\\s*<(?<sdd>[^>]*?)>)?\\s*BRANCH\\s*\"(?<branchNameA>[^\"]*?)\"\\s*INTO\\s*\"(?<branchNameB>[^\"]*?)\"(?<with>\\s*WITH\\s*\\{(?<triples>.*)\\})?",
            patternModifier);

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

//        boolean foundEntry = false;
//
//        action = m.group("action");
//        type = m.group("type");
//        graphName = m.group("graph");
//        sdd = m.group("sdd");
//        branchNameFrom = m.group("branchNameFrom").toLowerCase();
//        branchNameInto = m.group("branchNameInto").toLowerCase();
//        with = m.group("with") != null;
//        triples = m.group("triples");
//
//        logger.debug("type: " + type);
//        logger.debug("graph: " + graphName);
//        logger.debug("sdd: " + sdd);
//        logger.debug("branchNameFrom: " + branchNameA);
//        logger.debug("branchNameInto: " + branchNameB);
//        logger.debug("with: " + with);
//        logger.debug("triples: " + triples);


    }

//        while (m.find()) {
//            foundEntry = true;
//            String action = m.group("action");
//            this.graphName = m.group("graph");
//            this.revisionIdentifier = m.group("revision").toLowerCase();
//            this.referenceName = m.group("name").toLowerCase();
//            if (action.equals("TAG")) {
//                this.isBranch = false;
//            } else if (action.equals("BRANCH")) {
//                this.isBranch = true;
//            } else {
//                throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
//            }
//        }
//        if (!foundEntry) {
//            throw new QueryErrorException("Error in query: " + getRequest().query_sparql);
//        }
//    }
//
//
//
//        public R43plesMergeCommit(R43plesRequest request) throws InternalErrorException {
//            super(request);
//            Matcher m = patternMergeQuery.matcher(request.query_sparql);
//            if (!m.find())
//                throw new InternalErrorException("Error in query: " + request.query_sparql);
//
//            action = m.group("action");
//            type = m.group("type");
//            graphName = m.group("graph");
//            sdd = m.group("sdd");
//            branchNameA = m.group("branchNameA").toLowerCase();
//            branchNameB = m.group("branchNameB").toLowerCase();
//            with = m.group("with")!=null;
//            triples = m.group("triples");
//
//            logger.debug("type: " + type);
//            logger.debug("graph: " + graphName);
//            logger.debug("sdd: " + sdd);
//            logger.debug("branchNameA: " + branchNameA);
//            logger.debug("branchNameB: " + branchNameB);
//            logger.debug("with: " + with);
//            logger.debug("triples: " + triples);
//        }
//
//
//        public R43plesMergeCommit(final String graphName,
//                                  final String branchNameA,
//                                  final String branchNameB,
//                                  final String user,
//                                  final String message,
//                                  final String format
//        ) {
//            super(new R43plesRequest("", format));
//            this.graphName = graphName;
//            this.branchNameA = branchNameA;
//            this.branchNameB = branchNameB;
//            this.user = user;
//            this.message = message;
//            this.sdd = null;
//            this.action = null;
//            this.triples = null;
//            this.type = null;
//            this.with = false;
//        }
//
//
//


}